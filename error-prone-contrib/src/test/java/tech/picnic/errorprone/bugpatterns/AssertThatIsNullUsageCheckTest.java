package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class AssertThatIsNullUsageCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(AssertThatIsNullUsageCheck.class, getClass());

  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(AssertThatIsNullUsageCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "import org.junit.jupiter.api.Test;",
            "public final class A {",
            "  @Test",
            "  public void testAssertThat() {",
            "    String nullValue = null;",
            "    assertThat(12).isEqualTo(12);",
            "    // BUG: Diagnostic contains: assertThat(...).isNull()",
            "    assertThat(\"value\").isEqualTo(null);",
            "    // BUG: Diagnostic contains: assertThat(...).isNull()",
            "    assertThat(nullValue).isEqualTo(null);",
            "    isEqualTo(\"foo\");",
            "    isEqualTo(null);",
            "  }",
            "",
            " private boolean isEqualTo (Object value) {",
            "     return value.equals(\"bar\");",
            " }",
            "} ")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "import org.junit.jupiter.api.Test;",
            "public final class A {",
            "  @Test",
            "  public void testAssertThat() {",
            "    String nullValue = null;",
            "    assertThat(12).isEqualTo(12);",
            "    assertThat(\"value\").isEqualTo(null);",
            "    assertThat(nullValue).isEqualTo(null);",
            "    isEqualTo(\"foo\");",
            "    isEqualTo(null);",
            "  }",
            "",
            " private boolean isEqualTo (Object value) {",
            "     return value.equals(\"bar\");",
            " }",
            "} ")
        .addOutputLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "import org.junit.jupiter.api.Test;",
            "public final class A {",
            "  @Test",
            "  public void testAssertThat() {",
            "    String nullValue = null;",
            "    assertThat(12).isEqualTo(12);",
            "    assertThat(\"value\").isNull();",
            "    assertThat(nullValue).isNull();",
            "    isEqualTo(\"foo\");",
            "    isEqualTo(null);",
            "  }",
            "",
            " private boolean isEqualTo (Object value) {",
            "     return value.equals(\"bar\");",
            " }",
            "} ")
        .doTest();
  }
}
