#!/usr/bin/env bash

# Updates `.mvn/checksums/checksums.sha256` by running Maven builds that record
# trusted artifact checksums.
#
# This script must be run any time a dependency is updated. Naturally, it must
# be executed in a trusted execution environment.

set -e -u -o pipefail

REPO_ROOT="$(git -C "$(dirname "${0}")" rev-parse --show-toplevel)"
SETTINGS="${REPO_ROOT}/settings.xml"
CHECKSUMS_DIR="${REPO_ROOT}/.mvn/checksums"

tmp_dir="$(mktemp -d)"
trap 'rm -rf -- "${tmp_dir}"' INT TERM HUP EXIT

# Configure Maven to record trusted checksums in a temporary directory. The
# first property specified here also enables the `offline` Maven profile
# defined in the top-level `pom.xml`, such that a full build also includes
# plugins that would otherwise not be loaded.
export MAVEN_ARGS="${MAVEN_ARGS:-} -Daether.artifactResolver.postProcessor.trustedChecksums.record -Daether.trustedChecksumsSource.summaryFile.basedir=${tmp_dir}"

# Run a full build to collect all project and plugin dependencies. Verification
# and test failures are ignored, as those are not uncommon just after a
# dependency upgrade. (Test execution is not simply skipped, as due to Surefire
# implementation details, not all dependencies are then collected.)
mvn install -Dverification.warn -Dmaven.test.failure.ignore

# Run another quick compilation round, just to pull in the Picnic Error Prone
# fork artifacts.
mvn -s "${SETTINGS}" -Perror-prone-fork compile -Dverification.skip

# On success, replace the checksums.
mv -- "${tmp_dir}/checksums.sha256" "${CHECKSUMS_DIR}/checksums.sha256"
