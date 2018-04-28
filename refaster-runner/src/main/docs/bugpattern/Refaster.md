Error Prone's out-of-the-box support for the application of
[Refaster][refaster] templates is somewhat cumbersome. Additionally, by default
the focus of Refaster templates is on one-off code refactorings.

This plugin attempts to bring Refaster templates on equal footing with other
Error Prone plugins by locating all Refaster templates on the classpath and
reporing any match. The suggested changes can be applied using Error Prone's
built-in [patch][patching] functionality.

XXX: Expand documentation. Mention:
- The `refaster-resource-compiler`.
- How checks can be restricted using the `NamePattern` flag (see the Javadoc)
- An concrete patching example, with and without `NamePattern`.

[refaster]: https://errorprone.info/docs/refaster
[patching]: https://errorprone.info/docs/patching
