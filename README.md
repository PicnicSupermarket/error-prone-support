<div align="center">

<img src="logo.svg" alt="Error Prone Support logo" width="50%"/>

# Error Prone Support

Error Prone is a static analysis tool for Java that catches common programming mistakes at compile-time.

![GitHub Actions][ci-badge]
[![Maven central][maven-badge]][releases]

[Getting started](#⚡️-getting-started) •
[How it works](#💡-how-it-works) •
[Contribute](#✍️-contribute)

</div>

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

## 💡 How it works
Extending [google/error-prone][error-prone].
## ✍️ Contribute
TBD

[ci-badge]: https://github.com/PicnicSupermarket/error-prone-support/actions/workflows/build.yaml/badge.svg
[error-prone]: https://github.com/google/error-prone
[releases]: https://search.maven.org/artifact/tech.picnic.error-prone-support/error-prone-support
[maven-badge]: https://img.shields.io/maven-central/v/tech.picnic.error-prone-support/error-prone-support?color=blue
