#!/usr/bin/env bash

set -e -u -o pipefail

test_name="$(basename "${0}" .sh)"
project='checkstyle'
repository='https://github.com/checkstyle/checkstyle.git'
revision='checkstyle-14.0.0'
additional_build_flags='-Perror-prone-compile,error-prone-test-compile -Dmaven.compiler.failOnError=true'
additional_source_directories='${project.basedir}${file.separator}src${file.separator}it${file.separator}java,${project.basedir}${file.separator}src${file.separator}xdocs-examples${file.separator}java'
shared_error_prone_flags='-XepExcludedPaths:(\Q${project.basedir}${file.separator}src${file.separator}\E(it|test|xdocs-examples)\Q${file.separator}resources\E|\Q${project.build.directory}${file.separator}\E).*'
patch_error_prone_flags=''
validation_error_prone_flags=''
# Validation skips various tests:
# - Most of them validate that Javadoc has certain closing tags that are
#   removed by Google Java Format, or compare against output derived from such
#   Javadoc.
# - `MainTest#existingTargetFileWithOneErrorAgainstSunCheck` reports an extra
#   violation, as `sun_checks.xml` also observes the header file property that
#   the `com/sun` tests require.
# - `XdocsExampleFileTest` reflectively instantiates the example test classes,
#   which `JUnitClassModifiers` makes package-private.
# - `SuppressWarningsHolderExamplesTest#example1` observes a check alias
#   registered by another example's configuration, as `JUnitMethodDeclaration`
#   renames these tests and thus changes the order in which they run.
validation_build_flags='-Dtest=!AllChecksTest#allCheckstyleModulesHaveXdocDocumentation,!CheckstyleAntTaskTest#sarifOutput,!MainTest#existingTargetFileWithOneErrorAgainstSunCheck,!SarifLoggerTest,!SuppressWarningsHolderExamplesTest#example1,!XdocsCategoryIndexTest#allChecksListedInCategoryIndexAndDescriptionMatches,!XdocsExampleFileTest#allExampleFilesHaveCorrespondingTestMethods,!XdocsExampleFileTest#allModuleExamplesAreBehaviorallyUnique,!XdocsExamplesAstConsistencyTest#everyPropertyHasAnExample,!XdocsExamplesAstConsistencyTest#exampleCountMatchesPropertyCount,!XdocsJavaDocsTest#allCheckSectionJavaDocs,!XdocsMobileWrapperTest#allCheckSectionMobileWrapper,!XdocsPagesTest#allCheckSections,!XdocsPagesTest#allModulesPageInSyncWithModuleSummaries,!XdocsPagesTest#allSubSections,!XdocsPagesTest#allXmlExamples'

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
  "${@}"
