---
layout: default
title: ImmutablesSortedSetComparator
parent: Bug Patterns
nav_order: 1
---
<!--
*** AUTO-GENERATED, DO NOT MODIFY ***
To make changes, edit the @BugPattern annotation or the explanation in docs/bugpattern.
-->

# ImmutablesSortedSetComparator

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


# ImmutablesSortedSetComparator

__&#96;SortedSet&#96; properties of a &#96;@Value.Immutable&#96; or &#96;@Value.Modifiable&#96; type must be annotated with &#96;@Value.NaturalOrder&#96; or &#96;@Value.ReverseOrder&#96;__

<div style="float:right;"><table id="metadata">
<tr><td>Severity</td><td>ERROR</td></tr>
<tr><td>Tags</td><td>LikelyError</td></tr>
</table></div>



## Suppression
Suppress false positives by adding the suppression annotation `@SuppressWarnings("ImmutablesSortedSetComparator")` to the enclosing element.
