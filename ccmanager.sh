#!/usr/bin/env bash

# Launches CCManager with this project's Development Container setup, and
# instructing it to spawn Claude Code sessions
# `--dangerously-skip-permissions`.

set -e -u -o pipefail

PROJECT_ROOT="$(dirname "${0}")"

if ! command -v ccmanager >/dev/null 2>&1; then
  echo 'This script requires `ccmanager`; please install it.' >&2
  exit 1
fi

if ! command -v devcontainer >/dev/null 2>&1; then
  echo 'This script requires `devcontainer`; please install it.' >&2
  exit 1
fi

export CCMANAGER_CLAUDE_ARGS=--dangerously-skip-permissions

# XXX: This doesn't actually work, except when starting the container for the
# main repo. Figure out why.
ccmanager \
  --devc-up-command "devcontainer up --workspace-folder '${PROJECT_ROOT}'" \
  --devc-exec-command "devcontainer exec --workspace-folder '${PROJECT_ROOT}'"
