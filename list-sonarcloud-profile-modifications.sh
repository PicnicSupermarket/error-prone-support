#!/usr/bin/env bash

# Compares the `Picnic` SonarCloud quality profile for Java against the default
# `Sonar way` profile. While the web UI also provides such functionality, this
# script also compares the configuration parameters of each rule.

set -e -u -o pipefail

if [ -z "${SONAR_TOKEN}" ]; then
  echo 'Environment variable `SONAR_TOKEN` is not set.'
  exit 1
fi

export_profile() {
  local profile="${1}"

  curl -f -s -u "${SONAR_TOKEN}:" \
      "https://sonarcloud.io/api/qualityprofiles/export?qualityProfile=${profile}&language=java&organization=picnic-technologies"
}

tabulate() {
  faq -r '
    def enumerate_params:
      if .parameters == "" then
        []
      elif (.parameters.parameter | type) == "object" then
        .parameters.parameter | [.key, .value]
      else
        .parameters.parameter[] | [.key, .value]
      end;

    .profile.rules.rule
      | map([.repositoryKey, .key, .priority] + enumerate_params)
      | sort
      | .[]
      | @tsv
  ' -o json
}

vimdiff \
  <(export_profile 'Sonar%20way' | tabulate) \
  <(export_profile 'Picnic' | tabulate)
