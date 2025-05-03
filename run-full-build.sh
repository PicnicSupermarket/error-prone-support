#!/usr/bin/env bash

# Builds the project twice: once against the original Error Prone release,
# using only Error Prone checks available on Maven Central, and once against
# the Picnic Error Prone fork, additionally enabling all checks defined in this
# project and any Error Prone checks available only from other artifact
# repositories.

set -e -u -o pipefail

settings="$(dirname "${0}")/settings.xml"

mvn clean install \
  -T1C \
  -s "${settings}" \
  $@
mvn clean install \
  -T1C \
  -s "${settings}" \
  -Perror-prone-fork \
  -Pself-check \
  $@
