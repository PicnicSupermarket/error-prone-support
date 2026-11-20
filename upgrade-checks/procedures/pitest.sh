#!/usr/bin/env bash

# Compares the results of a full `./run-mutation-tests.sh` run, ignoring known
# run-to-run noise: the killing test of a killed mutant, and `KILLED` <->
# `TIMED_OUT` flips (both count as detected).

set -e -u -o pipefail

function probe() {
  mvn -N org.apache.maven.plugins:maven-help-plugin:3.6.0:evaluate -Dexpression='project.build.pluginManagement.pluginsAsMap(org.pitest:pitest-maven)' -q -DforceStdout \
    | sed -n 's/^ *<\(groupId\|artifactId\|version\)>\(.*\)<.*/\2/p' \
    | paste -d : - - -
}

function collect() {
  ./run-mutation-tests.sh >&2
  cut -d, -f1-6 -- */target/pit-reports/mutations.csv | sed 's/,TIMED_OUT$/,KILLED/' | sort
}
