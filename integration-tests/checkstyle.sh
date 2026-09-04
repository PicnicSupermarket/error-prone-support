#!/usr/bin/env bash

set -e -u -o pipefail

test_name="$(basename "${0}" .sh)"
project='checkstyle'
repository='https://github.com/checkstyle/checkstyle.git'
revision='checkstyle-14.0.0'
# The `no-validations` profile disables the code quality plugins using which
# this project validates its own sources; those sources are here reformatted
# using Google Java Format, which such validation does not tolerate. The
# profile also disables compilation of the test input resources, which is
# undesirable here.
additional_build_flags='-Perror-prone-compile,error-prone-test-compile,no-validations -Dmaven.compiler.failOnError=true -Dcheckstyle.skipCompileInputResources=false'
additional_source_directories='${project.basedir}${file.separator}src${file.separator}it${file.separator}java,${project.basedir}${file.separator}src${file.separator}xdocs-examples${file.separator}java'
shared_error_prone_flags='-XepExcludedPaths:(\Q${project.basedir}${file.separator}src${file.separator}\E(it|test|xdocs-examples)\Q${file.separator}resources\E|\Q${project.build.directory}${file.separator}\E).*'
patch_error_prone_flags=''
validation_error_prone_flags=''
# The `no-validations` profile enabled above also skips the tests.
validation_build_flags='-DskipTests=false'

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
