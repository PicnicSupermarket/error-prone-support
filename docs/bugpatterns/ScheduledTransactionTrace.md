---
layout: default
title: ScheduledTransactionTrace
parent: Bug Patterns
nav_order: 1
---
<!--
*** AUTO-GENERATED, DO NOT MODIFY ***
To make changes, edit the @BugPattern annotation or the explanation in docs/bugpattern.
-->

# ScheduledTransactionTrace

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


# ScheduledTransactionTrace

__Scheduled operation must start a new New Relic transaction__

<div style="float:right;"><table id="metadata">
<tr><td>Severity</td><td>ERROR</td></tr>
<tr><td>Tags</td><td>LikelyError</td></tr>
</table></div>



## Suppression
Suppress false positives by adding the suppression annotation `@SuppressWarnings("ScheduledTransactionTrace")` to the enclosing element.
