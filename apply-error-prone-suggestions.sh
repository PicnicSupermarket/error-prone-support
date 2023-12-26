#!/usr/bin/env bash

# Compiles the code using Error Prone and applies its suggestions. The set of
# checks applied can optionally be restricted by name.
#
# As this script may modify the project's code, it is important to execute it
# in a clean Git working directory.

set -e -u -o pipefail

if [ "${#}" -gt 1 ]; then
  echo "Usage: ${0} [PatchChecks]"
  exit 1
fi

patchChecks=${1:-}

mvn clean test-compile fmt:format \
  -s "$(dirname "${0}")/settings.xml" \
  -T 1.0C \
  -Perror-prone \
  -Perror-prone-fork \
  -Ppatch \
  -Pself-check \
  -Derror-prone.patch-checks="${patchChecks}" \
  -Dverification.skip
