#!/usr/bin/env bash

# Lists potential dependency, plugin and property upgrades. Suggested upgrades
# are subject to the rules configured in the `versions-maven-plugin`
# configuration in `pom.xml`.
#
# XXX: Consider extending this script beyond Maven dependencies.

set -e -u -o pipefail

settings="$(dirname "${0}")/settings.xml"

mvn -N -U \
  -s "${settings}" \
  -Perror-prone-fork \
  versions:display-dependency-updates \
  versions:display-plugin-updates \
  versions:display-property-updates
