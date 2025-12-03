#!/usr/bin/env bash

set -e -u -o pipefail

test_name="$(basename "${0}" .sh)"
project='checkstyle'
repository='https://github.com/checkstyle/checkstyle.git'
revision='checkstyle-12.0.1'
additional_build_flags='-Djava.version=21 -Perror-prone-compile,error-prone-test-compile -Dmaven.compiler.failOnError=true'
additional_source_directories='${project.basedir}${file.separator}src${file.separator}it${file.separator}java,${project.basedir}${file.separator}src${file.separator}xdocs-examples${file.separator}java'
shared_error_prone_flags='-XepExcludedPaths:(\Q${project.basedir}${file.separator}src${file.separator}\E(it|test|xdocs-examples)\Q${file.separator}resources\E|\Q${project.build.directory}${file.separator}\E).*'
patch_error_prone_flags=''
validation_error_prone_flags=''
# Validation skips various tests because they validate that Javadoc has certain
# closing tags that are removed by Google Java Format.
validation_build_flags='-Dtest=!AllChecksTest#allCheckstyleModulesHaveXdocDocumentation,!XdocsCategoryIndexTest#allChecksListedInCategoryIndexAndDescriptionMatches,!XdocsJavaDocsTest#allCheckSectionJavaDocs,!XdocsMobileWrapperTest#allCheckSectionMobileWrapper,!XdocsPagesTest#allCheckSections,!XdocsPagesTest#allModulesPageInSyncWithModuleSummaries,!XdocsPagesTest#allSubSections,!XdocsPagesTest#allXmlExamples'

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
  "${shared_error_prone_flags}" \
  "${patch_error_prone_flags}" \
  "${validation_error_prone_flags}" \
  "${validation_build_flags}" \
  $@
