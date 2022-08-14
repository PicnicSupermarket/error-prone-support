#!/usr/bin/env bash

set -e -u -o pipefail

# XXX: Clean this up.
# XXX: Drop this class.
mvn clean test-compile fmt:format \
  -s "$(dirname "${0}")/settings.xml" \
  -T 1.0C \
  -Perror-prone \
  -Perror-prone-fork \
  -Ppatch \
  -Pself-check \
  -Derror-prone.patch-checks=ErrorProneTestHelperSourceFormat \
  -Derror-prone.self-check-args='-XepOpt:ErrorProneTestHelperSourceFormat:AvoidTextBlocks=true -XepDisableAllChecks' \
  -Dverification.skip
