package com.picnicinternational.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CanonicalAnnotationSyntaxCheckTest {
    private final CompilationTestHelper compilationTestHelper =
            CompilationTestHelper.newInstance(CanonicalAnnotationSyntaxCheck.class, getClass());
    private final BugCheckerRefactoringTestHelper refactoringTestHelper =
            BugCheckerRefactoringTestHelper.newInstance(
                    new CanonicalAnnotationSyntaxCheck(), getClass());

    @Test
    public void testIdentification() {
        compilationTestHelper
                .addSourceLines(
                        "pkg/A.java",
                        "package pkg;",
                        "",
                        "import pkg.A.Foo;",
                        "",
                        "@Foo",
                        "interface A {",
                        "  @interface Foo {",
                        "    int value() default 0;",
                        "    int value2() default 0;",
                        "  }",
                        "",
                        "  @pkg.A.Foo Object minimal1();",
                        "  @A.Foo Object minimal2();",
                        "  @Foo Object minimal3();",
                        "",
                        "  // BUG: Diagnostic contains:",
                        "  @pkg.A.Foo() Object functional1();",
                        "  // BUG: Diagnostic contains:",
                        "  @A.Foo() Object functional2();",
                        "  // BUG: Diagnostic contains:",
                        "  @Foo() Object functional3();",
                        "",
                        "  @pkg.A.Foo(1) Object simple1();",
                        "  @A.Foo(1) Object simple2();",
                        "  @Foo(1) Object simple3();",
                        "",
                        "  // BUG: Diagnostic contains:",
                        "  @pkg.A.Foo(value = 1) Object verbose1();",
                        "  // BUG: Diagnostic contains:",
                        "  @A.Foo(value = 1) Object verbose2();",
                        "  // BUG: Diagnostic contains:",
                        "  @Foo(value = 1) Object verbose3();",
                        "",
                        "  @pkg.A.Foo(value2 = 2) Object custom1();",
                        "  @A.Foo(value2 = 2) Object custom2();",
                        "  @Foo(value2 = 2) Object custom3();",
                        "",
                        "  @pkg.A.Foo(value = 1, value2 = 2) Object extended1();",
                        "  @A.Foo(value = 1, value2 = 2) Object extended2();",
                        "  @Foo(value = 1, value2 = 2) Object extended3();",
                        "}")
                .doTest();
    }

    @Test
    public void testReplacement() throws IOException {
        refactoringTestHelper
                .addInputLines(
                        "in/pkg/A.java",
                        "package pkg;",
                        "",
                        "import pkg.A.Foo;",
                        "",
                        "interface A {",
                        "  @interface Foo {",
                        "    int value() default 0;",
                        "    int value2() default 0;",
                        "  }",
                        "",
                        "  @pkg.A.Foo() Object functional1();",
                        "  @A.Foo() Object functional2();",
                        "  @Foo() Object functional3();",
                        "",
                        "  @pkg.A.Foo(value = 1) Object verbose1();",
                        "  @A.Foo(value = 1) Object verbose2();",
                        "  @Foo(value = 1) Object verbose3();",
                        "}")
                .addOutputLines(
                        "out/pkg/A.java",
                        "package pkg;",
                        "",
                        "import pkg.A.Foo;",
                        "",
                        "interface A {",
                        "  @interface Foo {",
                        "    int value() default 0;",
                        "    int value2() default 0;",
                        "  }",
                        "",
                        "  @pkg.A.Foo Object functional1();",
                        "  @A.Foo Object functional2();",
                        "  @Foo Object functional3();",
                        "",
                        "  @pkg.A.Foo(1) Object verbose1();",
                        "  @A.Foo(1) Object verbose2();",
                        "  @Foo(1) Object verbose3();",
                        "}")
                .doTest();
    }
}
