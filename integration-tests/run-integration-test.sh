#!/usr/bin/env bash

# Integration test framework for Maven builds.
#
# This script is not meant to be invoked manually. Instead it should be invoked
# through one of the top-level integration test scripts, such as
# `checkstyle.sh`.

set -e -u -o pipefail

# Make sure that callers with a customized `MAVEN_ARGS` environment variable do
# not influence the test result.
export MAVEN_ARGS=

integration_test_root="$(cd "$(dirname -- "${0}")" && pwd)"
error_prone_support_root="${integration_test_root}/.."
repos_root="${integration_test_root}/.repos"

if [ "${#}" -lt 10 ]; then
  echo "Usage: $(basename "${0}") <test_name> <project> <repository> <revision> <additional_build_flags> <additional_source_directories> <shared_error_prone_flags> <patch_error_prone_flags> <validation_error_prone_flags> <validation_build_flags> [<option>...]" >&2
  exit 1
fi

test_name="${1}"
project="${2}"
repository="${3}"
revision="${4}"
additional_build_flags="${5}"
additional_source_directories="${6}"
shared_error_prone_flags="${7}"
patch_error_prone_flags="${8}"
validation_error_prone_flags="${9}"
validation_build_flags="${10}"
shift 10

# The remaining options are forwarded verbatim by the top-level integration
# test scripts, so they are reported in terms of those.
function usage() {
  echo "Usage: ${test_name}.sh [--phase={patch,validate}] [--sync] [<report_directory>]" >&2
  exit 1
}

# The `patch` phase applies Error Prone Support's suggested changes and
# validates the resulting diff, while the `validate` phase replays those
# changes, builds and tests the resulting code, and validates the emitted
# diagnostics. Selecting a single phase allows the two to be distributed over
# separate CI jobs. By default both phases are executed in sequence.
phase='all'
do_sync=''
report_directory=''
while [ "${#}" -gt 0 ]; do
  case "${1}" in
    --phase=patch | --phase=validate)
      phase="${1#--phase=}"
      ;;
    --sync)
      do_sync=1
      ;;
    -*)
      usage
      ;;
    *)
      [ -z "${report_directory}" ] || usage
      report_directory="${1}"
      ;;
  esac
  shift
done

if [ "${phase}" != 'all' ]; then
  # Each phase refreshes only the expected output it produces, so syncing would
  # leave the remaining files stale.
  if [ -n "${do_sync}" ]; then
    echo 'Cannot sync expected output unless both phases are executed.' >&2
    exit 1
  fi

  # The `patch` phase communicates its result to the `validate` phase through
  # the report directory, so a transient one will not do.
  if [ -z "${report_directory}" ]; then
    echo 'A report directory must be specified when executing a single phase.' >&2
    exit 1
  fi
fi

if [ -n "${report_directory}" ]; then
  mkdir -p "${report_directory}"
else
  report_directory="$(mktemp -d)"
  trap 'rm -rf -- "${report_directory}"' INT TERM HUP EXIT
fi

case "$(uname -s)" in
  Linux*)
    grep_command=grep
    sed_command=sed
    ;;
  Darwin*)
    grep_command=ggrep
    sed_command=gsed
    ;;
  *)
    echo "Unsupported distribution $(uname -s) for this script." >&2
    exit 1
    ;;
esac

shared_build_flags="
  -T1C
  -Derror-prone.version=$(
    mvn -f "${error_prone_support_root}" help:evaluate -Dexpression=version.error-prone -q -DforceStdout
  )
  -Derror-prone-support.version=$(
    mvn -f "${error_prone_support_root}" help:evaluate -Dexpression=project.version -q -DforceStdout
  )
  -DadditionalSourceDirectories=${additional_source_directories}
  ${additional_build_flags}
  "

format_goal='com.spotify.fmt:fmt-maven-plugin:2.29:format'

