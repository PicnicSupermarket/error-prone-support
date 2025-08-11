#!/usr/bin/env bash

set -e -u -o pipefail

test_name="$(basename "${0}" .sh)"
project='metrics'
repository='https://github.com/dropwizard/metrics.git'
revision='v5.0.0-rc22'
additional_build_flags=''
additional_source_directories=''
shared_error_prone_flags='-XepExcludedPaths:.*/target/generated-sources/.* -XepOpt:Slf4jLoggerDeclaration:CanonicalStaticLoggerName=LOGGER'
patch_error_prone_flags=''
validation_error_prone_flags=''
# Validation skips two instances of the
# `registersExpectedMetricsGivenNameStrategy` test because it attemps to
# connect to `example.com:80`, an operation that often times out. This in turn
# also requires specifying `-Dsurefire.failIfNoSpecifiedTests=false`, as
# otherwise Surefire complains that no tests are found in the `docs` module.
# Note that enabling the tests would also require adding `example.com:80` to
# the set of allowed endpoints in the integration tests GitHub Actions
# Harden-Runner configuration.
validation_build_flags='-Dtest=!InstrumentedHttpClientsTest#registersExpectedMetricsGivenNameStrategy -Dsurefire.failIfNoSpecifiedTests=false'

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
