---
layout: default
title: FluxFlatMapUsage
parent: Bug Patterns
nav_order: 1
---
<!--
*** AUTO-GENERATED, DO NOT MODIFY ***
To make changes, edit the @BugPattern annotation or the explanation in docs/bugpattern.
-->

# FluxFlatMapUsage

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


# FluxFlatMapUsage

__&#96;Flux#flatMap&#96; and &#96;Flux#flatMapSequential&#96; have subtle semantics; please use &#96;Flux#concatMap&#96; or explicitly specify the desired amount of concurrency__

<div style="float:right;"><table id="metadata">
<tr><td>Severity</td><td>ERROR</td></tr>
<tr><td>Tags</td><td>LikelyError</td></tr>
</table></div>



## Suppression
Suppress false positives by adding the suppression annotation `@SuppressWarnings("FluxFlatMapUsage")` to the enclosing element.
