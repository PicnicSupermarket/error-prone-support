#!/usr/bin/env bash

set -e -u -o pipefail

test_name="$(basename "${0}" .sh)"
project='prometheus-java-client'
repository='https://github.com/prometheus/client_java.git'
revision='v1.4.1'
# XXX: The `prometheus-metrics-{exporter-opentelemetry,exposition-formats}-shaded`
# modules copy the sources of the sibling module they shade during their
# `validate` phase, without declaring a dependency on it. Under `-T1C` such a
# copy may therefore observe sources that Error Prone is concurrently
# rewriting. Declaring the missing dependencies would change what the Shade
# plugin bundles, so this is left to
# https://github.com/prometheus/client_java/pull/2407.
additional_build_flags='-Djava.version=21 -Dwarnings='
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
