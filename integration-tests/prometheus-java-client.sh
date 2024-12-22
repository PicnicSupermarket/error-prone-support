#!/usr/bin/env bash

set -e -u -o pipefail

test_name="$(basename "${0}" .sh)"
project='prometheus-java-client'
repository='https://github.com/prometheus/client_java.git'
revision='v1.3.5'
additional_build_flags='-Djava.version=11 -Dwarnings='
additional_source_directories=''
shared_error_prone_flags=''
patch_error_prone_flags=''
validation_error_prone_flags=''
# XXX: Drop these flags once prometheus/client_java#1242 is resolved.
validation_build_flags='-Dtest=!SlidingWindowTest#rotate -Dsurefire.failIfNoSpecifiedTests=false'

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
