#!/usr/bin/env bash

TARGET_JDK=17

# Builds the project twice: once against the original Error Prone release,
# using only Error Prone checks available on Maven Central, and once against
# the Picnic Error Prone fork, additionally (a) enabling all checks defined in
# this project and (b) with tests executed against the oldest JDK supported.

set -e -u -o pipefail

settings="$(dirname "${0}")/settings.xml"
toolchains="${HOME}/.m2/toolchains.xml"

if ! grep -q "<version>${TARGET_JDK}</version>" "${toolchains}"; then
  echo "Error: JDK version ${TARGET_JDK} not specified in ${toolchains}." 1>&2
  exit 1
fi

mvn clean install \
  -T1C \
  -s "${settings}" \
  $@

mvn clean verify \
  -T1C \
  -s "${settings}" \
  -Perror-prone-fork \
  -Pself-check \
  -Dsurefire.jdk-toolchain-version="${TARGET_JDK}" \
  $@
