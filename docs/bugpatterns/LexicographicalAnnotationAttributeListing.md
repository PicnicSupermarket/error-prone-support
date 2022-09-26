---
layout: default
title: LexicographicalAnnotationAttributeListing
parent: Bug Patterns
nav_order: 1
---
<!--
*** AUTO-GENERATED, DO NOT MODIFY ***
To make changes, edit the @BugPattern annotation or the explanation in docs/bugpattern.
-->

# LexicographicalAnnotationAttributeListing

Style

${EXTRA_DOCS}

## Samples

\`\`\`java
public static void sample() {}
\`\`\`

<a href="https://github.com/PicnicSupermarket/error-prone-support/blob/master/${BUGPATTERN}" class="fs-3 btn external" target="_blank">
    View source code on GitHub
    <svg viewBox="0 0 24 24" aria-labelledby="svg-external-link-title"><use xlink:href="#svg-external-link"></use></svg>
</a>


# LexicographicalAnnotationAttributeListing

__Where possible, sort annotation array attributes lexicographically__

<div style="float:right;"><table id="metadata">
<tr><td>Severity</td><td>SUGGESTION</td></tr>
<tr><td>Tags</td><td>Style</td></tr>
</table></div>



## Suppression
Suppress false positives by adding the suppression annotation `@SuppressWarnings("LexicographicalAnnotationAttributeListing")` to the enclosing element.
