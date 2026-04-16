#!/usr/bin/env bash

# Runs a GitHub Actions workflow locally using `act`. The first argument
# selects the workflow; any remaining arguments are forwarded to `act`.
#
# Requires `act` to be installed. See https://nektosact.com for details.
# Credentials for external services are read from environment variables;
# see AGENTS.md for the list of variables and their purpose.

set -e -u -o pipefail

SCRIPT_DIR="$(dirname "${0}")"
EVENTS_DIR="${SCRIPT_DIR}/.github/act/events"

if [ "${#}" -lt 1 ]; then
  cat >&2 << 'EOF'
Usage: ./run-act.sh <workflow> [<act-flags...>]

Available workflows:
  validate-workflows          push                → validate-workflows.yml
  build                       push                → build.yml
  error-prone-compat          push                → error-prone-compat.yml
  sonarcloud                  push                → sonarcloud.yml
  validate-review-checklist   push                → validate-review-checklist.yml
  pitest-analyze              pull_request        → pitest-analyze-pr.yml
  pitest-update               workflow_run        → pitest-update-pr.yml
  reviewdog                   pull_request        → reviewdog.yml
  suggest-commit-message      pull_request        → suggest-commit-message.yml
  assign-milestone            pull_request_target → assign-milestone.yml
  integration-tests           issue_comment       → integration-tests.yml
  codeql                      pull_request        → codeql.yml
  openssf-scorecard           pull_request        → openssf-scorecard.yml
  deploy-website              pull_request        → deploy-website.yml
  default-branch-health-gate  pull_request        → default-branch-health-gate.yml
EOF
  exit 1
fi

workflow="${1}"
shift

case "${workflow}" in
  validate-workflows)
    act push \
      -W '.github/workflows/validate-workflows.yml' \
      --eventpath "${EVENTS_DIR}/push.json" \
      "${@}"
    ;;
  build)
    act push \
      -W '.github/workflows/build.yml' \
      --eventpath "${EVENTS_DIR}/push.json" \
      --matrix os:ubuntu-24.04 \
      "${@}"
    ;;
  error-prone-compat)
    act push \
      -W '.github/workflows/error-prone-compat.yml' \
      --eventpath "${EVENTS_DIR}/push.json" \
      "${@}"
    ;;
  sonarcloud)
    act push \
      -W '.github/workflows/sonarcloud.yml' \
      --eventpath "${EVENTS_DIR}/push.json" \
      "${@}"
    ;;
  validate-review-checklist)
    act push \
      -W '.github/workflows/validate-review-checklist.yml' \
      --eventpath "${EVENTS_DIR}/push.json" \
      "${@}"
    ;;
  pitest-analyze)
    act pull_request \
      -W '.github/workflows/pitest-analyze-pr.yml' \
      --eventpath "${EVENTS_DIR}/pull_request.json" \
      "${@}"
    ;;
  pitest-update)
    act workflow_run \
      -W '.github/workflows/pitest-update-pr.yml' \
      --eventpath "${EVENTS_DIR}/workflow_run.completed.json" \
      "${@}"
    ;;
  reviewdog)
    act pull_request \
      -W '.github/workflows/reviewdog.yml' \
      --eventpath "${EVENTS_DIR}/pull_request.json" \
      "${@}"
    ;;
  suggest-commit-message)
    act pull_request \
      -W '.github/workflows/suggest-commit-message.yml' \
      --eventpath "${EVENTS_DIR}/pull_request.json" \
      "${@}"
    ;;
  assign-milestone)
    act pull_request_target \
      -W '.github/workflows/assign-milestone.yml' \
      --eventpath "${EVENTS_DIR}/pull_request_target.labeled.json" \
      "${@}"
    ;;
  integration-tests)
    act issue_comment \
      -W '.github/workflows/integration-tests.yml' \
      --eventpath "${EVENTS_DIR}/issue_comment.created.json" \
      "${@}"
    ;;
  codeql)
    act pull_request \
      -W '.github/workflows/codeql.yml' \
      --eventpath "${EVENTS_DIR}/pull_request.json" \
      "${@}"
    ;;
  openssf-scorecard)
    act pull_request \
      -W '.github/workflows/openssf-scorecard.yml' \
      --eventpath "${EVENTS_DIR}/pull_request.json" \
      "${@}"
    ;;
  deploy-website)
    act pull_request \
      -W '.github/workflows/deploy-website.yml' \
      --eventpath "${EVENTS_DIR}/pull_request.json" \
      "${@}"
    ;;
  default-branch-health-gate)
    act pull_request \
      -W '.github/workflows/default-branch-health-gate.yml' \
      --eventpath "${EVENTS_DIR}/pull_request.json" \
      "${@}"
    ;;
  *)
    printf 'Unknown workflow: %s\n' "${workflow}" >&2
    exit 1
    ;;
esac
