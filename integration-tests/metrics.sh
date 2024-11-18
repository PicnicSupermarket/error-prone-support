#!/usr/bin/env bash

set -e -u -o pipefail

test_name="$(basename "${0}" .sh)"
project='metrics'
repository='https://github.com/dropwizard/metrics.git'
revision='v5.0.0-rc22'
additional_build_flags=''
additional_source_directories=''
shared_error_prone_flags='-XepExcludedPaths:.*/target/generated-sources/.*'
# XXX: These don't work...
patch_error_prone_flags='-Xep:CollectorMutability:OFF -XepOpt:Refaster:NamePattern=^(?!FileRules\$).*'
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
  "${shared_error_prone_flags}" \
  "${patch_error_prone_flags}" \
  "${validation_error_prone_flags}" \
  "${validation_build_flags}" \
  $@
