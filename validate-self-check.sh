#!/usr/bin/env bash

# Validates that the `self-check` profile applies this project's Error Prone
# checks to this project's own code.
#
# That property is not self-evident: the profile puts each module on its own
# annotation processor path, where Maven resolves the module to whatever output
# directory its artifact currently points at. A build step that leaves such a
# directory unpopulated silently withholds a module's checks from itself,
# without failing the build. This script detects that by introducing a
# violation of a check defined by the module under test, and asserting that the
# self-check build reports it.
#
# Requires that a prior build has installed the project in the local Maven
# repository, and that the file selected below is unmodified. The introduced
# violation is always reverted, including on failure.

set -e -u -o pipefail

# The module under test, and a check it defines that is trivially violated.
MODULE='error-prone-contrib'
CHECK='DirectReturn'
# The violation is introduced ahead of the final closing brace of the selected
# file. It is `private` and unused so that it cannot affect the module's API.
VIOLATION='
  @SuppressWarnings("unused")
  private static String selfCheckCanary() {
    String canary = "";
    return canary;
  }
'

PROJECT_ROOT="$(cd "$(dirname "${0}")" && pwd)"

function select_target() {
  # An arbitrary but deterministic unmodified file, so that this script never
  # discards uncommitted work.
  local file
  while read -r file; do
    if [ -z "$(git -C "${PROJECT_ROOT}" status --porcelain -- "${file}")" ]; then
      echo "${file}"
      return
    fi
  done < <(git -C "${PROJECT_ROOT}" ls-files "${MODULE}/src/main/java/**/bugpatterns/*.java" | sort)

  echo "Error: no unmodified ${MODULE} source file found." >&2
  exit 1
}

function introduce_violation() {
  local file="${PROJECT_ROOT}/${1}"
  local tmp="${file}.tmp"

  # Replace the file's last line, which closes the top-level type.
  sed '$d' "${file}" > "${tmp}"
  printf '%s}\n' "${VIOLATION}" >> "${tmp}"
  mv "${tmp}" "${file}"
}

target="$(select_target)"
trap 'git -C "${PROJECT_ROOT}" checkout -- "${target}"' INT TERM HUP EXIT

introduce_violation "${target}"

echo "Validating that \`${CHECK}\` is reported for \`${target}\`."
# A build failure is not by itself conclusive, so the build's outcome is
# derived from the reported diagnostics instead.
output="$(
  mvn clean compile \
    -f "${PROJECT_ROOT}/pom.xml" \
    -s "${PROJECT_ROOT}/settings.xml" \
    -pl "${MODULE}" \
    -Perror-prone-fork \
    -Pself-check \
    -Dverification.warn \
    2>&1
)" || true

if ! grep -qF "[${CHECK}]" <<<"${output}"; then
  echo "${output}" >&2
  echo >&2
  echo "Error: the self-check build did not report \`${CHECK}\` for the" \
    "violation introduced in \`${target}\`. Either ${MODULE} is not analysed" \
    "with its own Error Prone checks, or the build failed for an unrelated" \
    "reason; see the output above." >&2
  exit 1
fi

echo "OK: the \`self-check\` profile applies ${MODULE}'s checks to ${MODULE}."
