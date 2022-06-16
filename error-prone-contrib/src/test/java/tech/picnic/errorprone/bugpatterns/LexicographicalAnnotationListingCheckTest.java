package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Predicates.containsPattern;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class LexicographicalAnnotationListingCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(LexicographicalAnnotationListingCheck.class, getClass())
          .expectErrorMessage(
              "X", containsPattern("Sort annotations lexicographically where possible"));
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(
          LexicographicalAnnotationListingCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import java.lang.annotation.Repeatable;",
            "",
            "interface A {",
            "  @Repeatable(Foos.class)",
            "  @interface Foo {",
            "    String[] value() default {};",
            "    int[] ints() default {};",
            "    Bar[] anns() default {};",
            "  }",
            "",
            "  @interface Bar {",
            "    String[] value() default {};",
            "  }",
            "",
            "  @interface Baz {",
            "    String[] str() default {};",
            "  }",
            "",
            "  @interface Foos {",
            "     Foo[] value();",
            "  }",
            "",
            "  // BUG: Diagnostic matches: X",
            "  @Foo @Bar A unsortedSimpleCase();",
            "  // BUG: Diagnostic matches: X",
            "  @Foo() @Bar() A unsortedWithParens();",
            "  @Foo() A onlyOneAnnotation();",
            "  @Bar @Foo() A sortedAnnotationsOneWithParens();",
            "",
            "  // BUG: Diagnostic matches: X",
            "  @Foo @Baz @Bar A threeUnsortedAnnotationsSameInitialLetter();",
            "  // BUG: Diagnostic matches: X",
            "  @Bar @Foo() @Baz A firstOrderedWithTwoUnsortedAnnotations();",
            "  @Bar @Baz @Foo() A threeSortedAnnotations();",
            "",
            "  // BUG: Diagnostic matches: X",
            "  @Foo({\"b\"}) @Bar({\"a\"}) A unsortedWithStringAttributes();",
            "  // BUG: Diagnostic matches: X",
            "  @Baz(str = {\"a\", \"b\"}) @Foo(ints = {1, 0}) @Bar A unsortedWithAttributes();",
            "  // BUG: Diagnostic matches: X",
            "  @Bar @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) @Baz A unsortedWithNestedBar();",
            "  @Bar @Baz @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")})  A sortedWithNestedBar();",
            "",
            " @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) @Foo(ints = {1, 2}) @Foo({\"b\"}) A sortedRepeatableAnnotation();",
            "  // BUG: Diagnostic matches: X",
            " @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) @Bar @Foo(ints = {1, 2}) A unsortedRepeatableAnnotation();",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import java.lang.annotation.Repeatable;",
            "interface A {",
            "  @Repeatable(Foos.class)",
            "  @interface Foo {",
            "    String[] value() default {};",
            "    int[] ints() default {};",
            "    Bar[] anns() default {};",
            "  }",
            "",
            "  @interface Bar {",
            "    String[] value() default {};",
            "  }",
            "",
            "  @interface Baz {",
            "    String[] str() default {};",
            "  }",
            "",
            "  @interface Foos {",
            "     Foo[] value();",
            "  }",
            "",
            "  @Bar A singleAnnotation();",
            "  @Bar @Foo A sortedAnnotations();",
            "  @Foo @Bar A unsortedAnnotations();",
            "  @Foo() @Baz() @Bar A unsortedAnnotationsWithSomeParens();",
            "",
            "  @Bar @Baz(str = {\"a\", \"b\"}) @Foo() A unsortedAnnotationsOneContainingAttributes();",
            "  @Baz(str = {\"a\", \"b\"}) @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) @Bar({\"b\"}) A unsortedAnnotationsWithAttributes();",
            "",
            "  @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) @Foo(ints = {1, 2}) @Foo({\"b\"}) A sortedRepeatableAnnotation();",
            "  @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) @Bar @Foo(ints = {1, 2}) A unsortedRepeatableAnnotation();",
            "",
            "}")
        .addOutputLines(
            "out/A.java",
            "import java.lang.annotation.Repeatable;",
            "interface A {",
            "  @Repeatable(Foos.class)",
            "  @interface Foo {",
            "    String[] value() default {};",
            "    int[] ints() default {};",
            "    Bar[] anns() default {};",
            "  }",
            "",
            "  @interface Bar {",
            "    String[] value() default {};",
            "  }",
            "",
            "  @interface Baz {",
            "    String[] str() default {};",
            "  }",
            "",
            "  @interface Foos {",
            "     Foo[] value();",
            "  }",
            "  @Bar A singleAnnotation();",
            "  @Bar @Foo A sortedAnnotations();",
            "  @Bar @Foo A unsortedAnnotations();",
            "  @Bar @Baz() @Foo() A unsortedAnnotationsWithSomeParens();",
            "",
            "  @Bar @Baz(str = {\"a\", \"b\"}) @Foo() A unsortedAnnotationsOneContainingAttributes();",
            "  @Bar({\"b\"}) @Baz(str = {\"a\", \"b\"}) @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) A unsortedAnnotationsWithAttributes();",
            "",
            "  @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) @Foo(ints = {1, 2}) @Foo({\"b\"}) A sortedRepeatableAnnotation();",
            "  @Bar @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) @Foo(ints = {1, 2}) A unsortedRepeatableAnnotation();",
            "",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
