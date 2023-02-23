#!/usr/bin/bash

set -e -u -o pipefail

# If this is not a Maven build, exit here to skip Maven-specific steps.
if [ ! -f pom.xml ]; then
  exit 0
fi

# Use the Maven Wrapper executable if present.
MVN='mvn'
if [ -x ./mvnw ]; then
  MVN='./mvnw'
fi

"${MVN}" \
  -T 1.0C \
  -Perror-prone \
  -Ppatch \
  clean test-compile fmt:format \
  -Derror-prone.patch-checks="TestNGJUnitMigration" \
  -Dfrontend.skip \
  -Dverification.skip

echo "Finished migration steps!"
