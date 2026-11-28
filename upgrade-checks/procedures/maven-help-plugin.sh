#!/usr/bin/env bash

# Compares the stdout and stderr of `mvn help:evaluate -q -DforceStdout`, on
# which `integration-tests/run-integration-test.sh` relies.

set -e -u -o pipefail

function probe() {
  mvn -N org.apache.maven.plugins:maven-help-plugin:3.6.0:evaluate -Dexpression='project.build.pluginManagement.pluginsAsMap(org.apache.maven.plugins:maven-help-plugin).version' -q -DforceStdout
}

function collect() {
  local expression
  for expression in version.error-prone project.version; do
    echo "${expression} stdout: $(mvn help:evaluate -Dexpression="${expression}" -q -DforceStdout 2> stderr.txt)"
    echo "${expression} stderr: $(cat stderr.txt)"
  done
}
