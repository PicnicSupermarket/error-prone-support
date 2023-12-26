#!/usr/bin/env bash

# Executes Pitest to determine the mutation test coverage of changed code. By
# default code that is changed relative to the default branch is exercised,
# though another change set may be specified; see the Arcmutate Git integration
# documentation for details [1]. The results are found in each Maven module's
# `target/pit-reports` directory.
#
# [1] https://docs.arcmutate.com/docs/git-integration.html

set -e -u -o pipefail

if [ "${#}" -gt 1 ]; then
  echo "Usage: ${0} [DiffSpec]"
  exit 1
fi

diffSpec="${1:-+GIT(from[refs/remotes/origin/HEAD])}"

mvn clean test-compile pitest:mutationCoverage \
  -DargLine.xmx=2048m \
  -Dverification.skip \
  -Dfeatures="${diffSpec}"
