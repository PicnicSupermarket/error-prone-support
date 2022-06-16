package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class AssertThatIsNullCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(AssertThatIsNullCheck.class, getClass());

  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(AssertThatIsNullCheck.class, getClass());

  // XXX: Methods always start with lowercase. I thought we had a Checkstyle thing for this.
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
            "  }",
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
            "  }",
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
            "  }",
            "} ")
        .doTest();
  }
}
