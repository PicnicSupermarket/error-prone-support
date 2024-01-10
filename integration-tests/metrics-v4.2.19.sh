#!/usr/bin/env bash

set -e -u -o pipefail

test_name="$(basename "${0}" .sh)"
project=metrics
repository=https://github.com/dropwizard/metrics.git
revision=v4.2.19

if [ "${#}" -gt 2 ] || ([ "${#}" = 2 ] && [ "${1:---sync}" != '--sync' ]); then
  echo "Usage: ${0} [--sync] [<report_directory>]"
  exit 1
fi
do_sync="$([ "${#}" = 0 ] || [ "${1:-}" != '--sync' ] || echo 1)"
report_directory="$([ "${#}" = 0 ] || ([ -z "${do_sync}" ] && echo "${1}") || ([ "${#}" = 1 ] || echo "${2}"))"

# XXX: We exclude the `CollectorMutability` and the `Immutable*|Preconditions*|StringRules.StringIsNullOrEmpty` refaster rules
# as they introduce changes that expect guava to be on the classpath.
patch_flags="-Xep:CollectorMutability:OFF -XepOpt:Refaster:NamePattern=^((?!(Immutable|Preconditions|StringRules\.StringIsNullOrEmpty)).*)"

"$(dirname ${0})"/run-integration-test.sh "$test_name" "$project" "$repository" "$revision" "" "" "${patch_flags}" "" "" "$do_sync" "$report_directory" 
