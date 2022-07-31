#!/usr/bin/env bash

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
