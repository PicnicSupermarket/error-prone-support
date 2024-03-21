# Error Prone Support website

This directory contains the majority of the source code that powers
[error-prone.picnic.tech][error-prone-support-website]. The website is
statically generated using [Jekyll][jekyll].

# Local development

To view the website on `localhost`, first follow the [Jekyll installation
instructions][jekyll-docs-installation]. Once done, run the following Maven
commands in the root of the repository to extract the (test) data from the bug
patterns and Refaster rule collections and to transform this data into a
Jekyll-digestible format. Unless and relevant Java code has been changed, these
commands needs to be executed once.

```sh
mvn -T1C clean install -DskipTests -Dverification.skip -Pdocgen
mvn exec:java@generate-docs -pl documentation-support
```

Then to build the website for local development, execute in this directory:

```sh
bundle install
bundle exec jekyll serve --livereload
```

The website will now be [available][localhost-port-4000] on port 4000. Source
code modifications (including the result of rerunning `mvn
exec:java@generate-docs -pl documentation-support`) will automatically be
reflected. (An exception is `_config.yml`: changes to this file require a
server restart.) Subsequent server restarts do not require running `bundle
install`, unless `Gemfile` has been updated in the interim.

If you are not familiar with Jekyll, be sure to check out its
[documentation][jekyll-docs]. It is recommended to follow the provided
step-by-step tutorial.

We use the [Just the Docs][just-the-docs] Jekyll theme, which also includes
several configuration options.

###### Switch Ruby versions

The required Ruby version is set in `.ruby-version`. To switch, you can use
[rvm][rvm] to manage your Ruby version.

###### Resolve Bundler issues

On macOS, you may get an error such as the following when running `bundle
install`:

```sh
fatal error: 'openssl/ssl.h' file not found
```

In that case, run:

```sh
bundle config build.eventmachine --with-cppflags="-I$(brew --prefix openssl)/include"
bundle install
```

# Deployment

The website is regenerated and deployed using the
[`deploy-website.yml`][error-prone-support-website-deploy-workflow] GitHub
Actions workflow any time a change is merged to `master`.

[error-prone-support-website]: https://error-prone.picnic.tech
[error-prone-support-website-deploy-workflow]: https://github.com/PicnicSupermarket/error-prone-support/actions/workflows/deploy-website.yml
[jekyll]: https://jekyllrb.com
[jekyll-docs]: https://jekyllrb.com/docs
[jekyll-docs-installation]: https://jekyllrb.com/docs/installation
[just-the-docs]: https://just-the-docs.github.io/just-the-docs/
[localhost-port-4000]: http://127.0.0.1:4000
[rvm]: https://rvm.io
