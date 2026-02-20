#!/usr/bin/env bash

set -e -u -o pipefail

spec_file='integration-test-data.yml'
branch="${1:?Specify a branch}"
shift

if [ "${#}" -gt 0 ]; then
  prs="${@}"
else
  prs="$(yq -r '.tests[].pr' "${spec_file}")"
fi

# Use a lock file to synchronize test result output.
result_lock="$(mktemp)"
trap 'rm -f -- "${result_lock}"' INT TERM HUP EXIT

test_pr() {
  local pr="${1}"

  # Retrieve the expected suggested commit message.
  local expected
  expected="$(yq -r ".tests[] | select(.pr == ${pr}) | .expected" "${spec_file}")"

  # Run the suggested commit message workflow and capture its output.
  local output
  output="$(./suggest-commit-message.sh "${branch}" "${pr}" true)"

  # Extract the newly suggested commit message.
  local actual
  actual="$(echo "${output}" | sed -n '/^---$/,$p' | tail -n +2)"

  # Compute diff.
  local diff_output
  diff_output="$(diff -u <(echo "${expected}") <(echo "${actual}") 2>/dev/null || true)"

  # Report whether the new message differs from what's expected.
  if [ -z "${diff_output}" ]; then
    flock "${result_lock}" echo "Tested PR #${pr}: OK"
  else
    flock "${result_lock}" echo -e "Tested PR #${pr}: KO\n${diff_output}"
  fi
}

# Trigger concurrent execution of tests for each PR.
for pr in ${prs}; do
  test_pr "${pr}" &
done

# Wait for all tests to complete.
wait
