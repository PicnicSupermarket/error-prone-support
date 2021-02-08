package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class LexicographicalAnnotationCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(
          LexicographicalAnnotationCheck.class, getClass());
//  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
//      BugCheckerRefactoringTestHelper.newInstance(
//          new LexicographicalAnnotationCheck(), getClass());

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static java.math.RoundingMode.UP;",
            "import static java.math.RoundingMode.DOWN;",
            "",
            "import com.fasterxml.jackson.annotation.JsonPropertyOrder;",
            "import io.swagger.annotations.ApiImplicitParam;",
            "import io.swagger.annotations.ApiImplicitParams;",
            "import io.swagger.v3.oas.annotations.Parameter;",
            "import io.swagger.v3.oas.annotations.Parameters;",
            "import java.math.RoundingMode;",
            "import javax.xml.bind.annotation.XmlType;",
            "",
            "interface A {",
            "  @interface Foo {",
            "    String[] value() default {};",
            "    int[] ints() default {};",
            "    Class<?>[] cls() default {};",
            "    RoundingMode[] enums() default {};",
            "    Bar[] anns() default {};",
            "  }",
            "",
            "  @interface Bar {",
            "    String[] value() default {};",
            "  }",
            "",
//            " // BUG: Diagnostic contains:",
            "  @Foo() @Bar() A noString();",
//            "  @Foo({\"a\"}) A oneString();",
//            "  @Foo({\"a\", \"b\"}) A sortedStrings();",
//            "  // BUG: Diagnostic contains:",
//            "  @Foo({\"b\", \"a\"}) A unsortedString();",
//            "  @Foo({\"ab\", \"Ac\"}) A sortedStringCaseInsensitive();",
//            "  // BUG: Diagnostic contains:",
//            "  @Foo({\"ac\", \"Ab\"}) A unsortedStringCaseInsensitive();",
//            "  @Foo({\"A\", \"a\"}) A sortedStringCaseInsensitiveWithTotalOrderFallback();",
//            "  // BUG: Diagnostic contains:",
//            "  @Foo({\"a\", \"A\"}) A unsortedStringCaseInsensitiveWithTotalOrderFallback();",
//            "",
//            "  @Foo(ints = {}) A noInts();",
//            "  @Foo(ints = {0}) A oneInt();",
//            "  @Foo(ints = {0, 1}) A sortedInts();",
//            "  @Foo(ints = {1, 0}) A unsortedInts();",
//            "",
//            "  @Foo(cls = {}) A noClasses();",
//            "  @Foo(cls = {int.class}) A oneClass();",
//            "  @Foo(cls = {int.class, long.class}) A sortedClasses();",
//            "  // BUG: Diagnostic contains:",
//            "  @Foo(cls = {long.class, int.class}) A unsortedClasses();",
//            "",
//            "  @Foo(enums = {}) A noEnums();",
//            "  @Foo(enums = {DOWN}) A oneEnum();",
//            "  @Foo(enums = {DOWN, UP}) A sortedEnums();",
//            "  // BUG: Diagnostic contains:",
//            "  @Foo(enums = {UP, DOWN}) A unsortedEnums();",
//            "",
//            "  @Foo(anns = {}) A noAnns();",
//            "  @Foo(anns = {@Bar(\"a\")}) A oneAnn();",
//            "  @Foo(anns = {@Bar(\"a\"), @Bar(\"b\")}) A sortedAnns();",
//            "  // BUG: Diagnostic contains:",
//            "  @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) A unsortedAnns();",
//            "  // BUG: Diagnostic contains:",
//            "  @Foo(anns = {@Bar(\"a\"), @Bar({\"b\", \"a\"})}) A unsortedInnderAnns();",
//            "",
//            "  @Foo({\"a=foo\", \"a.b=bar\", \"a.c=baz\"}) A hierarchicallySorted();",
//            "  // BUG: Diagnostic contains:",
//            "  @Foo({\"a.b=bar\", \"a.c=baz\", \"a=foo\"}) A hierarchicallyUnsorted();",
//            "",
//            "  @JsonPropertyOrder({\"field2\", \"field1\"}) A dto();",
//            "  @ApiImplicitParams({@ApiImplicitParam(\"p2\"), @ApiImplicitParam(\"p1\")}) A firstEndpoint();",
//            "  @Parameters({@Parameter(name = \"p2\"), @Parameter(name = \"p1\")}) A secondEndpoint();",
//            "",
//            "  @XmlType(propOrder = {\"field2\", \"field1\"})",
//            "  class Dummy {}",
            "}")
        .doTest();
  }

  // XXX: Note that in the output below in one instance redundant `value =` assignments are
  // introduced. Avoiding that might make the code too complex. Instead, users can have the
  // `CanonicalAnnotationSyntaxCheck` correct the situation in a subsequent run.
//  @Test
//  public void testReplacement() {
//    refactoringTestHelper
//        .addInputLines(
//            "in/A.java",
//            "import static java.math.RoundingMode.UP;",
//            "import static java.math.RoundingMode.DOWN;",
//            "",
//            "import java.math.RoundingMode;",
//            "",
//            "interface A {",
//            "  @interface Foo {",
//            "    String[] value() default {};",
//            "    Class<?>[] cls() default {};",
//            "    RoundingMode[] enums() default {};",
//            "    Bar[] anns() default {};",
//            "  }",
//            "",
//            "  @interface Bar {",
//            "    String[] value() default {};",
//            "  }",
//            "",
//            "  @Foo({\"b\", \"a\"}) A unsortedString();",
//            "  @Foo(cls = {long.class, int.class}) A unsortedClasses();",
//            "  @Foo(enums = {UP, DOWN}) A unsortedEnums();",
//            "  @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) A unsortedAnns();",
//            "  @Foo(anns = {@Bar(\"a\"), @Bar({\"b\", \"a\"})}) A unsortedInnderAnns();",
//            "}")
//        .addOutputLines(
//            "out/A.java",
//            "import static java.math.RoundingMode.UP;",
//            "import static java.math.RoundingMode.DOWN;",
//            "",
//            "import java.math.RoundingMode;",
//            "",
//            "interface A {",
//            "  @interface Foo {",
//            "    String[] value() default {};",
//            "    Class<?>[] cls() default {};",
//            "    RoundingMode[] enums() default {};",
//            "    Bar[] anns() default {};",
//            "  }",
//            "",
//            "  @interface Bar {",
//            "    String[] value() default {};",
//            "  }",
//            "",
//            "  @Foo({\"a\", \"b\"}) A unsortedString();",
//            "  @Foo(cls = {int.class, long.class}) A unsortedClasses();",
//            "  @Foo(enums = {DOWN, UP}) A unsortedEnums();",
//            "  @Foo(anns = {@Bar(\"a\"), @Bar(\"b\")}) A unsortedAnns();",
//            "  @Foo(anns = {@Bar(\"a\"), @Bar({\"a\", \"b\"})}) A unsortedInnderAnns();",
//            "}")
//        .doTest(TestMode.TEXT_MATCH);
//  }
}
