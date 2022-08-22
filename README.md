<div align="center">

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="logo-dark.svg">
  <source media="(prefers-color-scheme: light)" srcset="logo.svg">
  <img alt="Error Prone Support logo'" src="logo.svg" width="50%">
</picture>

# Error Prone Support

Error Prone Support is a Picnic-opinionated extension of [Error Prone][error-prone-repo], a static analysis tool for Java that catches common programming mistakes at compile-time.

[![Maven central][maven-badge]][maven-eps]
![GitHub Actions][ci-badge]
[![Licence][licence-badge]][licence]
[![PRs Welcome][pr-badge]][contribute]

[Getting started](#%EF%B8%8F-getting-started) •
[How it works](#-how-it-works) •
[Building](#-building) •
[Contribute](#%EF%B8%8F-contribute)

</div>

---

## ⚡ Getting started

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

Two other goals that one may find relevant:

- `mvn fmt:format` formats the code using [`google-java-format`][google-java-format].
- `mvn pitest:mutationCoverage` runs mutation tests using [PIT][pitest]. The results can be reviewed by opening the respective `target/pit-reports/index.html` files. For more information check the [PIT Maven plugin][pitest-maven].

When loading the project in IntelliJ IDEA (and perhaps other IDEs) errors about the inaccessibility of `com.sun.tools.javac.*` classes may be reported. If this happens, configure your IDE to enable the `add-exports` profile.

## 💡 How it works

Extending [@google/error-prone][error-prone-repo].

## ✍️ Contribute

Want to fix a bug, improve the docs, or add a new feature? That's awesome! Please read the [contributing document][contribute].

[ci-badge]: https://github.com/PicnicSupermarket/error-prone-support/actions/workflows/build.yaml/badge.svg
[contribute]: CONTRIBUTING.md
[error-prone-fork-jitpack]: https://jitpack.io/#PicnicSupermarket/error-prone
[error-prone-fork-repo]: https://github.com/PicnicSupermarket/error-prone
[error-prone-repo]: https://github.com/google/error-prone
[google-java-format]: https://github.com/google/google-java-format
[licence-badge]: https://img.shields.io/github/license/PicnicSupermarket/error-prone-support
[licence]: LICENSE.md
[maven-badge]: https://img.shields.io/maven-central/v/tech.picnic.error-prone-support/error-prone-support?color=blue
[maven-eps]: https://search.maven.org/artifact/tech.picnic.error-prone-support/error-prone-support
[maven]: https://maven.apache.org
[pitest-maven]: https://pitest.org/quickstart/maven
[pitest]: https://pitest.org
[pr-badge]: https://img.shields.io/badge/PRs-welcome-brightgreen.svg
