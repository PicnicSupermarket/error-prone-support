#!/usr/bin/env bash

# Upserts a suggested commit message comment on the specified pull request,
# based on the workflow definition present on the specified branch. The
# generated commit message is also written to stdout.
#
# No commit message is generated if the pull request already has a suggestion
# by someone else, or if the workflow's comment was edited manually. In that
# case a note is written to stderr, and no commit message is written to stdout.

set -e -u -o pipefail

workflow='suggest-commit-message.yml'
branch="${1:?Specify a branch}"
pr_number="${2:?Specify a PR number}"

gh workflow run "${workflow}" --ref "${branch}" -f pr_number="${pr_number}"

# The new run may not start immediately, so we wait a bit.
sleep 30

run_id=$(
  gh run list --workflow="${workflow}" --event workflow_dispatch --limit 1 --json databaseId \
    | jq -r '.[].databaseId'
)

gh run watch --exit-status "${run_id}"

tmp_dir="$(mktemp -d)"
trap 'rm -rf -- "${tmp_dir}"' INT TERM HUP EXIT

has_artifact=$(
  gh api "repos/{owner}/{repo}/actions/runs/${run_id}/artifacts" \
    --jq '.artifacts | any(.name == "suggested-commit-message")'
)

if [ "${has_artifact}" = 'true' ]; then
  gh run download -n suggested-commit-message --dir "${tmp_dir}" "${run_id}"
  cat "${tmp_dir}/suggested-commit-message.txt"
else
  echo 'No commit message generated: the PR already has a suggestion, or its suggestion comment was edited.' >&2
fi
