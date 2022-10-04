<div align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="website/assets/images/logo-dark.svg">
    <source media="(prefers-color-scheme: light)" srcset="website/assets/images/logo.svg">
    <img alt="Error Prone Support logo" src="website/assets/images/logo.svg" width="50%">
  </picture>
</div>

# Error Prone Support

Error Prone Support is a [Picnic][picnic-blog]-opinionated extension of
Google's [Error Prone][error-prone-orig-repo]. It aims to improve code quality,
focussing on maintainability, consistency and avoidance of common pitfalls.

> Error Prone is a static analysis tool for Java that catches common
> programming mistakes at compile-time.

[![Maven Central][maven-central-badge]][maven-central-search]
[![GitHub Actions][github-actions-build-badge]][github-actions-build-master]
[![License][license-badge]][license]
[![PRs Welcome][pr-badge]][contributing]

[Getting started](#-getting-started) •
[Developing Error Prone Support](#-developing-error-prone-support) •
[How it works](#-how-it-works) • [Contributing](#%EF%B8%8F-contributing)

---

## ⚡ Getting started

### Installation

This library is built on top of [Error Prone][error-prone-orig-repo]. To use
it:

1. First, follow Error Prone's [installation
   guide][error-prone-installation-guide].
2. Next, edit your `pom.xml` file to add one or more Error Prone Support
   modules to the `annotationProcessorPaths` of the `maven-compiler-plugin`:

   ```xml
   <build>
       <pluginManagement>
           <plugins>
               <plugin>
                   <groupId>org.apache.maven.plugins</groupId>
                   <artifactId>maven-compiler-plugin</artifactId>
                   <configuration>
                       <annotationProcessorPaths>
                           <!-- Error Prone itself. -->
                           <path>
                               <groupId>com.google.errorprone</groupId>
                               <artifactId>error_prone_core</artifactId>
                               <version>${error-prone.version}</version>
                           </path>
                           <!-- Error Prone Support's additional bug checkers. -->
                           <path>
                               <groupId>tech.picnic.error-prone-support</groupId>
                               <artifactId>error-prone-contrib</artifactId>
                               <version>${error-prone-support.version}</version>
                           </path>
                           <!-- Error Prone Support's Refaster templates. -->
                           <path>
                               <groupId>tech.picnic.error-prone-support</groupId>
                               <artifactId>refaster-runner</artifactId>
                               <version>${error-prone-support.version}</version>
                           </path>
                       </annotationProcessorPaths>
                       <compilerArgs>
                           <arg>
                               -Xplugin:ErrorProne
                               <!-- Add other Error Prone flags here. See
                               https://errorprone.info/docs/flags. -->
                           </arg>
                           <arg>-XDcompilePolicy=simple</arg>
                       </compilerArgs>
                       <!-- Some checks raise warnings rather than errors. -->
                       <showWarnings>true</showWarnings>
                       <!-- Enable this if you'd like to fail your build upon warnings. -->
                       <!-- <failOnWarning>true</failOnWarning> -->
                   </configuration>
               </plugin>
           </plugins>
       </pluginManagement>
   </build>
   ```

<!-- XXX: Reference `oss-parent`'s `pom.xml` once that project also uses Error
Prone Support. Alternatively reference this project's `self-check` profile
definition. -->

### Seeing it in action

Consider the following example code:

```java
import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;

public class Example {
  static BigDecimal getNumber() {
    return BigDecimal.valueOf(0);
  }

  public ImmutableSet<Integer> getSet() {
    ImmutableSet<Integer> set = ImmutableSet.of(1);
    return ImmutableSet.copyOf(set);
  }
}
```

If the [installation](#installation) was successful, then building the above
code with Maven should yield two compiler warnings:

```sh
$ mvn clean install
...
[INFO] -------------------------------------------------------------
[WARNING] COMPILATION WARNING :
[INFO] -------------------------------------------------------------
[WARNING] Example.java:[9,34] [tech.picnic.errorprone.refastertemplates.BigDecimalTemplates.BigDecimalZero]
  Did you mean 'return BigDecimal.ZERO;'?
[WARNING] Example.java:[13,35] [IdentityConversion] This method invocation appears redundant; remove it or suppress this warning and add a comment explaining its purpose
    (see https://error-prone.picnic.tech/bugpatterns/IdentityConversion)
  Did you mean 'return set;' or '@SuppressWarnings("IdentityConversion") public ImmutableSet<Integer> getSet() {'?
[INFO] 2 warnings
[INFO] -------------------------------------------------------------
...
```

Two things are kicking in here:

1. An Error Prone [`BugChecker`][error-prone-bugchecker] that flags unnecessary
   [identity conversions][bug-checks-identity-conversion].
2. A [Refaster][refaster] template capable of
   [rewriting][refaster-templates-bigdecimal] expressions of the form
   `BigDecimal.valueOf(0)` and `new BigDecimal(0)` to `BigDecimal.ZERO`.

Be sure to check out all [bug checks][bug-checks] and [refaster
templates][refaster-templates].

## 👷 Developing Error Prone Support

This is a [Maven][maven] project, so running `mvn clean install` performs a
full clean build and installs the library to your local Maven repository. Some
relevant flags:

- `-Dverification.warn` makes the warnings and errors emitted by various
  plugins and the Java compiler non-fatal, where possible.
- `-Dverification.skip` disables various non-essential plugins and compiles the
  code with minimal checks (i.e. without linting, Error Prone checks, etc.).
- `-Dversion.error-prone=some-version` runs the build using the specified
  version of Error Prone. This is useful e.g. when testing a locally built
  Error Prone SNAPSHOT.
- `-Perror-prone-fork` runs the build using Picnic's [Error Prone
  fork][error-prone-fork-repo], hosted on [Jitpack][error-prone-fork-jitpack].
  This fork generally contains a few changes on top of the latest Error Prone
  release.
- `-Pself-check` runs the checks defined by this project against itself.
  Pending a release of [google/error-prone#3301][error-prone-pull-3301], this
  flag must currently be used in combination with `-Perror-prone-fork`.

Some other commands one may find relevant:

- `mvn fmt:format` formats the code using
  [`google-java-format`][google-java-format].
- `./run-mutation-tests.sh` runs mutation tests using [PIT][pitest]. The
  results can be reviewed by opening the respective
  `target/pit-reports/index.html` files. For more information check the [PIT
  Maven plugin][pitest-maven].
- `./apply-error-prone-suggestions.sh` applies Error Prone and Error Prone
  Support code suggestions to this project. Before running this command, make
  sure to have installed the project (`mvn clean install`) and make sure that
  the current working directory does not contain unstaged or uncommited
  changes.

When running the project's tests in IntelliJ IDEA, you might see the following
error:

```
java: exporting a package from system module jdk.compiler is not allowed with --release
```

If this happens, go to _Settings -> Build, Execution, Deployment -> Compiler ->
Java Compiler_ and deselect the option _Use '--release' option for
cross-compilation (Java 9 and later)_. See [IDEA-288052][idea-288052] for
details.

## 💡 How it works

This project provides additional [`BugChecker`][error-prone-bugchecker]
implementations.

<!-- XXX: Extend this section. -->

## ✍️ Contributing

Want to report or fix a bug, suggest or add a new feature, or improve the
documentation? That's awesome! Please read our [contribution
guidelines][contributing].

[bug-checks]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/bugpatterns/
[bug-checks-identity-conversion]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/bugpatterns/IdentityConversion.java
[contributing]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/CONTRIBUTING.md
[error-prone-bugchecker]: https://github.com/google/error-prone/blob/master/check_api/src/main/java/com/google/errorprone/bugpatterns/BugChecker.java
[error-prone-fork-jitpack]: https://jitpack.io/#PicnicSupermarket/error-prone
[error-prone-fork-repo]: https://github.com/PicnicSupermarket/error-prone
[error-prone-installation-guide]: https://errorprone.info/docs/installation#maven
[error-prone-orig-repo]: https://github.com/google/error-prone
[error-prone-pull-3301]: https://github.com/google/error-prone/pull/3301
[github-actions-build-badge]: https://github.com/PicnicSupermarket/error-prone-support/actions/workflows/build.yaml/badge.svg
[github-actions-build-master]: https://github.com/PicnicSupermarket/error-prone-support/actions/workflows/build.yaml?query=branch%3Amaster
[google-java-format]: https://github.com/google/google-java-format
[idea-288052]: https://youtrack.jetbrains.com/issue/IDEA-288052
[license-badge]: https://img.shields.io/github/license/PicnicSupermarket/error-prone-support
[license]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/LICENSE.md
[maven-central-badge]: https://img.shields.io/maven-central/v/tech.picnic.error-prone-support/error-prone-support?color=blue
[maven-central-search]: https://search.maven.org/artifact/tech.picnic.error-prone-support/error-prone-support
[maven]: https://maven.apache.org
[picnic-blog]: https://blog.picnic.nl
[pitest]: https://pitest.org
[pitest-maven]: https://pitest.org/quickstart/maven
[pr-badge]: https://img.shields.io/badge/PRs-welcome-brightgreen.svg
[refaster]: https://errorprone.info/docs/refaster
[refaster-templates-bigdecimal]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/refastertemplates/BigDecimalTemplates.java
[refaster-templates]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/refastertemplates/
