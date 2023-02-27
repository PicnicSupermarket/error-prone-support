#!/usr/bin/bash

set -e -u -o pipefail

# If this is not a Maven build, exit here to skip Maven-specific steps.
if [ ! -f pom.xml ]; then
  exit 0
fi

# Use the Maven Wrapper executable if present.
MVN='mvn'
if [ -x ./mvnw ]; then
  MVN='./mvnw'
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
            ]" -nl "${pomFile}" && exit

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
    # because `xmlstarlet` does not support insertion of complex XML subdocuments.
    placeholder="$(head -c 30 /dev/urandom | base64 | sed 's,[^a-zA-Z0-9],,g')"

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
        sed '/></d' |
        sed ':a;N;$!ba;s/\n/\\n/g')"

    # Replace the placeholder with the actual dependeny declaration.
    sed -i "s,${placeholder},${decl}," "${pomFile}"
}

echo "Counting number of TestNG tests..."
# count testng tests
testng_results=$(${MVN} test | grep -n "Results:" -A 3 | grep -oP "Tests run: \K\d+(?=,)" | awk '{s+=$1} END {print s}')
echo "Number of TestNG tests run: $testng_results"


echo "Migrating to JUnit 5..."
echo "Adding required dependencies..."
for module in $(grep -rl "org.testng.annotations.Test" $(pwd) | awk -F "$(pwd)" '{print $2}' | awk -F '/' '{print $2}' | uniq); do
    (
        cd $module
        insert_dependency "org.junit.jupiter" "junit-jupiter-api" "" "test"
    )
    echo "[$module] Added org.junit.jupiter:junit-jupiter-api"
    (
        cd $module
        insert_dependency "org.junit.jupiter" "junit-jupiter-engine" "" "test"
    )
    echo "[$module] Added org.junit.jupiter:junit-jupiter-engine"
done
for module in $(grep -rl "@DataProvider" $(pwd) | awk -F "$(pwd)" '{print $2}' | awk -F '/' '{print $2}' | uniq); do
    (
        cd $module
        insert_dependency "org.junit.jupiter" "junit-jupiter-params" "" "test"
    )
    echo "[$module] Added org.junit.jupiter:junit-jupiter-params"
done

echo "Running migration..."
"${MVN}" \
  -Perror-prone \
  -Ptestng-migrator \
  -Ppatch \
  clean test-compile fmt:format \
  -Derror-prone.patch-checks="TestNGJUnitMigration" \
  -Dfrontend.skip \
  -Dverification.skip

echo "Counting JUnit tests run..."
junit_results=$(${MVN} test | grep -n "Results:" -A 3 | grep -oP "Tests run: \K\d+(?=,)" | awk '{s+=$1} END {print s}')
echo "Number of TestNG tests run: $testng_results"
echo "Number of JUnit tests run: $junit_results"
echo "Difference: $(expr ${testng_results} - ${junit_results})"

echo "Finished migration steps!"
