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

Read more on how Picnic uses Error Prone (Support) in the blog post [_Picnic
loves Error Prone: producing high-quality and consistent Java
code_][picnic-blog-ep-post].

[![Maven Central][maven-central-badge]][maven-central-search]
[![Reproducible Builds][reproducible-builds-badge]][reproducible-builds-report]
[![OpenSSF Best Practices][openssf-best-practices-badge]][openssf-best-practices-scorecard]
[![CodeQL Analysis][codeql-badge]][codeql-master]
[![GitHub Actions][github-actions-build-badge]][github-actions-build-master]
[![Mutation tested with PIT][pitest-badge]][pitest]
[![Quality Gate Status][sonarcloud-quality-badge]][sonarcloud-quality-master]
[![Maintainability Rating][sonarcloud-maintainability-badge]][sonarcloud-maintainability-master]
[![Reliability Rating][sonarcloud-reliability-badge]][sonarcloud-reliability-master]
[![Security Rating][sonarcloud-security-badge]][sonarcloud-security-master]
[![Coverage][sonarcloud-coverage-badge]][sonarcloud-coverage-master]
[![Duplicated Lines (%)][sonarcloud-duplication-badge]][sonarcloud-duplication-master]
[![Technical Debt][sonarcloud-tech-debt-badge]][sonarcloud-tech-debt-master]
[![License][license-badge]][license]
[![PRs Welcome][pr-badge]][contributing]

