#!/usr/bin/env bash

set -e -u -o pipefail

test_name="$(basename "${0}" .sh)"
project='prometheus-java-client'
repository='https://github.com/prometheus/client_java.git'
revision='v1.3.8'
additional_build_flags='-Djava.version=11 -Dwarnings='
additional_source_directories=''
shared_error_prone_flags=''
patch_error_prone_flags=''
validation_error_prone_flags=''
# Validation skips some tests:
# - Starting from a clean repository, the `PushGatewayIT` tests reference a JAR
#   file that is created only after test completion, causing the tests to fail.
# - The `SlidingWindowTest#rotate` test is flaky.
# XXX: Drop the `SlidingWindowTest` exclusion once prometheus/client_java#1242
# is resolved. For unclear reasons, it appears that without this exclusion the
# integration tests are no longer executed, meaning that perhaps all flags
# specified here can then be dropped.
validation_build_flags='-Dtest=!PushGatewayIT,!SlidingWindowTest#rotate -Dsurefire.failIfNoSpecifiedTests=false'

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
