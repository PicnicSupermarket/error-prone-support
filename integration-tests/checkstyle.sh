#!/usr/bin/env bash

set -e -u -o pipefail

test_name="$(basename "${0}" .sh)"
project='checkstyle'
repository='https://github.com/checkstyle/checkstyle.git'
revision='checkstyle-10.14.0'
# XXX: Configure Renovate to manage the AssertJ version declared here.
additional_build_flags='-Dassertj.version=3.24.2'
additional_source_directories='${project.basedir}${file.separator}src${file.separator}it${file.separator}java,${project.basedir}${file.separator}src${file.separator}xdocs-examples${file.separator}java'
patch_error_prone_flags=''
validation_error_prone_flags=''
# Validation skips some tests:
# - The `metadataFilesGenerationAllFiles` test is skipped because it makes line
#   number assertions that will fail when the code is formatted or patched.
# - The `allCheckSectionJavaDocs` test is skipped because it validates that
#   Javadoc has certain closing tags that are removed by Google Java Format.
validation_build_flags='-Dtest=!MetadataGeneratorUtilTest#metadataFilesGenerationAllFiles,!XdocsJavaDocsTest#allCheckSectionJavaDocs'

if [ "${#}" -gt 2 ] || ([ "${#}" = 2 ] && [ "${1:---sync}" != '--sync' ]); then
  >&2 echo "Usage: ${0} [--sync] [<report_directory>]"
  exit 1
fi

"$(dirname "${0}")/run-integration-test.sh" \
  "${test_name}" \
  "${project}" \
  "${repository}" \
  "${revision}" \
  "${additional_build_flags}" \
  "${additional_source_directories}" \
  "${patch_error_prone_flags}" \
  "${validation_error_prone_flags}" \
  "${validation_build_flags}" \
  $@
