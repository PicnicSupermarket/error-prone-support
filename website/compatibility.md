---
layout: default
title: Compatibility matrix
nav_order: 2
---

# Compatibility matrix

{% comment %}
XXX: Once available on the default branch, include a link to the
`generate-version-compatibility-overview.sh` script.
{% endcomment %}

Error Prone Support releases are generally compatible with only a limited
number of Error Prone releases. The table below shows, for each Error Prone
Support release, the Error Prone versions it is expected to be compatible with.
Compatibility is determined by:
1. Compiling and testing the Error Prone Support release source code against a
   given Error Prone version. This validates source and behavioral
   compatibility.[^1] [^2]
2. Applying the released Refaster rules using a given Error Prone version. This
   validates that the rules can be read by the targeted version of Error Prone,
   proving that the serialization format is compatible.

| Error Prone Support version | Compatible Error Prone versions |
| --------------------------- | ------------------------------- |
{% for release in site.data.compatibility.releases -%}
| [{{ release.version }}](https://github.com/PicnicSupermarket/error-prone-support/releases/tag/v{{ release.version }}) | {%
for version in release.compatible -%}
[{{ version }}](https://github.com/google/error-prone/releases/tag/v{{ version }}){% unless forloop.last %}, {% endunless %}
{%- endfor %} |
{% endfor %}

[^1]: Note that this [does not prove][source-binary-compat] that the Error Prone Support and Error Prone versions are _binary_ compatible. This limitation does not appear to be an issue in practice.
[^2]: The approach taken here may yield false negatives, because a reported incompatibility may merely be due to a test API incompatibility.
[source-binary-compat]: https://stackoverflow.com/questions/57871898/
