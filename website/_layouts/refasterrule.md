---
layout: default
---

{% capture markdown_layout %}

# {{ page.name }}
{: .no_toc }

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

{: .note-title }
> Suppression
>
> Disable all rules by adding `-XepOpt:Refaster:NamePattern=^(?!{{page.name}}\$).*` as
> compiler argument.
{% comment %}
  # XXX: Create an internal page on documenting the usage of compiler flags.
{% endcomment %}

<details open markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
  1. TOC
  {:toc}
</details>

{% for rule in page.rules %}
## {{rule.name}}

{{ page.severity }}
  {: .label .label-{{ site.data.severities[rule.severity].color }} }

{% for tag in rule.tags %}
{{ tag }}
  {: .label }
{% endfor %}

{: .note-title }
> Suppression
>
> Suppress false positives by adding the suppression annotation `@SuppressWarnings("{{rule.name}}")` to
> the enclosing element.
>
> Disable this rule by adding `-XepOpt:Refaster:NamePattern=^(?!{{page.name}}\${{rule.name}}).*`
> as compiler argument.
{% comment %}
  # XXX: Create an internal page on documenting the usage of compiler flags.
{% endcomment %}

### Samples
{: .no_toc .text-delta }

Shows the difference in example code before and after the Refaster rule is
applied.

{% highlight diff %}
{{ rule.diff }}
{% endhighlight %}
{% endfor %}

{% endcapture %}
{{ markdown_layout | markdownify }}
