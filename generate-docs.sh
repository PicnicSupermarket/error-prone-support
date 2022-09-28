#!/usr/bin/env bash

WEBSITE_FOLDER="website"
HOMEPAGE="${WEBSITE_FOLDER}/index.md"

configure() {
    cd "$(git rev-parse --show-toplevel || echo .)" || exit
    mkdir -p "${BUGPATTERN_FOLDER}"
    mkdir -p "${REFASTER_FOLDER}"
}

generate_homepage() {
    echo "Generating ${HOMEPAGE}"
    cat > "${HOMEPAGE}" << EOF
---
layout: default
title: Home
nav_order: 1
---
EOF

    cat "README.md" >> ${HOMEPAGE}

    SEDOPTION="-i"
    if [[ "$OSTYPE" == "darwin"* ]]; then
        SEDOPTION="-i .bak"
    fi
    sed $SEDOPTION 's/src="website\//src="/g' ${HOMEPAGE}
    sed $SEDOPTION 's/srcset="website\//srcset="/g' ${HOMEPAGE}
}

# Generate the website.
configure
generate_homepage
