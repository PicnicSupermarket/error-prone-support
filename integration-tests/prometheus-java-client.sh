#!/usr/bin/env bash

set -e -u -o pipefail

test_name="$(basename "${0}" .sh)"
project='prometheus-java-client'
repository='https://github.com/prometheus/client_java.git'
revision='v1.8.0'
additional_build_flags='-Dwarnings='
additional_source_directories=''
shared_error_prone_flags=''
patch_error_prone_flags=''
validation_error_prone_flags=''
validation_build_flags=''

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
