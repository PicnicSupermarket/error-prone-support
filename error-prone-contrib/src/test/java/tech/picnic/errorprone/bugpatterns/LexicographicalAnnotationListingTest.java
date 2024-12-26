package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class LexicographicalAnnotationListingTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(LexicographicalAnnotationListing.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Repeatable;
            import java.lang.annotation.Target;

            interface A {
              @Repeatable(Foos.class)
              @interface Foo {
                String[] value() default {};

                int[] ints() default {};

                Bar[] anns() default {};
              }

              @Target(ElementType.METHOD)
              @interface Bar {
                String[] value() default {};
              }

              @interface Baz {
                String[] str() default {};
              }

              @interface Foos {
                Foo[] value();
              }

              @Target(ElementType.TYPE_USE)
              @interface FooTypeUse {
                String[] value() default {};
              }

              @Target(ElementType.TYPE_USE)
              @interface BarTypeUse {
                String[] value() default {};
              }

              // BUG: Diagnostic contains:
              @Foo
              @Bar
              A unsortedSimpleCase();

              // BUG: Diagnostic contains:
              @Foo()
              @Bar()
              A unsortedWithParens();

              @Foo()
              A onlyOneAnnotation();

              @Bar
              @Foo()
              A sortedAnnotationsOneWithParens();

              // BUG: Diagnostic contains:
              @Foo
              @Baz
              @Bar
              A threeUnsortedAnnotationsSameInitialLetter();

              // BUG: Diagnostic contains:
              @Bar
              @Foo()
              @Baz
              A firstOrderedWithTwoUnsortedAnnotations();

              @Bar
              @Baz
              @Foo()
              A threeSortedAnnotations();

              // BUG: Diagnostic contains:
              @Foo({"b"})
              @Bar({"a"})
              A unsortedWithStringAttributes();

              // BUG: Diagnostic contains:
              @Baz(str = {"a", "b"})
              @Foo(ints = {1, 0})
              @Bar
              A unsortedWithAttributes();

              // BUG: Diagnostic contains:
              @Bar
              @Foo(anns = {@Bar("b"), @Bar("a")})
              @Baz
              A unsortedWithNestedBar();

              @Bar
              @Baz
              @Foo(anns = {@Bar("b"), @Bar("a")})
              A sortedWithNestedBar();

              @Foo(anns = {@Bar("b"), @Bar("a")})
              @Foo(ints = {1, 2})
              @Foo({"b"})
              A sortedRepeatableAnnotation();

              // BUG: Diagnostic contains:
              @Foo(anns = {@Bar("b"), @Bar("a")})
              @Bar
              @Foo(ints = {1, 2})
              A unsortedRepeatableAnnotation();

              // BUG: Diagnostic contains:
              default @FooTypeUse @BarTypeUse A unsortedTypeAnnotations() {
                return null;
              }

              // BUG: Diagnostic contains:
              @Baz
              @Bar
              default @FooTypeUse @BarTypeUse A unsortedTypeUseAndOtherAnnotations() {
                return null;
              }
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(LexicographicalAnnotationListing.class, getClass())
        .addInputLines(
            "A.java",
            """
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Repeatable;
            import java.lang.annotation.Target;

            interface A {
              @Repeatable(Foos.class)
              @interface Foo {
                String[] value() default {};

                int[] ints() default {};

                Bar[] anns() default {};
              }

              @Target(ElementType.METHOD)
              @interface Bar {
                String[] value() default {};
              }

              @interface Baz {
                String[] str() default {};
              }

              @interface Foos {
                Foo[] value();
              }

              @Target(ElementType.TYPE_USE)
              @interface FooTypeUse {
                String[] value() default {};
              }

              @Target(ElementType.TYPE_USE)
              @interface BarTypeUse {
                String[] value() default {};
              }

              @Bar
              A singleAnnotation();

              @Bar
              @Foo
              A sortedAnnotations();

              @Foo
              @Bar
              A unsortedAnnotations();

              @Foo()
              @Baz()
              @Bar
              A unsortedAnnotationsWithSomeParens();

              @Bar
              @Baz(str = {"a", "b"})
              @Foo()
              A unsortedAnnotationsOneContainingAttributes();

              @Baz(str = {"a", "b"})
              @Foo(anns = {@Bar("b"), @Bar("a")})
              @Bar({"b"})
              A unsortedAnnotationsWithAttributes();

              @Foo(anns = {@Bar("b"), @Bar("a")})
              @Foo(ints = {1, 2})
              @Foo({"b"})
              A sortedRepeatableAnnotation();

              @Foo(anns = {@Bar("b"), @Bar("a")})
              @Bar
              @Foo(ints = {1, 2})
              A unsortedRepeatableAnnotation();

              @Baz
              @Bar
              default @FooTypeUse @BarTypeUse A unsortedWithTypeUseAnnotations() {
                return null;
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import java.lang.annotation.ElementType;
            import java.lang.annotation.Repeatable;
            import java.lang.annotation.Target;

            interface A {
              @Repeatable(Foos.class)
              @interface Foo {
                String[] value() default {};

                int[] ints() default {};

                Bar[] anns() default {};
              }

              @Target(ElementType.METHOD)
              @interface Bar {
                String[] value() default {};
              }

              @interface Baz {
                String[] str() default {};
              }

              @interface Foos {
                Foo[] value();
              }

              @Target(ElementType.TYPE_USE)
              @interface FooTypeUse {
                String[] value() default {};
              }

              @Target(ElementType.TYPE_USE)
              @interface BarTypeUse {
                String[] value() default {};
              }

              @Bar
              A singleAnnotation();

              @Bar
              @Foo
              A sortedAnnotations();

              @Bar
              @Foo
              A unsortedAnnotations();

              @Bar
              @Baz()
              @Foo()
              A unsortedAnnotationsWithSomeParens();

              @Bar
              @Baz(str = {"a", "b"})
              @Foo()
              A unsortedAnnotationsOneContainingAttributes();

              @Bar({"b"})
              @Baz(str = {"a", "b"})
              @Foo(anns = {@Bar("b"), @Bar("a")})
              A unsortedAnnotationsWithAttributes();

              @Foo(anns = {@Bar("b"), @Bar("a")})
              @Foo(ints = {1, 2})
              @Foo({"b"})
              A sortedRepeatableAnnotation();

              @Bar
              @Foo(anns = {@Bar("b"), @Bar("a")})
              @Foo(ints = {1, 2})
              A unsortedRepeatableAnnotation();

              @Bar
              @Baz
              default @BarTypeUse @FooTypeUse A unsortedWithTypeUseAnnotations() {
                return null;
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
