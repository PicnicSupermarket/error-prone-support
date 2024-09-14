---
# XXX: To be implemented:
# - Support for alt names.
# - Support for explanations.
# - Support for "can disable".
# - Support for custom suppression annotations.
layout: default
---

{% capture markdown_layout %}

# {{ page.name }}

{{ page.severity }}
  {: .label .label-{{ site.data.severities[page.severity].color }} }

{% for tag in page.tags %}
{{ tag }}
  {: .label }
{% endfor %}

<a href="https://github.com/PicnicSupermarket/error-prone-support/blob/master/{{ page.source }}" class="fs-3 btn external" target="_blank">
    View source code on GitHub
    <svg viewBox="0 0 24 24" aria-labelledby="svg-external-link-title">
        <use xlink:href="#svg-external-link"></use>
    </svg>
</a>

{: .summary-title }
> Summary
>
> {{ page.summary }}

{% comment %}
  # XXX: Here, include a more elaborate explantion, if available.
{% endcomment %}

{: .note-title }
> Suppression
>
> Suppress false positives by adding the suppression annotation `@SuppressWarnings("{{ page.name }}")` to
> the enclosing element.
>
> Disable this pattern completely by adding `-Xep:{{ page.name }}:OFF` as compiler argument.
> [Learn more][error-prone-flags].
{% comment %}
  # XXX: Create an internal page on documenting the usage of compiler flags.
{% endcomment %}

{% if page.replacement or page.identification %}

## Samples

{% comment %}
  # XXX: Either make this "Samples" header useful, or drop it. (In which case
  # the wrapping conjunctive guard should also go.)
{% endcomment %}

{% if page.replacement %}

### Replacement

Shows the difference in example code before and after the bug pattern is
applied.

{% for diff in page.replacement %}
{% highlight diff %}
{{ diff }}
{% endhighlight %}
{% endfor %}

{% endif %}

{% if page.identification %}

### Identification

Shows code lines which will (not) be flagged by this bug pattern. \
A `//BUG: Diagnostic contains:` comment is placed above any violating line.

{% for source in page.identification %}
{% highlight java %}
{{ source }}
{% endhighlight %}
{% endfor %}

{% endif %}

{% endif %}

[error-prone-flags]: https://errorprone.info/docs/flags

{% endcapture %}
{{ markdown_layout | markdownify }}
