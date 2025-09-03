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

if [ "${#}" -lt 10 ] || [ "${#}" -gt 12 ] || ([ "${#}" = 12 ] && [ "${11:---sync}" != '--sync' ]); then
  >&2 echo "Usage: $(basename "${0}") <test_name> <project> <repository> <revision> <additional_build_flags> <additional_source_directories> <shared_error_prone_flags> <patch_error_prone_flags> <validation_error_prone_flags> <validation_build_flags> [--sync] [<report_directory>]"
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
do_sync="$([ "${#}" = 10 ] || [ "${11:-}" != '--sync' ] || echo 1)"
report_directory="$([ "${#}" = 10 ] || ([ -z "${do_sync}" ] && echo "${11}") || ([ "${#}" = 11 ] || echo "${12}"))"

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
    echo "Unsupported distribution $(uname -s) for this script."
    exit 1
    ;;
esac

shared_build_flags="
  -Derror-prone.version=$(
    mvn -f "${error_prone_support_root}" help:evaluate -Dexpression=version.error-prone -q -DforceStdout
  )
  -Derror-prone-support.version=$(
    mvn -f "${error_prone_support_root}" help:evaluate -Dexpression=project.version -q -DforceStdout
  )
  -DadditionalSourceDirectories=${additional_source_directories}
  ${additional_build_flags}
  "

format_goal='com.spotify.fmt:fmt-maven-plugin:2.27:format'

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
git clean -fdx
git apply < "${initial_patch}"
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

# Apply Error Prone Support-suggested changes until a fixed point is reached.
function apply_patch() {
  local extra_build_args="${1}"

  (
    set -x \
      && mvn ${shared_build_flags} ${extra_build_args} \
           package "${format_goal}" \
           -Derror-prone.configuration-args="${error_prone_patch_flags}" \
           -DskipTests
  )

  if ! git diff --exit-code; then
    git commit -m 'minor: Apply patches' .

    # Changes were applied, so another compilation round may apply yet more
    # changes. For performance reasons we perform incremental compilation,
    # enabled using a misleading flag. (See
    # https://issues.apache.org/jira/browse/MCOMPILER-209 for details.)
    apply_patch '-Dmaven.compiler.useIncrementalCompilation=false'
  elif [ "${extra_build_args}" != 'clean' ]; then
    # No changes were applied. We'll attempt one more round in which all files
    # are recompiled, because there are cases in which violations are missed
    # during incremental compilation.
    apply_patch 'clean'
  fi
}
apply_patch ''

# Run one more full build and log the output.
#
# By also running the tests, we validate that the (majority of) applied changes
# are behavior preserving.
validation_build_log="${report_directory}/${test_name}-validation-build-log.txt"
(
  set -x \
    && mvn ${shared_build_flags} \
         clean package \
         -Derror-prone.configuration-args="${error_prone_validation_flags}" \
         ${validation_build_flags}
) | tee "${validation_build_log}" || failure=1

# Collect the applied changes.
expected_changes="${integration_test_root}/${test_name}-expected-changes.patch"
actual_changes="${report_directory}/${test_name}-changes.patch"
(git diff "${diff_base}"..HEAD | "${grep_command}" -vP '^(diff|index)' || true) > "${actual_changes}"

# Collect the warnings reported by Error Prone Support checks.
expected_warnings="${integration_test_root}/${test_name}-expected-warnings.txt"
actual_warnings="${report_directory}/${test_name}-validation-build-warnings.txt"
("${grep_command}" -oP "(?<=^\\Q[WARNING] ${PWD}/\\E).*" "${validation_build_log}" | "${grep_command}" -P '\] \[' || true) | LC_ALL=C sort > "${actual_warnings}"

# Persist or validate the applied changes and reported warnings.
if [ -n "${do_sync}" ]; then
  echo 'Saving changes...'
  cp "${actual_changes}" "${expected_changes}"
  cp "${actual_warnings}" "${expected_warnings}"
else
  echo 'Inspecting changes...'
  # XXX: This "diff of diffs" also contains vacuous sections, introduced due to
  # line offset differences. Try to omit those from the final output.
  if ! diff -u "${expected_changes}" "${actual_changes}"; then
    echo 'There are unexpected changes. Inspect the preceding output for details.'
    failure=1
  fi
  echo 'Inspecting emitted warnings...'
  if ! diff -u "${expected_warnings}" "${actual_warnings}"; then
    echo 'Diagnostics output changed. Inspect the preceding output for details.'
    failure=1
  fi
fi

if [ -n "${failure:-}" ]; then
  exit 1
fi
