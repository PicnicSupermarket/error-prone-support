#!/usr/bin/env bash

# Executes Pitest to determine the code base' mutation test coverage. The set
# of tests executed can optionally be restricted by name. The results are found
# in each Maven module's `target/pit-reports` directory.

set -e -u -o pipefail

if [ "${#}" -gt 1 ]; then
  echo "Usage: ./$(basename "${0}") [TargetTests]"
  exit 1
fi

targetTests=${1:-*}

mvn clean test pitest:mutationCoverage \
  -DargLine.xmx=2048m \
  -Dverification.skip \
  -DfailIfNoTests=false \
  -Dtest="${targetTests}" \
  -DtargetTests="${targetTests}"
