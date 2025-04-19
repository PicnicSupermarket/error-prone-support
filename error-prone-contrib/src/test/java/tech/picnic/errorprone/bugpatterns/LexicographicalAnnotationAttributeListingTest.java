package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class LexicographicalAnnotationAttributeListingTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(LexicographicalAnnotationAttributeListing.class, getClass())
        .addSourceLines(
            "A.java",
            "import static java.math.RoundingMode.DOWN;",
            "import static java.math.RoundingMode.UP;",
            "",
            "import com.fasterxml.jackson.annotation.JsonPropertyOrder;",
            "import io.swagger.annotations.ApiImplicitParam;",
            "import io.swagger.annotations.ApiImplicitParams;",
            "import io.swagger.v3.oas.annotations.Parameter;",
            "import io.swagger.v3.oas.annotations.Parameters;",
            "import java.math.RoundingMode;",
            "import javax.xml.bind.annotation.XmlType;",
            "import org.springframework.context.annotation.PropertySource;",
            "import org.springframework.test.context.TestPropertySource;",
            "",
            "interface A {",
            "  @interface Foo {",
            "    String[] value() default {};",
            "",
            "    boolean[] bools() default {};",
            "",
            "    char[] chars() default {};",
            "",
            "    int[] ints() default {};",
            "",
            "    Class<?>[] cls() default {};",
            "",
            "    RoundingMode[] enums() default {};",
            "",
            "    Bar[] anns() default {};",
            "  }",
            "",
            "  @interface Bar {",
            "    String[] value() default {};",
            "  }",
            "",
            "  @Foo({})",
            "  A noString();",
            "",
            "  @Foo({\"a\"})",
            "  A oneString();",
            "",
            "  @Foo({\"a\", \"b\"})",
            "  A sortedStrings();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Foo({\"b\", \"a\"})",
            "  A unsortedString();",
            "",
            "  @Foo({\"ab\", \"Ac\"})",
            "  A sortedStringCaseInsensitive();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Foo({\"ac\", \"Ab\"})",
            "  A unsortedStringCaseInsensitive();",
            "",
            "  @Foo({\"A\", \"a\"})",
            "  A sortedStringCaseInsensitiveWithTotalOrderFallback();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Foo({\"a\", \"A\"})",
            "  A unsortedStringCaseInsensitiveWithTotalOrderFallback();",
            "",
            "  @Foo(bools = {})",
            "  A noBools();",
            "",
            "  @Foo(bools = {false})",
            "  A oneBool();",
            "",
            "  @Foo(bools = {false, true})",
            "  A sortedBools();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Foo(bools = {true, false})",
            "  A unsortedBools();",
            "",
            "  @Foo(chars = {})",
            "  A noChars();",
            "",
            "  @Foo(chars = {'a'})",
            "  A oneChar();",
            "",
            "  @Foo(chars = {'a', 'b'})",
            "  A sortedChars();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Foo(chars = {'b', 'a'})",
            "  A unsortedChars();",
            "",
            "  @Foo(ints = {})",
            "  A noInts();",
            "",
            "  @Foo(ints = {0})",
            "  A oneInt();",
            "",
            "  @Foo(ints = {0, 1})",
            "  A sortedInts();",
            "",
            "  @Foo(ints = {1, 0})",
            "  A unsortedInts();",
            "",
            "  @Foo(cls = {})",
            "  A noClasses();",
            "",
            "  @Foo(cls = {int.class})",
            "  A oneClass();",
            "",
            "  @Foo(cls = {int.class, long.class})",
            "  A sortedClasses();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Foo(cls = {long.class, int.class})",
            "  A unsortedClasses();",
            "",
            "  @Foo(enums = {})",
            "  A noEnums();",
            "",
            "  @Foo(enums = {DOWN})",
            "  A oneEnum();",
            "",
            "  @Foo(enums = {DOWN, UP})",
            "  A sortedEnums();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Foo(enums = {UP, DOWN})",
            "  A unsortedEnums();",
            "",
            "  @Foo(anns = {})",
            "  A noAnns();",
            "",
            "  @Foo(anns = {@Bar(\"a\")})",
            "  A oneAnn();",
            "",
            "  @Foo(anns = {@Bar(\"a\"), @Bar(\"b\")})",
            "  A sortedAnns();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")})",
            "  A unsortedAnns();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Foo(anns = {@Bar(\"a\"), @Bar({\"b\", \"a\"})})",
            "  A unsortedInnerAnns();",
            "",
            "  @Foo({\"a=foo\", \"a.b=bar\", \"a.c=baz\"})",
            "  A hierarchicallySorted();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Foo({\"a.b=bar\", \"a.c=baz\", \"a=foo\"})",
            "  A hierarchicallyUnsorted();",
            "",
            "  @JsonPropertyOrder({\"field2\", \"field1\"})",
            "  A dto();",
            "",
            "  @ApiImplicitParams({@ApiImplicitParam(\"p2\"), @ApiImplicitParam(\"p1\")})",
            "  A firstEndpoint();",
            "",
            "  @Parameters({@Parameter(name = \"p2\"), @Parameter(name = \"p1\")})",
            "  A secondEndpoint();",
            "",
            "  @XmlType(propOrder = {\"field2\", \"field1\"})",
            "  class XmlTypeDummy {}",
            "",
            "  @PropertySource({\"field2\", \"field1\"})",
            "  class PropertySourceDummy {}",
            "",
            "  @TestPropertySource(locations = {\"field2\", \"field1\"})",
            "  class FirstTestPropertySourceDummy {}",
            "",
            "  @TestPropertySource({\"field2\", \"field1\"})",
            "  class SecondTestPropertySourceDummy {}",
            "}")
        .doTest();
  }

  // XXX: Note that in the output below in one instance redundant `value =` assignments are
  // introduced. Avoiding that might make the code too complex. Instead, users can have the
  // `CanonicalAnnotationSyntax` checker correct the situation in a subsequent run.
  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(
            LexicographicalAnnotationAttributeListing.class, getClass())
        .addInputLines(
            "A.java",
            "import static java.math.RoundingMode.DOWN;",
            "import static java.math.RoundingMode.UP;",
            "",
            "import java.math.RoundingMode;",
            "",
            "interface A {",
            "  @interface Foo {",
            "    String[] value() default {};",
            "",
            "    boolean[] bools() default {};",
            "",
            "    char[] chars() default {};",
            "",
            "    Class<?>[] cls() default {};",
            "",
            "    RoundingMode[] enums() default {};",
            "",
            "    Bar[] anns() default {};",
            "  }",
            "",
            "  @interface Bar {",
            "    String[] value() default {};",
            "  }",
            "",
            "  @Foo({\" \", \"\", \"b\", \"a\"})",
            "  A unsortedStrings();",
            "",
            "  @Foo(bools = {true, false})",
            "  A unsortedBooleans();",
            "",
            "  @Foo(chars = {'b', 'a'})",
            "  A unsortedChars();",
            "",
            "  @Foo(cls = {long.class, int.class})",
            "  A unsortedClasses();",
            "",
            "  @Foo(enums = {UP, DOWN})",
            "  A unsortedEnums();",
            "",
            "  @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")})",
            "  A unsortedAnns();",
            "",
            "  @Foo(anns = {@Bar(\"a\"), @Bar({\"b\", \"a\"})})",
            "  A unsortedInnerAnns();",
            "}")
        .addOutputLines(
            "A.java",
            "import static java.math.RoundingMode.DOWN;",
            "import static java.math.RoundingMode.UP;",
            "",
            "import java.math.RoundingMode;",
            "",
            "interface A {",
            "  @interface Foo {",
            "    String[] value() default {};",
            "",
            "    boolean[] bools() default {};",
            "",
            "    char[] chars() default {};",
            "",
            "    Class<?>[] cls() default {};",
            "",
            "    RoundingMode[] enums() default {};",
            "",
            "    Bar[] anns() default {};",
            "  }",
            "",
            "  @interface Bar {",
            "    String[] value() default {};",
            "  }",
            "",
            "  @Foo({\"\", \" \", \"a\", \"b\"})",
            "  A unsortedStrings();",
            "",
            "  @Foo(bools = {false, true})",
            "  A unsortedBooleans();",
            "",
            "  @Foo(chars = {'a', 'b'})",
            "  A unsortedChars();",
            "",
            "  @Foo(cls = {int.class, long.class})",
            "  A unsortedClasses();",
            "",
            "  @Foo(enums = {DOWN, UP})",
            "  A unsortedEnums();",
            "",
            "  @Foo(anns = {@Bar(\"a\"), @Bar(\"b\")})",
            "  A unsortedAnns();",
            "",
            "  @Foo(anns = {@Bar(\"a\"), @Bar({\"a\", \"b\"})})",
            "  A unsortedInnerAnns();",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void filtering() {
    /* Some violations are not flagged because they are not in- or excluded. */
    CompilationTestHelper.newInstance(LexicographicalAnnotationAttributeListing.class, getClass())
        .setArgs(
            "-XepOpt:LexicographicalAnnotationAttributeListing:Includes=pkg.A.Foo,pkg.A.Bar",
            "-XepOpt:LexicographicalAnnotationAttributeListing:Excludes=pkg.A.Bar#value")
        .addSourceLines(
            "pkg/A.java",
            "package pkg;",
            "",
            "interface A {",
            "  @interface Foo {",
            "    String[] value() default {};",
            "",
            "    String[] value2() default {};",
            "  }",
            "",
            "  @interface Bar {",
            "    String[] value() default {};",
            "",
            "    String[] value2() default {};",
            "  }",
            "",
            "  @interface Baz {",
            "    String[] value() default {};",
            "",
            "    String[] value2() default {};",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Foo({\"b\", \"a\"})",
            "  A fooValue();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Foo(value2 = {\"b\", \"a\"})",
            "  A fooValue2();",
            "",
            "  @Bar({\"b\", \"a\"})",
            "  A barValue();",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Bar(value2 = {\"b\", \"a\"})",
            "  A barValue2();",
            "",
            "  @Baz({\"b\", \"a\"})",
            "  A bazValue();",
            "",
            "  @Baz(value2 = {\"b\", \"a\"})",
            "  A bazValue2();",
            "}")
        .doTest();
  }
}
