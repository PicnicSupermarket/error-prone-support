package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class StaticImportCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(StaticImportCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(new StaticImportCheck(), getClass());

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static com.google.common.collect.ImmutableMap.toImmutableMap;",
            "import static com.google.common.collect.ImmutableSet.toImmutableSet;",
            "import static java.nio.charset.StandardCharsets.UTF_8;",
            "",
            "import com.google.common.collect.ImmutableMap;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.nio.charset.StandardCharsets;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    ImmutableMap.toImmutableMap(v -> v, v -> v);",
            "    ImmutableMap.<String, String,  String>toImmutableMap(v -> v, v -> v);",
            "    toImmutableMap(v -> v, v -> v);",
            "",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSet.toImmutableSet();",
            "    ImmutableSet.<String>toImmutableSet();",
            "    toImmutableSet();",
            "",
            "    // BUG: Diagnostic contains:",
            "    Object o1 = StandardCharsets.UTF_8;",
            "    Object o2 = UTF_8;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void testReplacement() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import com.google.common.collect.ImmutableMap;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.nio.charset.StandardCharsets;",
            "import org.springframework.format.annotation.DateTimeFormat;",
            "import org.springframework.format.annotation.DateTimeFormat.ISO;",
            "",
            "class A {",
            "  void m1() {",
            "    ImmutableMap.toImmutableMap(v -> v, v -> v);",
            "    ImmutableMap.<String, String,  String>toImmutableMap(v -> v, v -> v);",
            "",
            "    ImmutableSet.toImmutableSet();",
            "    ImmutableSet.<String>toImmutableSet();",
            "",
            "    Object o = StandardCharsets.UTF_8;",
            "  }",
            "",
            "  void m2(",
            "      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date,",
            "      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) String dateTime,",
            "      @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) String time) {}",
            "",
            "  void m3(",
            "      @DateTimeFormat(iso = ISO.DATE) String date,",
            "      @DateTimeFormat(iso = ISO.DATE_TIME) String dateTime,",
            "      @DateTimeFormat(iso = ISO.TIME) String time) {}",
            "}")
        .addOutputLines(
            "out/A.java",
            "import static com.google.common.collect.ImmutableMap.toImmutableMap;",
            "import static com.google.common.collect.ImmutableSet.toImmutableSet;",
            "import static java.nio.charset.StandardCharsets.UTF_8;",
            "import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;",
            "import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;",
            "import static org.springframework.format.annotation.DateTimeFormat.ISO.TIME;",
            "",
            "import com.google.common.collect.ImmutableMap;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.nio.charset.StandardCharsets;",
            "import org.springframework.format.annotation.DateTimeFormat;",
            "import org.springframework.format.annotation.DateTimeFormat.ISO;",
            "",
            "class A {",
            "  void m1() {",
            "    toImmutableMap(v -> v, v -> v);",
            "    ImmutableMap.<String, String,  String>toImmutableMap(v -> v, v -> v);",
            "",
            "    toImmutableSet();",
            "    ImmutableSet.<String>toImmutableSet();",
            "",
            "    Object o = UTF_8;",
            "  }",
            "",
            "  void m2(",
            "      @DateTimeFormat(iso = DATE) String date,",
            "      @DateTimeFormat(iso = DATE_TIME) String dateTime,",
            "      @DateTimeFormat(iso = TIME) String time) {}",
            "",
            "  void m3(",
            "      @DateTimeFormat(iso = DATE) String date,",
            "      @DateTimeFormat(iso = DATE_TIME) String dateTime,",
            "      @DateTimeFormat(iso = TIME) String time) {}",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }
}
