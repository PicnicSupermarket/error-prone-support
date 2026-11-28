#!/usr/bin/env bash

# Compares how the `reviewdog` binary used by `reviewdog.yml` parses sample
# javac and Checkstyle output, modelled after that workflow's actual input:
# warnings and SARIF results that reference source files by absolute path or
# `file:` URI, processed from the repository root. Reporting to GitHub is not
# covered.

set -e -u -o pipefail

function probe() {
  grep -oP 'reviewdog_version: v\K\S+' .github/workflows/reviewdog.yml
}

function collect() {
  local version
  local release
  local efm
  local source="${PWD}/error-prone-utils/src/main/java/tech/picnic/errorprone/utils/SourceCode.java"
  version="$(probe)"
  release="https://github.com/reviewdog/reviewdog/releases/download/v${version}"
  efm="$(grep -oP "\-efm='\K[^']+" .github/workflows/reviewdog.yml)"
  curl -fsSLO "${release}/reviewdog_${version}_Linux_x86_64.tar.gz" -O "${release}/checksums.txt"
  sha256sum -c --ignore-missing checksums.txt >&2
  tar -xzf "reviewdog_${version}_Linux_x86_64.tar.gz" reviewdog

  echo "[WARNING] ${source}:[69,24] [UnnecessaryFinal] Since Java 8, it's been unnecessary to make local variables final." \
    | ./reviewdog -name=javac -efm="${efm}" -filter-mode=nofilter -reporter=sarif
  echo '{"version":"2.1.0","runs":[{"tool":{"driver":{"name":"Checkstyle"}},"results":[{"level":"error","locations":[{"physicalLocation":{"artifactLocation":{"uri":"file:'"${source}"'"},"region":{"startColumn":47,"startLine":7}}}],"message":{"id":"trailing.comments","text":"Don'"'"'t use trailing comments."},"ruleId":"com.puppycrawl.tools.checkstyle.checks.TrailingCommentCheck"}]}]}' \
    | ./reviewdog -name=Checkstyle -f=sarif -filter-mode=nofilter -reporter=sarif
}
