#!/usr/bin/env bash
set -e -u -o pipefail

generate_bugpattern() {
    json=$(echo ${1} | base64 --decode | jq -c ".")
    name=$(echo $json | jq ".name")
    input=$(echo $json | jq ".inputLines")
    output=$(echo $json | jq ".outputLines")
    echo $name
    # TODO: format these values nicely
}

generate_bugpattern_docs() {
    json_data=$(cat "error-prone-contrib/target/bug-pattern-data.jsonl")
    for row in $(echo ${json_data} | jq -s "." | jq -r ".[] | @base64"); do
        generate_bugpattern $row
    done
}

generate_bugpattern_docs
