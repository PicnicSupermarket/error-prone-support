#!/usr/bin/env bash

WEBSITE_FOLDER="website"
DOCS_FOLDER="docs"

BUGPATTERN_FOLDER="${WEBSITE_FOLDER}/bugpatterns"
BUGPATTERN_DOCS_FOLDER="${DOCS_FOLDER}/bugpatterns"

REFASTER_FOLDER="${WEBSITE_FOLDER}/refastertemplates"
REFASTER_DOCS_FOLDER="${DOCS_FOLDER}/refastertemplates"

HOMEPAGE="${WEBSITE_FOLDER}/index.md"

configure() {
    cd "$(git rev-parse --show-toplevel || echo .)"
    mkdir "${BUGPATTERN_FOLDER}" 2>/dev/null
    mkdir "${REFASTER_FOLDER}" 2>/dev/null
}

generate_homepage() {
    echo "Generating ${HOMEPAGE}"
    cat > "${HOMEPAGE}" << EOF
---
# Do not modify. This file is generated.
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
    sed $SEDOPTION 's/src="/src="assets\/images\//g' ${HOMEPAGE}
    sed $SEDOPTION 's/srcset="/srcset="assets\/images\//g' ${HOMEPAGE}
}

generate_bugpattern_docs() {
    BUGPATTERNS=$(find error-prone-contrib/src/main/java/tech/picnic/errorprone/bugpatterns -type f -iname "*.java" ! -iname "package-info.java")
    for BUGPATTERN in $BUGPATTERNS; do
        NAME=$(basename "${BUGPATTERN}" ".java")
        FILENAME="${BUGPATTERN_FOLDER}/${NAME}.md"

        EXTRA_DOCS=$(cat "${BUGPATTERN_DOCS_FOLDER}/${NAME}.md" 2>/dev/null)

        echo "Generating ${FILENAME}"
        cat > "${FILENAME}" << EOF
---
# Do not modify. This file is generated.
layout: default
title: ${NAME}
parent: Bug Patterns
nav_order: 1
---

# ${NAME}

Simplification
{: .label .label-blue }

Suggestion
{: .label .label-yellow }

${EXTRA_DOCS}

## Samples

\`\`\`java
public static void sample() {}
\`\`\`

<a href="https://github.com/PicnicSupermarket/error-prone-support/blob/master/${BUGPATTERN}" class="fs-3 btn external" target="_blank">
    View source code on GitHub
    <svg viewBox="0 0 24 24" aria-labelledby="svg-external-link-title"><use xlink:href="#svg-external-link"></use></svg>
</a>
EOF
    done
}

generate_refaster_docs() {
    TEMPLATES=$(find error-prone-contrib/src/main/java/tech/picnic/errorprone/refastertemplates -type f -iname "*.java" ! -iname "package-info.java")
    for TEMPLATE in $TEMPLATES; do
        NAME=$(basename "${TEMPLATE}" ".java")
        FILENAME="${REFASTER_FOLDER}/${NAME}.md"

        EXTRA_DOCS=$(cat "${REFASTER_DOCS_FOLDER}/${NAME}.md" 2>/dev/null)

        echo "Generating ${FILENAME}"
        cat > "${FILENAME}" << EOF
---
# Do not modify. This file is generated.
layout: default
title: ${NAME}
parent: Refaster templates
nav_order: 1
---

# ${NAME}

Style
{: .label .label-blue }

Error
{: .label .label-red }

${EXTRA_DOCS}

## Samples

\`\`\`java
public static void sample() {}
\`\`\`

<a href="https://github.com/PicnicSupermarket/error-prone-support/blob/master/${TEMPLATE}" class="fs-3 btn external">
    View source code on GitHub
    <svg viewBox="0 0 24 24" aria-labelledby="svg-external-link-title"><use xlink:href="#svg-external-link"></use></svg>
</a>
EOF
    done
}

# Do it
configure
generate_homepage
generate_bugpattern_docs
generate_refaster_docs
