package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class LexicographicalAnnotationCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(LexicographicalAnnotationCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(new LexicographicalAnnotationCheck(), getClass());

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import java.lang.annotation.Repeatable;",
            "",
            "interface A {",
            "  @Repeatable(FooRepeat.class)",
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
            "  @interface FooRepeat {",
            "     Foo[] value();",
            "  }",
            "",
            " // BUG: Diagnostic contains:",
            "  @Foo @Bar A unsortedSimpleCase();",
            " // BUG: Diagnostic contains:",
            "  @Foo() @Bar() A unsortedWithBrackets();",
            "  @Foo() A onlyOneAnnotation();",
            "  @Bar @Foo() A sortedAnnotationsOneWithBrackets();",
            "",
            " // BUG: Diagnostic contains:",
            "  @Foo @Baz @Bar A threeUnsortedAnnotationsSameStartLetters();",
            " // BUG: Diagnostic contains:",
            "  @Bar @Foo() @Baz A firstOrderedWithTwoUnsortedAnnotations();",
            "  @Bar @Baz @Foo() A threeSortedAnnotations();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Foo({\"b\"}) @Bar({\"a\"}) A unsortedWithStringAttributes();",
            "  // BUG: Diagnostic contains:",
            "  @Baz(str = {\"a\", \"b\"}) @Foo(ints = {1, 0}) @Bar A unsortedWithAttributes();",
            "  // BUG: Diagnostic contains:",
            "  @Bar @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) @Baz A unsortedWithNestedBar();",
            "  @Bar @Baz @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")})  A sortedWithNestedBar();",
            "",
            //            " @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) @Foo({\"b\"}) @Foo(ints = {1,
            // 2}) A sortedRepeatableAnnotation();",
            "  // BUG: Diagnostic contains:",
            " @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) @Bar @Foo(ints = {1, 2}) A unsortedRepeatableAnnotation();",
            "}")
        .doTest();
  }

  @Test
  public void testReplacement() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import java.lang.annotation.Repeatable;",
            "interface A {",
            "  @Repeatable(FooRepeat.class)",
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
            "  @interface FooRepeat {",
            "     Foo[] value();",
            "  }",
            "",
            "  @Bar A singleAnnotation();",
            "  @Bar @Foo A sortedAnnotations();",
            "  @Foo @Bar A unsortedAnnotations();",
            "  @Foo() @Baz() @Bar A unsortedAnnotationsWithSomeBrackets();",
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
            "  @Repeatable(FooRepeat.class)",
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
            "  @interface FooRepeat {",
            "     Foo[] value();",
            "  }",
            "  @Bar A singleAnnotation();",
            "  @Bar @Foo A sortedAnnotations();",
            "  @Bar @Foo A unsortedAnnotations();",
            "  @Bar @Baz() @Foo() A unsortedAnnotationsWithSomeBrackets();",
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
