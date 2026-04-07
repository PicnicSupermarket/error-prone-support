#!/usr/bin/env bash

# Launches a Claude Code session with `--dangerously-skip-permissions` inside a
# Development Container. Each session runs in its own Git worktree, isolated
# from the host filesystem.
#
# Usage:
#   ./claude-sandbox.sh <branch-or-worktree-path> [claude-args...]
#
# The first argument is interpreted as follows:
# - Existing directory: used as the worktree path.
# - Branch with an existing worktree: that worktree's path is used.
# - Otherwise: a worktree is created at `${WORKTREE_BASE}/<name>` (default base:
#   ~/workspace/worktrees/error-prone-support/), where slashes in the name are
#   replaced with dashes.

set -e -u -o pipefail

if [ "${#}" -lt 1 ]; then
  echo "Usage: ${0} <branch-or-worktree-path> [claude-args...]" >&2
  exit 1
fi

for cmd in devcontainer docker flock git shasum; do
  if ! command -v "${cmd}" >/dev/null 2>&1; then
    echo "This script requires \`${cmd}\`; please install it." >&2
    exit 1
  fi
done

REPO_ROOT="$(git -C "$(dirname "${0}")" rev-parse --show-toplevel)"
REPO_NAME="$(basename "${REPO_ROOT}")"
WORKTREE_BASE="${WORKTREE_BASE:-${REPO_ROOT}/../${REPO_NAME}-worktrees}"
GIT_DIR="$(git -C "${REPO_ROOT}" rev-parse --absolute-git-dir)"
DEVCONTAINER_CONFIG="${REPO_ROOT}/.devcontainer/devcontainer.json"

TARGET="${1}"
shift

# Determine the workspace folder:
# 1. Existing directory: use as-is.
# 2. Branch with an attached worktree: use that worktree's path.
# 3. Otherwise: derive the path as ${WORKTREE_BASE}/${TARGET} with slashes
#    replaced by dashes, and create the worktree if it does not yet exist.
if [ -d "${TARGET}" ]; then
  WORKSPACE_FOLDER="$(cd "${TARGET}" && pwd)"
else
  WORKSPACE_FOLDER="$(git -C "${REPO_ROOT}" worktree list --porcelain \
    | awk -v branch="refs/heads/${TARGET}" '
        /^worktree / { wt = substr($0, 10) }
        $0 == "branch " branch { print wt; exit }
      ')"

  if [ -z "${WORKSPACE_FOLDER}" ]; then
    WORKSPACE_FOLDER="${WORKTREE_BASE}/${TARGET//\//-}"
    if [ ! -d "${WORKSPACE_FOLDER}" ]; then
      echo "Creating worktree at ${WORKSPACE_FOLDER}"
      if git -C "${REPO_ROOT}" rev-parse --verify --quiet "refs/heads/${TARGET}" \
          >/dev/null 2>&1; then
        git -C "${REPO_ROOT}" worktree add "${WORKSPACE_FOLDER}" "${TARGET}"
      else
        git -C "${REPO_ROOT}" worktree add -b "${TARGET}" "${WORKSPACE_FOLDER}"
      fi
    fi
  fi
fi

# We want to keep locally-installed Maven artifacts (SNAPSHOTs) separate from
# remotely-cached ones, so that `mvn install` invocations in different
# environments (host, container) don't overwrite each other's builds. The
# Devcontainer configures `MAVEN_OPTS` to achieve this, but it does require an
# installation prefix that is unique to the workspace; here we generate that
# prefix.
export WORKSPACE_ID="$(echo "${WORKSPACE_FOLDER}" | shasum -a 256 | cut -d ' ' -f 1)"

# Use a lock file to track concurrent sessions for this workspace. A shared
# lock is held while the session is active; on exit, an exclusive lock is
# attempted: if it succeeds, this was the last session and the container is
# shut down.
exec {SESSION_LOCK}>"/tmp/claude-sandbox-${WORKSPACE_ID}.session.lock"
flock -s "${SESSION_LOCK}"

function tear_down() {
  if flock -x -n "${SESSION_LOCK}" 2>/dev/null; then
    # XXX: Use `devcontainer down` once available; see
    # https://github.com/devcontainers/cli/issues/386.
    docker ps -q -f "label=devcontainer.local_folder=${WORKSPACE_FOLDER}" \
        | xargs docker rm -f
  fi
}
trap tear_down INT TERM HUP EXIT

# Start the devcontainer based on the configuration in the main repo.
#
# This operation is performed under an exclusive lock, so that per workspace
# only a single container is created.
#
# Worktrees have a `.git` file specifying the absolute path to the main repo's
# `.git/worktrees/<name>` directory. For this to resolve inside the container,
# we mount the main `.git` directory at its real host path.
flock -x "/tmp/claude-sandbox-${WORKSPACE_ID}.startup.lock" \
  devcontainer up \
    --workspace-folder "${WORKSPACE_FOLDER}" \
    --config "${DEVCONTAINER_CONFIG}" \
    --mount "type=bind,source=${GIT_DIR},target=${GIT_DIR}"

# Start Claude Code inside the container.
devcontainer exec \
  --workspace-folder "${WORKSPACE_FOLDER}" \
  --config "${DEVCONTAINER_CONFIG}" \
  claude --dangerously-skip-permissions "${@}"
