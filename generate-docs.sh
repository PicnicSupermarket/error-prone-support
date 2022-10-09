#!/usr/bin/env bash

set -e -u -o pipefail

REPOSITORY_ROOT="$(git rev-parse --show-toplevel)"
WEBSITE_ROOT="${REPOSITORY_ROOT}/website"

generate_homepage() {
    local homepage="${WEBSITE_ROOT}/index.md"

    echo "Generating ${homepage}..."
    cat - "${REPOSITORY_ROOT}/README.md" > "${homepage}" << EOF
---
layout: default
title: Home
nav_order: 1
---
EOF

    local macos_compat=""
    [[ "${OSTYPE}" == "darwin"* ]] && macos_compat="yes"
    sed -i ${macos_compat:+".bak"} 's/src="website\//src="/g' "${homepage}"
    sed -i ${macos_compat:+".bak"} 's/srcset="website\//srcset="/g' "${homepage}"
}

# Generate the website.
generate_homepage
