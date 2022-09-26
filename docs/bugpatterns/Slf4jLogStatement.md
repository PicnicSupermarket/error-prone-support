---
layout: default
title: Slf4jLogStatement
parent: Bug Patterns
nav_order: 1
---
<!--
*** AUTO-GENERATED, DO NOT MODIFY ***
To make changes, edit the @BugPattern annotation or the explanation in docs/bugpattern.
-->

# Slf4jLogStatement

LikelyError

${EXTRA_DOCS}

## Samples

\`\`\`java
public static void sample() {}
\`\`\`

<a href="https://github.com/PicnicSupermarket/error-prone-support/blob/master/${BUGPATTERN}" class="fs-3 btn external" target="_blank">
    View source code on GitHub
    <svg viewBox="0 0 24 24" aria-labelledby="svg-external-link-title"><use xlink:href="#svg-external-link"></use></svg>
</a>


# Slf4jLogStatement

__Make sure SLF4J log statements contain proper placeholders with matching arguments__

<div style="float:right;"><table id="metadata">
<tr><td>Severity</td><td>WARNING</td></tr>
<tr><td>Tags</td><td>LikelyError</td></tr>
</table></div>



## Suppression
Suppress false positives by adding the suppression annotation `@SuppressWarnings("Slf4jLogStatement")` to the enclosing element.
