#!/usr/bin/env bash

# Runs a GitHub Actions workflow locally using `act` (or `gh act` if `act` is
# unavailable). The first argument selects the workflow; any remaining arguments
# are forwarded to `act`.
#
# Requires `act` (https://nektosact.com) or the `nektos/gh-act` GitHub CLI
# extension (`gh extension install nektos/gh-act`). Credentials for external
# services are read from environment variables; see `AGENTS.md` for the list
# of variables and their purpose.

set -e -u -o pipefail

declare -a ACT
if command -v act >/dev/null 2>&1; then
  ACT=(act)
elif gh act --version >/dev/null 2>&1; then
  ACT=(gh act)
else
  echo 'This script requires `act` or the `nektos/gh-act` GitHub CLI extension; see https://nektosact.com.' >&2
  exit 1
fi

SCRIPT_DIR="$(dirname "${0}")"
EVENTS_DIR="${SCRIPT_DIR}/.github/act/events"
REPO_ROOT="$(git -C "${SCRIPT_DIR}" rev-parse --show-toplevel)"

# Redirect GitHub checkouts to the local repository. This allows workflows to
# run against unpushed commits: `actions/checkout` inside composite actions
# (e.g. `s4u/setup-maven-action`) fetches `GITHUB_SHA` from the GitHub remote,
# which fails if the commit has not been pushed. By rewriting the URL to a
# bind-mounted local path, the fetch succeeds regardless.
ACT+=(
  --container-options "-v ${REPO_ROOT}:/tmp/local-repo"
  --env "GIT_CONFIG_COUNT=2"
  --env "GIT_CONFIG_KEY_0=url.file:///tmp/local-repo.insteadOf"
  --env "GIT_CONFIG_VALUE_0=https://github.com/PicnicSupermarket/error-prone-support"
  --env "GIT_CONFIG_KEY_1=protocol.file.allow"
  --env "GIT_CONFIG_VALUE_1=always"
)

function print_usage() {
  cat << 'EOF'
Usage: ./run-act.sh <workflow> [<act-flags...>]

Available workflows:
  assign-milestone            pull_request_target  assign-milestone.yml
  build                       push                 build.yml
  codeql                      pull_request         codeql.yml
  copilot-setup-steps         workflow_dispatch    copilot-setup-steps.yml
  default-branch-health-gate  pull_request         default-branch-health-gate.yml
  deploy-website              pull_request         deploy-website.yml
  error-prone-compat          push                 error-prone-compat.yml
  integration-tests           issue_comment        integration-tests.yml
  openssf-scorecard           pull_request         openssf-scorecard.yml
  pitest-analyze              pull_request         pitest-analyze-pr.yml
  pitest-update               workflow_run         pitest-update-pr.yml
  reviewdog                   pull_request         reviewdog.yml
  sonarcloud                  push                 sonarcloud.yml
  suggest-commit-message      pull_request         suggest-commit-message.yml
  validate-review-checklist   push                 validate-review-checklist.yml
  validate-workflows          push                 validate-workflows.yml
EOF
}

if [ "${#}" -lt 1 ]; then
  print_usage >&2
  exit 1
fi

if [ "${1}" = '--help' ]; then
  print_usage
  exit
fi

workflow="${1}"
shift

case "${workflow}" in
  assign-milestone)
    "${ACT[@]}" pull_request_target \
      -W '.github/workflows/assign-milestone.yml' \
      --eventpath "${EVENTS_DIR}/pull_request_target.labeled.json" \
      "${@}"
    ;;
  build)
    "${ACT[@]}" push \
      -W '.github/workflows/build.yml' \
      --eventpath "${EVENTS_DIR}/push.json" \
      --matrix os:ubuntu-24.04 \
      "${@}"
    ;;
  codeql)
    "${ACT[@]}" pull_request \
      -W '.github/workflows/codeql.yml' \
      --eventpath "${EVENTS_DIR}/pull_request.json" \
      "${@}"
    ;;
  copilot-setup-steps)
    "${ACT[@]}" workflow_dispatch \
      -W '.github/workflows/copilot-setup-steps.yml' \
      "${@}"
    ;;
  default-branch-health-gate)
    "${ACT[@]}" pull_request \
      -W '.github/workflows/default-branch-health-gate.yml' \
      --eventpath "${EVENTS_DIR}/pull_request.json" \
      "${@}"
    ;;
  deploy-website)
    "${ACT[@]}" pull_request \
      -W '.github/workflows/deploy-website.yml' \
      --eventpath "${EVENTS_DIR}/pull_request.json" \
      "${@}"
    ;;
  error-prone-compat)
    "${ACT[@]}" push \
      -W '.github/workflows/error-prone-compat.yml' \
      --eventpath "${EVENTS_DIR}/push.json" \
      "${@}"
    ;;
  integration-tests)
    "${ACT[@]}" issue_comment \
      -W '.github/workflows/integration-tests.yml' \
      --eventpath "${EVENTS_DIR}/issue_comment.created.json" \
      "${@}"
    ;;
  openssf-scorecard)
    "${ACT[@]}" pull_request \
      -W '.github/workflows/openssf-scorecard.yml' \
      --eventpath "${EVENTS_DIR}/pull_request.json" \
      "${@}"
    ;;
  pitest-analyze)
    "${ACT[@]}" pull_request \
      -W '.github/workflows/pitest-analyze-pr.yml' \
      --eventpath "${EVENTS_DIR}/pull_request.json" \
      "${@}"
    ;;
  pitest-update)
    "${ACT[@]}" workflow_run \
      -W '.github/workflows/pitest-update-pr.yml' \
      --eventpath "${EVENTS_DIR}/workflow_run.completed.json" \
      "${@}"
    ;;
  reviewdog)
    "${ACT[@]}" pull_request \
      -W '.github/workflows/reviewdog.yml' \
      --eventpath "${EVENTS_DIR}/pull_request.json" \
      "${@}"
    ;;
  sonarcloud)
    "${ACT[@]}" push \
      -W '.github/workflows/sonarcloud.yml' \
      --eventpath "${EVENTS_DIR}/push.json" \
      "${@}"
    ;;
  suggest-commit-message)
    "${ACT[@]}" pull_request \
      -W '.github/workflows/suggest-commit-message.yml' \
      --eventpath "${EVENTS_DIR}/pull_request.json" \
      "${@}"
    ;;
  validate-review-checklist)
    "${ACT[@]}" push \
      -W '.github/workflows/validate-review-checklist.yml' \
      --eventpath "${EVENTS_DIR}/push.json" \
      "${@}"
    ;;
  validate-workflows)
    "${ACT[@]}" push \
      -W '.github/workflows/validate-workflows.yml' \
      --eventpath "${EVENTS_DIR}/push.json" \
      "${@}"
    ;;
  *)
    echo "Unknown workflow: ${workflow}" >&2
    exit 1
    ;;
esac
