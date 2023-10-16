#!/usr/bin/env bash

set -e -u -o pipefail

project=checkstyle
revision=checkstyle-10.9.3

if [ "${#}" -gt 1 ] || [[ ${1:---sync} != '--sync' ]]; then
  echo "Usage: ${0} [--sync]"
  exit 1
fi
do_sync="${1:-}"

error_prone_support_version="$(
 mvn -f .. help:evaluate -Dexpression=project.version -q -DforceStdout
)"

error_prone_shared_flags='-XepExcludedPaths:(\Q${project.basedir}${file.separator}src${file.separator}\E(it|test)\Q${file.separator}resources\E|\Q${project.build.directory}${file.separator}\E).*'

error_prone_patch_flags="${error_prone_shared_flags} -XepPatchLocation:IN_PLACE -XepPatchChecks:$(
  find .. -path "*/META-INF/services/com.google.errorprone.bugpatterns.BugChecker" \
    | xargs grep -hoP '[^.]+$' \
    | paste -s -d ','
)"

error_prone_validation_flags="${error_prone_shared_flags} -XepDisableAllChecks $(
  find .. -path "*/META-INF/services/com.google.errorprone.bugpatterns.BugChecker" \
    | xargs grep -hoP '[^.]+$' \
    | sed -r 's,(.*),-Xep:\1:WARN,' \
    | paste -s -d ' '
)"

validation_log_file="$(mktemp)"
trap 'rm -rf -- "${validation_log_file}"' INT TERM HUP EXIT

echo "Error Prone Support version: ${error_prone_support_version}"
echo "Error Prone patch flags: ${error_prone_patch_flags}"
echo "Error Prone validation flags: ${error_prone_validation_flags}"

pushd "${project}"

git checkout -f "${revision}"
git apply < "../${project}-${revision}-init.patch"
git commit -m 'dependency: Introduce Error Prone Support' .

mvn com.spotify.fmt:fmt-maven-plugin:2.19:format \
  -DadditionalSourceDirectories='${project.basedir}${file.separator}src${file.separator}it${file.separator}java'
git commit -m 'minor: Reformat using Google Java Format' .

function apply_patch() {
  local current_diff="${1}"

  mvn clean package com.spotify.fmt:fmt-maven-plugin:2.19:format \
    -DadditionalSourceDirectories='${project.basedir}${file.separator}src${file.separator}it${file.separator}java' \
    -Perror-prone-compile,error-prone-test-compile \
    -Derror-prone.flags="${error_prone_patch_flags}" \
    -Derror-prone-support.version="${error_prone_support_version}" \
    -DskipTests

  local new_diff="$(git diff | shasum --algorithm 256)"

  if [ "${current_diff}" != "${new_diff}" ]; then
    apply_patch "${new_diff}"
  fi
}

apply_patch "$(git diff | shasum --algorithm 256)"

# disable sync mechanism, we just want to upload the changes
baseline_patch="../${project}-${revision}-expected-changes.patch"
# if [ -n "${do_sync}" ]; then
echo 'Saving changes...'
git diff > "${baseline_patch}"
# else
#   echo 'Inspecting changes...'
#   if ! diff -u "${baseline_patch}" <(git diff); then
#     echo 'There are unexpected changes.'
#     exit 1
#   fi
# fi

# Validate the results.
#
# - The `metadataFilesGenerationAllFiles` test is skipped because is makes line
#   number assertions that will fail when the code is formatted or patched.
# - The `allCheckSectionJavaDocs` test is skipped because is validates that
#   Javadoc has certain closing tags that are removed by Google Java Format.
# XXX: Figure out why the `validateCliDocSections` test fails.
echo "Validation file: ${validation_log_file}"
mvn clean package \
    -Perror-prone-compile,error-prone-test-compile \
    -Derror-prone.flags="${error_prone_validation_flags}" \
    -Derror-prone-support.version="${error_prone_support_version}" \
    -Dmaven.compiler.showWarnings \
  | tee "${validation_log_file}"
echo "Finished validation run!"

baseline_warnings="../${project}-${revision}-expected-warnings.txt"
# note: added '*' in the final grep, required in order to get matches in GNU grep 3.11
# disable sync mechanism, we just want to upload the expected warnings
generated_warnings="$(grep -oP "(?<=^\\Q[WARNING] ${PWD}/\\E).*" "${validation_log_file}" | grep -P '\]*\[')"
# if [ -n "${do_sync}" ]; then
echo 'Saving emitted warnings...'
echo "${generated_warnings}" > "${baseline_warnings}"
# else
#   echo 'Inspecting emitted warnings...'
#   if ! diff -u "${baseline_warnings}" <(echo "${generated_warnings}"); then
#     echo 'Diagnostics output changed.'
#     exit 1
#   fi
# fi
