# Contributing

Thank you for checking this document! This framework is free software, and we (the maintainers) encourage and value any contribution.

Here are some guidelines to help you get started.

## 🐛 Report a bug

Like any non-trivial piece of software, this framework is probably not bug-free. If you found a bug, feel free to report it to us via GitHub, in the Issues section.

Before doing so, try to search for your bug in the already existing ones - maybe it is already known, and maybe there's already a solution in place.

Be sure to use the latest version of the framework, and to provide in your bug report:

- Information about your environment (at the very least, operating system and Java version).
- Description of what is going on (e.g. logging output, stacktraces).
- A mininum reproducible example, so that other developers can try to reproduce the bug, and fix it.
- Any additional information that you deem necessary.

## 💡 Report an improvement

If you would like to see an improvement, you can file a GitHub issue. Also when you're already working towards opening up a Pull Request, this allows for discussion around the idea.

## 🚀 Open a Pull Request

All submissions, including submissions by project members, require at least two reviews. We use GitHub Pull Requests for this purpose.

Before opening a Pull Request, please check if there is not already one open that aims to solve same problem.

To the extend possible, the pull request process guards our coding guidelines. Some pointers:

- Checks should we topical: Ideally they address a single concern.
- Where possible checks should provide fixes, and ideally these are completely behavior preserving. In order for a check to be adopted by users it must not "get in the way". So for a check which addresses a relatively trivial stylistic concern it is doubly important that the violations it detects can be auto-patched.
- Make sure you have read Error Prone's criteria for new checks. Most guidelines described there apply to this project as well, except that this project does focus quite heavy on style enforcement. But that just makes the previous point doubly important.
- Make sure that a check's (mutation) coverage is or remains about as high as it can be. Not only does this lead to better tests, it also points out opportunities to simplify the code.
- Please restrict the scope of a pull request to a single feature or fix. Don't sneak in unrelated changes.
- When in doubt about whether a pull request will be accepted, please first file an issue to discuss it.