error_prone_patch_flags="${shared_error_prone_flags} -XepPatchLocation:IN_PLACE -XepPatchChecks:$(
   find "${error_prone_support_root}" \
      -path "*/META-INF/services/com.google.errorprone.bugpatterns.BugChecker" \
      -not -path "*/error-prone-experimental/*" \
      -not -path "*/error-prone-guidelines/*" \
      -print0 \
    | xargs -0 "${grep_command}" -hoP '[^.]+$' \
    | paste -s -d ',' -
) ${patch_error_prone_flags}"

error_prone_validation_flags="${shared_error_prone_flags} -XepDisableAllChecks $(
   find "${error_prone_support_root}" \
      -path "*/META-INF/services/com.google.errorprone.bugpatterns.BugChecker" \
      -not -path "*/error-prone-experimental/*" \
      -not -path "*/error-prone-guidelines/*" \
      -print0 \
    | xargs -0 "${grep_command}" -hoP '[^.]+$' \
    | "${sed_command}" -r 's,(.*),-Xep:\1:WARN,' \
    | paste -s -d ' ' -
) ${validation_error_prone_flags}"

echo "Shared build flags: ${shared_build_flags}"
echo "Error Prone patch flags: ${error_prone_patch_flags}"
echo "Error Prone validation flags: ${error_prone_validation_flags}"

mkdir -p "${repos_root}"

# Make sure that the targeted tag of the project's Git repository is checked
# out.
project_root="${repos_root}/${project}"
if [ ! -d "${project_root}" ]; then
  # The repository has not yet been cloned; create a shallow clone.
  git clone --branch "${revision}" --depth 1 "${repository}" "${project_root}"
else
  # The repository does already appear to exist. Try to check out the requested
  # tag if possible, and fetch it otherwise.
  #
  # Under certain circumstances this does not cause the relevant tag to be
  # created, so if necessary we manually create it.
  git -C "${project_root}" checkout --force "${revision}" 2>/dev/null \
    || (
         git -C "${project_root}" fetch --depth 1 "${repository}" "${revision}" \
           && git -C "${project_root}" checkout --force FETCH_HEAD \
           && (git -C "${project_root}" tag "${revision}" || true)
       )
fi

pushd "${project_root}"

# Make sure that Git is sufficiently configured to enable committing to the
# project's Git repository.
git config user.email || git config user.email 'integration-test@example.com'
git config user.name || git config user.name 'Integration Test'

# Prepare the code for analysis by applying the minimal set of changes required
# to run Error Prone with Error Prone Support.
initial_patch="${integration_test_root}/${test_name}-init.patch"
git clean -ffdx
git apply --index < "${initial_patch}"
git commit -m 'dependency: Introduce Error Prone Support' .
if [ -n "${do_sync}" ]; then
  # The initial patch applied successfully, but if it was created against a
  # different version, then offsets may have changed. Here we update the patch
  # to exactly match the new state.
  git diff HEAD~1 | "${grep_command}" -vP '^(diff|index)' > "${initial_patch}"
fi

# Format the patched code using the same method by which it will be formatted
# after each compilation round. This initial formatting operation ensures that
# subsequent modifications can be rendered in a clean manner.
mvn ${shared_build_flags} "${format_goal}"
git commit -m 'minor: Reformat using Google Java Format' .
diff_base="$(git rev-parse HEAD)"

# Files containing the changes applied by the `patch` phase, and the tree they
# yield, respectively. Tracking the latter enables guarding against the two
# phases analyzing different code, including for reasons the patch does not
# itself describe: for example, `git apply` silently ignores file mode changes
# not expressed in the `diff` headers stripped below.
actual_changes="${report_directory}/${test_name}-changes.patch"
patched_tree="${report_directory}/${test_name}-patched-tree.txt"

