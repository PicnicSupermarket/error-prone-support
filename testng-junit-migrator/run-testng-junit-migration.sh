#!/bin/bash

set -e -u -o pipefail

# If this is not a Maven build, exit here to skip Maven-specific steps.
if [ ! -f pom.xml ]; then
  echo "Not a Maven build, exiting."
  exit 0
fi

function insert_dependency() {
  groupId=${1:?groupId not specified or empty}
  artifactId=${2:?artifactId not specified or empty}
  classifier=${3?classifier not specified}
  scope=${4?scope not specified}
  pomFile=${5:-pom.xml}

  # If the dependency declaration is already present (irrespective of scope),
  # then we don't modify the file.
  xmlstarlet sel -T -N 'x=http://maven.apache.org/POM/4.0.0' \
    -t -m "/x:project/x:dependencies/x:dependency[
                x:groupId/text() = '${groupId}' and
                x:artifactId/text() = '${artifactId}' and
                (x:classifier/text() = '${classifier}' or '${classifier}' = '')
            ]" -nl "${pomFile}" && return 0

  # Determine the index at which to insert the dependency declaration.
  insertionIndex="$(
    (xmlstarlet sel -T -N 'x=http://maven.apache.org/POM/4.0.0' \
      -t -m '/x:project/x:dependencies/x:dependency' \
      -v 'concat(x:groupId, " : ", x:artifactId, " : ", x:classifier, " : ", x:scope)' -nl \
      "${pomFile}" || true) |
      awk "\$0 < \"${groupId} : ${artifactId} : ${classifier} : ${scope}\"" |
      wc -l
  )"

  # Generate a placeholder that will be inserted at the place where the new
  # dependency declaration should reside. We need to jump through this hoop
  # because `xmlstarlet` does not support insertion of complex XML
  # sub-documents.
  placeholder="$(head -c 30 /dev/urandom | base64 | $sed_command 's,[^a-zA-Z0-9],,g')"

  # Insert the placeholder. (Note that only one case will match.)
  xmlstarlet ed -L -P -N 'x=http://maven.apache.org/POM/4.0.0' \
    -s "/x:project[not(x:dependencies)]" \
    -t elem -n dependencies -v "${placeholder}" \
    -i "/x:project/x:dependencies/x:dependency[${insertionIndex} = 0 and position() = 1]" \
    -t text -n placeholder -v "${placeholder}" \
    -a "/x:project/x:dependencies/x:dependency[${insertionIndex}]" \
    -t text -n placeholder -v "${placeholder}" \
    "${pomFile}"

  # Generate the XML subdocument we _actually_ want to insert.
  decl="$(echo "
            <dependency>
                <groupId>${groupId}</groupId>
                <artifactId>${artifactId}</artifactId>
                <classifier>${classifier}</classifier>
                <scope>${scope}</scope>
            </dependency>" |
    $sed_command '/></d' |
    $sed_command ':a;N;$!ba;s/\n/\\n/g')"

  # Replace the placeholder with the actual dependency declaration.
  $sed_command -i "s,${placeholder},${decl}," "${pomFile}"
}

if [[ -n "${1-}" ]] && [[ "${1}" == "--count" ]]; then
  echo "Counting number of tests..."
  test_results=$(mvn test | $grep_command -n "Results:" -A 3 | $grep_command -oP "Tests run: \K\d+(?=,)" | awk '{s+=$1} END {print s}')
  echo "Number of tests run: $test_results"
  exit
fi

function handle_file() {
  module=${1:?module not specified or empty}
  groupId=${2:?groupId not specified or empty}
  artifactId=${3:?artifactId not specified or empty}
  classifier=${4?classifier not specified}
  scope=${5?scope not specified}
  pomFile=${6:-pom.xml}

  if [[ -d $module ]] && [[ -f "$module/pom.xml" ]]; then
    cd "$module"
    insert_dependency "$groupId" "$artifactId" "$classifier" "$scope"
    echo "[$module] Added $groupId:$artifactId"
    cd -
  fi
}

case "$(uname -s)" in
Linux*)
  grep_command="grep"
  sed_command="sed"
  ;;
Darwin*)
  grep_command="ggrep"
  sed_command="gsed"
  ;;
*)
  echo "Unsupported distribution $(uname -s) for this script."
  exit 1
  ;;
esac

echo "Migrating to JUnit 5..."
echo "Adding required dependencies..."

if $grep_command -q "<packaging>pom</packaging>" "pom.xml"; then

  for module in $($grep_command -rl "org.testng.annotations.Test" $(pwd) | awk -F "$(pwd)" '{print $2}' | awk -F '/' '{print $2}' | uniq); do
    (
      handle_file "$module" "org.junit.jupiter" "junit-jupiter-api" "" "test"
    )
    (
      handle_file "$module" "org.junit.jupiter" "junit-jupiter-engine" "" "test"
    )
  done
  for module in $($grep_command -rl "org.testng.annotations.DataProvider" $(pwd) | awk -F "$(pwd)" '{print $2}' | awk -F '/' '{print $2}' | uniq); do
    (
      handle_file "$module" "org.junit.jupiter" "junit-jupiter-params" "" "test"
    )
  done
else
  if $grep_command -rq "org.testng.annotations.Test" "src/"; then
    handle_file "./" "org.junit.jupiter" "junit-jupiter-api" "" "test"
    handle_file "./" "org.junit.jupiter" "junit-jupiter-engine" "" "test"
  fi
  if $grep_command -rq "org.testng.annotations.DataProvider" "src/"; then
    handle_file "./" "org.junit.jupiter" "junit-jupiter-params" "" "test"
  fi
fi
echo "Running migration..."
mvn \
  -Perror-prone \
  -Ptestng-migrator \
  -Ppatch \
  clean test-compile fmt:format \
  -Derror-prone.patch-checks="TestNGJUnitMigration" \
  -Dverification.skip

echo "Finished executing migration!"
