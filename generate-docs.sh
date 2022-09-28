#!/usr/bin/env bash

WEBSITE_FOLDER="website"
HOMEPAGE="${WEBSITE_FOLDER}/index.md"

configure() {
    cd "$(git rev-parse --show-toplevel || echo .)" || exit
    mkdir "${BUGPATTERN_FOLDER}" 2>/dev/null
    mkdir "${REFASTER_FOLDER}" 2>/dev/null
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

configure
generate_homepage
