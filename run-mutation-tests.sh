#!/usr/bin/env bash

set -euo pipefail

targetTests=${1:-*}

mvn clean test pitest:mutationCoverage \
  -DargLine.xmx=2048m \
  -Dverification.skip \
  -DfailIfNoTests=false \
  -Dtest="${targetTests}" \
  -DtargetTests="${targetTests}"