[Getting started](#-getting-started) •
[Developing Error Prone Support](#-developing-error-prone-support) •
[How it works](#-how-it-works) • [Contributing](#%EF%B8%8F-contributing)

---

## ⚡ Getting started

### Installation

This library is built on top of [Error Prone][error-prone-orig-repo]. To use
it, read the installation guide for Maven or Gradle below.

#### Maven

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
                           <!-- Error Prone Support's Refaster rules. -->
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

#### Gradle

1. First, follow the [installation guide]
   [error-prone-gradle-installation-guide] of the `gradle-errorprone-plugin`.
2. Next, edit your `build.gradle` file to add one or more Error Prone Support
   modules:

   ```groovy
   dependencies {
       // Error Prone itself.
       errorprone("com.google.errorprone:error_prone_core:${errorProneVersion}")
       // Error Prone Support's additional bug checkers.
       errorprone("tech.picnic.error-prone-support:error-prone-contrib:${errorProneSupportVersion}")
       // Error Prone Support's Refaster rules.
       errorprone("tech.picnic.error-prone-support:refaster-runner:${errorProneSupportVersion}")
   }

   tasks.withType(JavaCompile).configureEach {
       options.errorprone.disableWarningsInGeneratedCode = true
       // Add other Error Prone flags here. See:
       // - https://github.com/tbroyer/gradle-errorprone-plugin#configuration
       // - https://errorprone.info/docs/flags
   }
   ```

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
[INFO] Example.java:[9,34] [Refaster Rule] BigDecimalRules.BigDecimalZero: Refactoring opportunity
    (see https://error-prone.picnic.tech/refasterrules/BigDecimalRules#BigDecimalZero)
  Did you mean 'return BigDecimal.ZERO;'?
...
[WARNING] Example.java:[13,35] [IdentityConversion] This method invocation appears redundant; remove it or suppress this warning and add a comment explaining its purpose
    (see https://error-prone.picnic.tech/bugpatterns/IdentityConversion)
  Did you mean 'return set;' or '@SuppressWarnings("IdentityConversion") public ImmutableSet<Integer> getSet() {'?
...
```

Two things are kicking in here:

1. An Error Prone [`BugChecker`][error-prone-bugchecker] that flags unnecessary
   [identity conversions][bug-checks-identity-conversion].
2. A [Refaster][refaster] rule capable of
   [rewriting][refaster-rules-bigdecimal] expressions of the form
   `BigDecimal.valueOf(0)` and `new BigDecimal(0)` to `BigDecimal.ZERO`.

Be sure to check out all [bug checks][bug-checks] and [refaster
rules][refaster-rules].

## 👷 Developing Error Prone Support

This is a [Maven][maven] project, so running `mvn clean install` performs a
full clean build and installs the library to your local Maven repository.

Once you've made changes, the build may fail due to a warning or error emitted
by static code analysis. The flags and commands listed below allow you to
suppress or (in a large subset of cases) automatically fix such cases. Make
sure to carefully check the available options, as this can save you significant
amounts of development time!

Relevant Maven build parameters:

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

Other highly relevant commands:

- `mvn fmt:format` formats the code using
  [`google-java-format`][google-java-format].
- [`./run-full-build.sh`][script-run-full-build] builds the project twice,
  where the second pass validates compatbility with Picnic's [Error Prone
  fork][error-prone-fork-repo] and compliance of the code with any rules
  defined within this project. (Consider running this before [opening a pull
  request][contributing-pull-request], as the PR checks also perform this
  validation.)
- [`./apply-error-prone-suggestions.sh`][script-apply-error-prone-suggestions]
  applies Error Prone and Error Prone Support code suggestions to this project.
  Before running this command, make sure to have installed the project (`mvn
  clean install`) and make sure that the current working directory does not
  contain unstaged or uncommited changes.
- [`./run-mutation-tests.sh`][script-run-mutation-tests] runs mutation tests
  using [Pitest][pitest]. The results can be reviewed by opening the respective
  `target/pit-reports/index.html` files. For more information check the [PIT
  Maven plugin][pitest-maven].

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

### Security

If you want to report a security vulnerablity, please do so through a private
channel; please see our [security policy][security] for details.

[bug-checks]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/bugpatterns/
[bug-checks-identity-conversion]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/bugpatterns/IdentityConversion.java
[codeql-badge]: https://github.com/PicnicSupermarket/error-prone-support/actions/workflows/codeql.yml/badge.svg?branch=master&event=push
[codeql-master]: https://github.com/PicnicSupermarket/error-prone-support/actions/workflows/codeql.yml?query=branch:master+event:push
[contributing]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/CONTRIBUTING.md
[contributing-pull-request]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/CONTRIBUTING.md#-opening-a-pull-request
[error-prone-bugchecker]: https://github.com/google/error-prone/blob/master/check_api/src/main/java/com/google/errorprone/bugpatterns/BugChecker.java
[error-prone-fork-jitpack]: https://jitpack.io/#PicnicSupermarket/error-prone
[error-prone-fork-repo]: https://github.com/PicnicSupermarket/error-prone
[error-prone-gradle-installation-guide]: https://github.com/tbroyer/gradle-errorprone-plugin
[error-prone-installation-guide]: https://errorprone.info/docs/installation#maven
[error-prone-orig-repo]: https://github.com/google/error-prone
[error-prone-pull-3301]: https://github.com/google/error-prone/pull/3301
[github-actions-build-badge]: https://github.com/PicnicSupermarket/error-prone-support/actions/workflows/build.yaml/badge.svg
[github-actions-build-master]: https://github.com/PicnicSupermarket/error-prone-support/actions/workflows/build.yaml?query=branch:master&event=push
[google-java-format]: https://github.com/google/google-java-format
[idea-288052]: https://youtrack.jetbrains.com/issue/IDEA-288052
[license-badge]: https://img.shields.io/github/license/PicnicSupermarket/error-prone-support
[license]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/LICENSE.md
[maven-central-badge]: https://img.shields.io/maven-central/v/tech.picnic.error-prone-support/error-prone-support?color=blue
[maven-central-search]: https://search.maven.org/artifact/tech.picnic.error-prone-support/error-prone-support
[maven]: https://maven.apache.org
[openssf-best-practices-badge]: https://bestpractices.coreinfrastructure.org/projects/7199/badge
[openssf-best-practices-scorecard]: https://bestpractices.coreinfrastructure.org/projects/7199
[picnic-blog-ep-post]: https://blog.picnic.nl/picnic-loves-error-prone-producing-high-quality-and-consistent-java-code-b8a566be6886
[picnic-blog]: https://blog.picnic.nl
[pitest-badge]: https://img.shields.io/badge/-Mutation%20tested%20with%20PIT-blue.svg
[pitest]: https://pitest.org
[pitest-maven]: https://pitest.org/quickstart/maven
[pr-badge]: https://img.shields.io/badge/PRs-welcome-brightgreen.svg
[refaster]: https://errorprone.info/docs/refaster
[refaster-rules-bigdecimal]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/refasterrules/BigDecimalRules.java
[refaster-rules]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/refasterrules/
[reproducible-builds-badge]: https://img.shields.io/badge/Reproducible_Builds-ok-success?labelColor=1e5b96
[reproducible-builds-report]: https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/tech/picnic/error-prone-support/error-prone-support/README.md
[script-apply-error-prone-suggestions]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/apply-error-prone-suggestions.sh
[script-run-full-build]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/run-full-build.sh
[script-run-mutation-tests]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/run-mutation-tests.sh
[security]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/SECURITY.md
[sonarcloud-coverage-badge]: https://sonarcloud.io/api/project_badges/measure?project=PicnicSupermarket_error-prone-support&metric=coverage
[sonarcloud-coverage-master]: https://sonarcloud.io/component_measures?id=PicnicSupermarket_error-prone-support&metric=coverage
[sonarcloud-duplication-badge]: https://sonarcloud.io/api/project_badges/measure?project=PicnicSupermarket_error-prone-support&metric=duplicated_lines_density
[sonarcloud-duplication-master]: https://sonarcloud.io/component_measures?id=PicnicSupermarket_error-prone-support&metric=duplicated_lines_density
[sonarcloud-maintainability-badge]: https://sonarcloud.io/api/project_badges/measure?project=PicnicSupermarket_error-prone-support&metric=sqale_rating
[sonarcloud-maintainability-master]: https://sonarcloud.io/component_measures?id=PicnicSupermarket_error-prone-support&metric=sqale_rating
[sonarcloud-quality-badge]: https://sonarcloud.io/api/project_badges/measure?project=PicnicSupermarket_error-prone-support&metric=alert_status
[sonarcloud-quality-master]: https://sonarcloud.io/summary/new_code?id=PicnicSupermarket_error-prone-support
[sonarcloud-reliability-badge]: https://sonarcloud.io/api/project_badges/measure?project=PicnicSupermarket_error-prone-support&metric=reliability_rating
[sonarcloud-reliability-master]: https://sonarcloud.io/component_measures?id=PicnicSupermarket_error-prone-support&metric=reliability_rating
[sonarcloud-security-badge]: https://sonarcloud.io/api/project_badges/measure?project=PicnicSupermarket_error-prone-support&metric=security_rating
[sonarcloud-security-master]: https://sonarcloud.io/component_measures?id=PicnicSupermarket_error-prone-support&metric=security_rating
[sonarcloud-tech-debt-badge]: https://sonarcloud.io/api/project_badges/measure?project=PicnicSupermarket_error-prone-support&metric=sqale_index
[sonarcloud-tech-debt-master]: https://sonarcloud.io/component_measures?id=PicnicSupermarket_error-prone-support&metric=sqale_index
