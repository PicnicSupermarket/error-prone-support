<div align="center">

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="logo-dark.svg">
  <source media="(prefers-color-scheme: light)" srcset="logo.svg">
  <img alt="Error Prone Support logo'" src="logo.svg" width="50%">
</picture>

# Error Prone Support

Error Prone Support is a Picnic-opinionated extension of [Error Prone][error-prone], a static analysis tool for Java that catches common programming mistakes at compile-time.

[![Maven central][maven-badge]][maven-eps]
![GitHub Actions][ci-badge]
[![Licence][licence-badge]][licence]
[![PRs Welcome][pr-badge]][contribute]

[Getting started](#⚡️-getting-started) •
[How it works](#💡-how-it-works) •
[Building](#👷-building) •
[Contribute](#✍️-contribute)

</div>

---

## ⚡️ Getting started

Edit your `pom.xml` file to add Error Prone Support to your project.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>tech.picnic.error-prone-support</groupId>
                        <artifactId>error-prone-support</artifactId>
                        <version>0.1.0</version>
                    </path>
                </annotationProcessorPaths>
                <compilerArgs>
                    <!-- Enable and configure Error Prone. -->
                    <arg>
                        -Xplugin:ErrorProne
                        <!-- Other Error Prone flags, see
                        https://errorprone.info/docs/flags. -->
                    </arg>
                    <!-- The Error Prone plugin makes certain
                    assumptions about the state of the AST at the
                    moment it is invoked. Those assumptions are met
                    when using the `simple` compile policy. This
                    flag may be dropped after resolution of
                    https://bugs.openjdk.java.net/browse/JDK-8155674. -->
                    <arg>-XDcompilePolicy=simple</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## 👷 Building

This is a [Maven][maven] project, so running `mvn clean install` performs a
full clean build. Some relevant flags:

- `-Dverification.warn` makes the warnings and errors emitted by various plugins and the Java compiler non-fatal, where possible.
- `-Dverification.skip` disables various non-essential plugins and compiles the code with minimal checks (i.e. without linting, Error Prone checks, etc.)
- `-Dversion.error-prone=some-version` runs the build using the specified version of Error Prone. This is useful e.g. when testing a locally built Error Prone SNAPSHOT.
- `-Perror-prone-fork` run the build using Picnic's [Error Prone fork][error-prone-fork-repo], hosted on [Jitpack][error-prone-fork-jitpack]. This fork generally contains a few changes on top of the latest Error Prone release.

## 💡 How it works

Extending [google/error-prone][error-prone].

## ✍️ Contribute

Want to fix a bug, improve the docs, or add a new feature? That's awesome! Please read the [contributing document][contribute].

[ci-badge]: https://github.com/PicnicSupermarket/error-prone-support/actions/workflows/build.yaml/badge.svg
[contribute]: CONTRIBUTING.md
[error-prone]: https://github.com/google/error-prone
[error-prone-fork-repo]: https://github.com/PicnicSupermarket/error-prone
[error-prone-fork-jitpack]: https://jitpack.io/#PicnicSupermarket/error-prone
[licence]: LICENCE
[licence-badge]: https://img.shields.io/github/license/PicnicSupermarket/error-prone-support
[maven]: https://maven.apache.org
[maven-eps]: https://search.maven.org/artifact/tech.picnic.error-prone-support/error-prone-support
[maven-badge]: https://img.shields.io/maven-central/v/tech.picnic.error-prone-support/error-prone-support?color=blue
[pr-badge]: https://img.shields.io/badge/PRs-welcome-brightgreen.svg
