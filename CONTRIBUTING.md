# Contributing

Thank you for checking this document! This project is free software, and we
(the maintainers) encourage and value any contribution.

Here are some guidelines to help you get started.

## 🐛 Reporting a bug

Like any non-trivial piece of software, this library is probably not bug-free.
If you found a bug, feel free to [report the issue][error-prone-support-issues]
on GitHub.

Before doing so, please:
- Verify that the issue is reproducible against the latest version of the
  project.
- Search through the existing set of issues to see whether the problem is
  already known. With some luck a solution is already in place, or a workaround
  may have been provided.

When filing a bug report, please include the following:
- Any relevant information about your environment. This should generally
  include the output of `java -version`, as well as the version of Error Prone
  you're using.
- A description of what is going on (e.g. logging output, stacktraces).
- A minimum reproducible example, so that other developers can try to reproduce
  (and optionally fix) the bug.
- Any additional information that may be relevant.

## 💡 Reporting an improvement

If you would like to see an improvement, you can file a [GitHub
issue][error-prone-support-issues]. This is also a good idea when you're
already working towards opening a pull request, as this allows for discussion
around the idea.

## 🚀 Opening a pull request

All submissions, including submissions by project members, require approval by
at least two reviewers. We use [GitHub pull
requests][error-prone-support-pulls] for this purpose.

Before opening a pull request, please check whether there are any existing
(open or closed) issues or pull requests addressing the same problem. This
avoids double work or lots of time spent on a solution that may ultimately not
be accepted. When in doubt, make sure to first raise an
[issue][error-prone-support-issues] to discuss the idea.

To the extent possible, the pull request process guards our coding guidelines.
Some pointers:
- Checks should be _topical_: ideally they address a single concern.
- Where possible checks should provide _fixes_, and ideally these are
  completely behavior-preserving. In order for a check to be adopted by users
  it must not "get in the way". So for a check that addresses a relatively
  trivial stylistic concern it is doubly important that the violations it
  detects can be auto-patched.
- Make sure you have read Error Prone's [criteria for new
  checks][error-prone-criteria]. Most guidelines described there apply to this
  project as well, except that this project _does_ focus quite heavy on style
  enforcement. But that just makes the previous point doubly important.
- Make sure that a check's [(mutation) test
  coverage][error-prone-support-mutation-tests] is or remains about as high as
  it can be. Not only does this lead to better tests, it also points out
  opportunities to simplify the code.
- Please restrict the scope of a pull request to a single feature or fix. Don't
  sneak in unrelated changes; instead just open more than one pull request 😉.

[error-prone-criteria]: https://errorprone.info/docs/criteria
[error-prone-support-issues]: https://github.com/PicnicSupermarket/error-prone-support/issues
[error-prone-support-mutation-tests]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/run-mutation-tests.sh
[error-prone-support-pulls]: https://github.com/PicnicSupermarket/error-prone-support/pulls
