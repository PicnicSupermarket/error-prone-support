#!/usr/bin/env bash

# Compares the upgrades listed by `./list-potential-upgrades.sh` (except for
# this plugin itself), and the files changed by `mvn versions:set`. As the
# listing reflects live repository metadata, a release published between the
# base and head runs shows up as a spurious difference.

set -e -u -o pipefail

function probe() {
  mvn -N org.apache.maven.plugins:maven-help-plugin:3.6.0:evaluate -Dexpression='project.build.pluginManagement.pluginsAsMap(org.codehaus.mojo:versions-maven-plugin).version' -q -DforceStdout
}

function collect() {
  ./list-potential-upgrades.sh \
    | tee /dev/stderr \
    | sed -n 's/\x1b\[[0-9;]*m//g; /versions-maven-plugin/{/\.\.\.$/N; d}; /^\[INFO\]   /p'
  mvn versions:set -DnewVersion=9.9.9-TEST >&2
  git diff --numstat
}
