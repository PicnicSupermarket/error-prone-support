#!/usr/bin/env bash

# Upserts a suggested commit message comment on the specified pull request,
# based on the workflow definition present on the specified branch. The
# generated commit message is also written to stdout.

set -e -u -o pipefail

workflow='suggest-commit-message.yml'
branch="${1:?Specify a branch}"
pr_number="${2:?Specify a PR number}"
dry_run="${3:-false}"
lock_file="${4:-$(mktemp)}"

run_id="$(
  xargs -0 <<EOF flock "${lock_file}" sh -c
    gh workflow run '${workflow}' --ref '${branch}' -f pr_number='${pr_number}' -f dry_run='${dry_run}' 1>/dev/null

    # The new run may not start immediately, so we wait a bit.
    sleep 10

    gh run list --workflow='${workflow}' --event workflow_dispatch --limit 1 --json databaseId \
      | jq -r '.[].databaseId'
EOF
)"

gh run watch "${run_id}"

tmp_dir="$(mktemp -d)"
trap 'rm -rf -- "${tmp_dir}"' INT TERM HUP EXIT

gh run download -n suggested-commit-message --dir "${tmp_dir}" "${run_id}"
echo 'Suggested commit message:'
echo '---'
cat "${tmp_dir}/suggested-commit-message.txt"
