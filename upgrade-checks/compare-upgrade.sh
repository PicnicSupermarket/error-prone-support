#!/usr/bin/env bash

# Compares the behaviour of the build at two commits, such as before and after
# a dependency upgrade, and prints the result as Markdown.
#
# Each `procedures/<name>.sh` file is sourced, and defines two functions that
# are executed from the root of a worktree of each commit. The procedure
# applies if the output of `probe` differs between the commits, if `probe`
# fails, or if the procedure itself changed. In that case the output of
# `collect` is compared. The file's header comment, following the shebang
# line and a blank line, describes the comparison.

set -e -u -o pipefail

BASE_REF="${1:?Usage: ${0} <base-ref> <head-ref>}"
HEAD_REF="${2:?Usage: ${0} <base-ref> <head-ref>}"
# Longer diffs are truncated, such that the report fits in a GitHub comment.
MAX_DIFF_LENGTH=10000
PROCEDURES_DIR="$(cd "$(dirname "${0}")/procedures" && pwd)"
TREES="$(mktemp -d)"
trap 'rm -rf -- "${TREES}"; git worktree prune' EXIT
trap 'exit 1' INT TERM HUP
git worktree add -q --detach "${TREES}/base" "${BASE_REF}"
git worktree add -q --detach "${TREES}/head" "${HEAD_REF}"

# Executes the given function of the given procedure file in the given
# worktree.
function run() {
  (cd "${TREES}/${3}" && bash -e -u -o pipefail -c 'source "${0}" && "${1}"' "${1}" "${2}")
}

# Prints the given text in a code block of the given language, using a fence
# that is longer than any backtick sequence in the text.
function fence() {
  local fence='```'
  while [[ "${2}" == *"${fence}"* ]]; do
    fence+='`'
  done
  printf '%s%s\n%s\n%s\n' "${fence}" "${1}" "${2}" "${fence}"
}

declare -A probe
for file in "${PROCEDURES_DIR}"/*.sh; do
  for side in base head; do
    git -C "${TREES}/${side}" reset -q --hard
    git -C "${TREES}/${side}" clean -q -f -d -x
    probe[${side}]="$(run "${file}" probe "${side}")" \
      || probe[${side}]="(${side} probe failed with exit status ${?})"
  done
  if [ "${probe[base]}" = "${probe[head]}" ] && git diff --quiet "${BASE_REF}" "${HEAD_REF}" -- "${file}"; then
    continue
  fi

  printf '### `%s`\n\n' "$(basename "${file}" .sh)"
  sed -n '3,/^$/ s/^# \{0,1\}//p' "${file}"
  echo
  fence text "Base: $(paste -s -d ' ' <<< "${probe[base]}")
Head: $(paste -s -d ' ' <<< "${probe[head]}")"
  echo

  failures=''
  for side in base head; do
    run "${file}" collect "${side}" > "${TREES}/${side}.txt" \
      || failures+="${failures:+, }${side} (exit status ${?})"
  done

  output_diff="$(diff -u --label base --label head "${TREES}/base.txt" "${TREES}/head.txt")" || true
  if [ -n "${failures}" ]; then
    echo ":x: Inconclusive, as collection failed for ${failures}; see the job log."
  elif [ -z "${output_diff}" ]; then
    echo ":white_check_mark: Identical output ($(wc -c < "${TREES}/base.txt") bytes)."
  else
    echo ':warning: Output differs:'
  fi
  if [ "${#output_diff}" -gt "${MAX_DIFF_LENGTH}" ]; then
    output_diff="${output_diff:0:${MAX_DIFF_LENGTH}}"$'\n[Truncated]'
  fi
  if [ -n "${output_diff}" ]; then
    echo
    fence diff "${output_diff}"
  fi
  echo
done
