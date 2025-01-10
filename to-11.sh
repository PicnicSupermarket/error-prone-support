#!/usr/bin/env bash

set -e -u -o pipefail

# XXX: Clean this up.
# XXX: Drop this script.
mvn clean test-compile fmt:format \
  -s "$(dirname "${0}")/settings.xml" \
  -T 1.0C \
  -Perror-prone \
  -Perror-prone-fork \
  -Ppatch \
  -Pself-check \
  -Derror-prone.patch-checks=TestHelperSourceFormat \
  -Derror-prone.self-check-args='-XepOpt:TestHelperSourceFormat:AvoidTextBlocks=true' \
  -Dverification.skip
