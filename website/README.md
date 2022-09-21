# Error Prone Support website

This directory contains the majority of the source code that powers
[error-prone.picnic.tech][error-prone-support-website]. The website is
statically generated using [Jekyll][jekyll].

# Local development

To view the webite on `localhost`, first follow the [Jekyll installation
instructions][jekyll-docs-installation]. Once done, in this directory execute:

```sh
bundle install
../generate-docs.sh && bundle exec jekyll serve --livereload
```

The website will now be [available][localhost-port-4000] on port 4000. Source
code modifications will automatically be reflected. (An exception is
`_config.yml`: changes to this file require a server restart.) Subsequent
server restarts do not require running `bundle install`, unless `Gemfile` has
been updated in the interim.

Documentation can be re-generated whist jekyll is running, by executing:

```sh
../generate-docs.sh
```

If you are not familiar with Jekyll, be sure to check out its
[documentation][jekyll-docs]. It is recommended to follow the provided
step-by-step tutorial.

# Deployment

The website is regenerated and deployed using the
[`deploy-website.yaml`][error-prone-support-website-deploy-workflow] Github
Actions workflow any time a change is merged to `master`.

[error-prone-support-website-deploy-workflow]: https://github.com/PicnicSupermarket/error-prone-support/actions/workflows/deploy-website.yaml
[error-prone-support-website]: https://error-prone.picnic.tech
[jekyll-docs]: https://jekyllrb.com/docs/
[jekyll-docs-installation]: https://jekyllrb.com/docs/installation/
[jekyll]: https://jekyllrb.com
[localhost-port-4000]: http://127.0.0.1:4000
