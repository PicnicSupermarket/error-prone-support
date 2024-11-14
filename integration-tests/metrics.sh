#!/usr/bin/env bash

set -e -u -o pipefail

test_name="$(basename "${0}" .sh)"
project='metrics'
repository='https://github.com/dropwizard/metrics.git'
#revision='v4.2.28'
revision='v5.0.0-rc22'
additional_build_flags=''
additional_source_directories='${project.basedir}${file.separator}src${file.separator}it${file.separator}java,${project.basedir}${file.separator}src${file.separator}xdocs-examples${file.separator}java'
# XXX: We exclude the `CollectorMutability` and the `Immutable*|Preconditions*|StringRules.StringIsNullOrEmpty` Refaster rules
# as they introduce changes that expect Guava to be on the classpath.
#patch_error_prone_flags="-Xep:CollectorMutability:OFF -XepOpt:Refaster:NamePattern=^(?!.*File).*"
patch_error_prone_flags="-Xep:CollectorMutability:OFF -XepOpt:Refaster:NamePattern=^(?!FileRules\$).*"
validation_error_prone_flags=''
validation_build_flags=''

if [ "${#}" -gt 2 ] || ([ "${#}" = 2 ] && [ "${1:---sync}" != '--sync' ]); then
  echo "Usage: ${0} [--sync] [<report_directory>]"
  exit 1
fi

"$(dirname "${0}")"/run-integration-test.sh \
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