# Apply Error Prone Support-suggested changes until a fixed point is reached.
# Every round recompiles all sources, as there are cases in which violations
# are missed during incremental compilation. Doing so is generally also faster:
# a round that recompiles only the files changed by its predecessor is cheaper,
# but often more rounds are then required to reach the fixed point.
function apply_patch() {
  (
    set -x \
      && mvn ${shared_build_flags} \
           clean package "${format_goal}" \
           -Derror-prone.configuration-args="${error_prone_patch_flags}" \
           -DskipTests
  )

  if ! git diff --exit-code; then
    git commit -m 'minor: Apply patches' .

    # Changes were applied, so another compilation round may apply yet more
    # changes.
    apply_patch
  fi
}
if [ "${phase}" != 'validate' ]; then
  apply_patch
  git rev-parse 'HEAD^{tree}' > "${patched_tree}"

  # Collect the applied changes.
  expected_changes="${integration_test_root}/${test_name}-expected-changes.patch"
  git diff "${diff_base}"..HEAD | ("${grep_command}" -vP '^(diff|index)' || true) > "${actual_changes}"

  # Persist or validate the applied changes.
  if [ -n "${do_sync}" ]; then
    echo 'Saving changes...'
    cp "${actual_changes}" "${expected_changes}"
  else
    echo 'Inspecting changes...'
    # XXX: This "diff of diffs" also contains vacuous sections, introduced due
    # to line offset differences. Try to omit those from the final output.
    if ! diff -u "${expected_changes}" "${actual_changes}"; then
      echo 'There are unexpected changes. Inspect the preceding output for details.'
      failure=1
    fi
  fi
else
  if [ ! -f "${actual_changes}" ] || [ ! -f "${patched_tree}" ]; then
    echo "Output of the 'patch' phase not found in '${report_directory}'." >&2
    exit 1
  fi

  # Replay the changes applied by the `patch` phase, so that the validation
  # build analyzes the same code. An empty patch means that Error Prone Support
  # suggested no changes at all.
  if [ -s "${actual_changes}" ]; then
    if ! git apply --index "${actual_changes}"; then
      # The two phases run on separate machines, so say which two things
      # disagree; the raw `git apply` output does not.
      echo "Cannot replay the changes produced by the 'patch' phase." >&2
      exit 1
    fi
    git commit -m 'minor: Apply patches' .
  fi

  if [ "$(git rev-parse 'HEAD^{tree}')" != "$(cat "${patched_tree}")" ]; then
    echo "Replayed code differs from that produced by the 'patch' phase." >&2
    exit 1
  fi
fi

if [ "${phase}" != 'patch' ]; then
  # Run a full build and log the output.
  #
  # By also running the tests, we validate that the (majority of) applied
  # changes are behavior preserving.
  validation_build_log="${report_directory}/${test_name}-validation-build-log.txt"
  (
    set -x \
      && mvn ${shared_build_flags} \
           clean package \
           -Derror-prone.configuration-args="${error_prone_validation_flags}" \
           ${validation_build_flags}
  ) | tee "${validation_build_log}" || failure=1

  # Collect the warnings reported by Error Prone Support checks.
  expected_warnings="${integration_test_root}/${test_name}-expected-warnings.txt"
  actual_warnings="${report_directory}/${test_name}-validation-build-warnings.txt"
  ("${grep_command}" -oP "(?<=^\\Q[WARNING] ${PWD}/\\E).*" "${validation_build_log}" | "${grep_command}" -P '\] \[' || true) | LC_ALL=C sort > "${actual_warnings}"

  # Persist or validate the reported warnings.
  if [ -n "${do_sync}" ]; then
    echo 'Saving warnings...'
    cp "${actual_warnings}" "${expected_warnings}"
  else
    echo 'Inspecting emitted warnings...'
    if ! diff -u "${expected_warnings}" "${actual_warnings}"; then
      echo 'Diagnostics output changed. Inspect the preceding output for details.'
      failure=1
    fi
  fi
fi

if [ -n "${failure:-}" ]; then
  exit 1
fi
