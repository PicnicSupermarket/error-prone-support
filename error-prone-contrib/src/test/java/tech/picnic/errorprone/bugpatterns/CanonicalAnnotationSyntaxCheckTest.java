package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class CanonicalAnnotationSyntaxCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(CanonicalAnnotationSyntaxCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(CanonicalAnnotationSyntaxCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "pkg/A.java",
            "package pkg;",
            "",
            "import pkg.A.Foo;",
            "",
            "interface A {",
            "  @interface Foo {",
            "    int[] value() default {};",
            "    int[] value2() default {};",
            "  }",
            "",
            "  @pkg.A.Foo A minimal1();",
            "  @A.Foo A minimal2();",
            "  @Foo A minimal3();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @pkg.A.Foo() A functional1();",
            "  // BUG: Diagnostic contains:",
            "  @A.Foo() A functional2();",
            "  // BUG: Diagnostic contains:",
            "  @Foo() A functional3();",
            "",
            "  @pkg.A.Foo(1) A simple1();",
            "  @A.Foo(1) A simple2();",
            "  @Foo(1) A simple3();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @pkg.A.Foo({1}) A singleton1();",
            "  // BUG: Diagnostic contains:",
            "  @A.Foo({1}) A singleton2();",
            "  // BUG: Diagnostic contains:",
            "  @Foo({1}) A singleton3();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @pkg.A.Foo(value = 1) A verbose1();",
            "  // BUG: Diagnostic contains:",
            "  @A.Foo(value = 1) A verbose2();",
            "  // BUG: Diagnostic contains:",
            "  @Foo(value = 1) A verbose3();",
            "",
            "  @pkg.A.Foo(value2 = 2) A custom1();",
            "  @A.Foo(value2 = 2) A custom2();",
            "  @Foo(value2 = 2) A custom3();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @pkg.A.Foo(value2 = {2}) A customSingleton1();",
            "  // BUG: Diagnostic contains:",
            "  @A.Foo(value2 = {2}) A customSingleton2();",
            "  // BUG: Diagnostic contains:",
            "  @Foo(value2 = {2}) A customSingleton3();",
            "",
            "  @pkg.A.Foo(value2 = {2, 2}) A customPair1();",
            "  @A.Foo(value2 = {2, 2}) A customPair2();",
            "  @Foo(value2 = {2, 2}) A customPair3();",
            "",
            "  @pkg.A.Foo(value = 1, value2 = 2) A extended1();",
            "  @A.Foo(value = 1, value2 = 2) A extended2();",
            "  @Foo(value = 1, value2 = 2) A extended3();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @pkg.A.Foo({1, 1,}) A trailingComma1();",
            "  // BUG: Diagnostic contains:",
            "  @A.Foo({1, 1,}) A trailingComma2();",
            "  // BUG: Diagnostic contains:",
            "  @Foo({1, 1,}) A trailingComma3();",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "in/pkg/A.java",
            "package pkg;",
            "",
            "import pkg.A.Foo;",
            "",
            "interface A {",
            "  @interface Foo {",
            "    String[] value() default {};",
            "    int[] value2() default {};",
            "  }",
            "",
            "  @pkg.A.Foo() A functional1();",
            "  @A.Foo() A functional2();",
            "  @Foo() A functional3();",
            "",
            "  @pkg.A.Foo(value = \"foo\") A verbose1();",
            "  @A.Foo(value = \"a'b\") A verbose2();",
            "  @Foo(value = \"a\" + \"\\nb\") A verbose3();",
            "",
            "  @pkg.A.Foo(value = {\"foo\"}) A moreVerbose1();",
            "  @A.Foo(value = {\"a'b\"}) A moreVerbose2();",
            "  @Foo(value = {\"a\" + \"\\nb\"}) A moreVerbose3();",
            "",
            "  @pkg.A.Foo(value = {\"foo\", \"bar\"}, value2 = {2}) A extended1();",
            "  @A.Foo(value = {\"a'b\", \"c'd\"}, value2 = {2}) A extended2();",
            "  @Foo(value = {\"a\" + \"\\nb\", \"c\" + \"\\nd\"}, value2 = {2}) A extended3();",
            "",
            "  @pkg.A.Foo({\"foo\", \"bar\",}) A trailingComma1();",
            "  @A.Foo({\"a'b\", \"c'd\",}) A trailingComma2();",
            "  @Foo({\"a\" + \"\\nb\", \"c\" + \"\\nd\",}) A trailingComma3();",
            "}")
        .addOutputLines(
            "out/pkg/A.java",
            "package pkg;",
            "",
            "import pkg.A.Foo;",
            "",
            "interface A {",
            "  @interface Foo {",
            "    String[] value() default {};",
            "    int[] value2() default {};",
            "  }",
            "",
            "  @pkg.A.Foo A functional1();",
            "  @A.Foo A functional2();",
            "  @Foo A functional3();",
            "",
            "  @pkg.A.Foo(\"foo\") A verbose1();",
            "  @A.Foo(\"a'b\") A verbose2();",
            "  @Foo(\"a\" + \"\\nb\") A verbose3();",
            "",
            "  @pkg.A.Foo(\"foo\") A moreVerbose1();",
            "  @A.Foo(\"a'b\") A moreVerbose2();",
            "  @Foo(\"a\" + \"\\nb\") A moreVerbose3();",
            "",
            "  @pkg.A.Foo(value = {\"foo\", \"bar\"}, value2 = 2) A extended1();",
            "  @A.Foo(value = {\"a'b\", \"c'd\"}, value2 = 2) A extended2();",
            "  @Foo(value = {\"a\" + \"\\nb\", \"c\" + \"\\nd\"}, value2 = 2) A extended3();",
            "",
            "  @pkg.A.Foo({\"foo\", \"bar\"}) A trailingComma1();",
            "  @A.Foo({\"a'b\", \"c'd\"}) A trailingComma2();",
            "  @Foo({\"a\" + \"\\nb\", \"c\" + \"\\nd\"}) A trailingComma3();",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
