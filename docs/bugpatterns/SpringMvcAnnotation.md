---
layout: default
title: SpringMvcAnnotation
parent: Bug Patterns
nav_order: 1
---
<!--
*** AUTO-GENERATED, DO NOT MODIFY ***
To make changes, edit the @BugPattern annotation or the explanation in docs/bugpattern.
-->

# SpringMvcAnnotation

Simplification

${EXTRA_DOCS}

## Samples

\`\`\`java
public static void sample() {}
\`\`\`

<a href="https://github.com/PicnicSupermarket/error-prone-support/blob/master/${BUGPATTERN}" class="fs-3 btn external" target="_blank">
    View source code on GitHub
    <svg viewBox="0 0 24 24" aria-labelledby="svg-external-link-title"><use xlink:href="#svg-external-link"></use></svg>
</a>


# SpringMvcAnnotation

__Prefer the conciseness of &#96;@{Get,Put,Post,Delete,Patch}Mapping&#96; over &#96;@RequestMapping&#96;__

<div style="float:right;"><table id="metadata">
<tr><td>Severity</td><td>SUGGESTION</td></tr>
<tr><td>Tags</td><td>Simplification</td></tr>
</table></div>



## Suppression
Suppress false positives by adding the suppression annotation `@SuppressWarnings("SpringMvcAnnotation")` to the enclosing element.
