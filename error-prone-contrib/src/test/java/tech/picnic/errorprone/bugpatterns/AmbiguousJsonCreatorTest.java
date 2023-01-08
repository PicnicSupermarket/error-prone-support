package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Predicates.containsPattern;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class AmbiguousJsonCreatorTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(AmbiguousJsonCreator.class, getClass())
        .expectErrorMessage(
            "X", containsPattern("`JsonCreator.Mode` should be set for single-argument creators"))
        .addSourceLines(
            "Container.java",
            "import com.fasterxml.jackson.annotation.JsonCreator;",
            "import com.fasterxml.jackson.annotation.JsonValue;",
            "",
            "interface Container {",
            "  enum A {",
            "    FOO(1);",
            "",
            "    private final int i;",
            "",
            "    A(int i) {",
            "      this.i = i;",
            "    }",
            "",
            "    // BUG: Diagnostic matches: X",
            "    @JsonCreator",
            "    public static A of(int i) {",
            "      return FOO;",
            "    }",
            "  }",
            "",
            "  enum B {",
            "    FOO(1);",
            "",
            "    private final int i;",
            "",
            "    B(int i) {",
            "      this.i = i;",
            "    }",
            "",
            "    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)",
            "    public static B of(int i) {",
            "      return FOO;",
            "    }",
            "  }",
            "",
            "  enum C {",
            "    FOO(1, \"s\");",
            "",
            "    @JsonValue private final int i;",
            "    private final String s;",
            "",
            "    C(int i, String s) {",
            "      this.i = i;",
            "      this.s = s;",
            "    }",
            "",
            "    // BUG: Diagnostic matches: X",
            "    @JsonCreator",
            "    public static C of(int i) {",
            "      return FOO;",
            "    }",
            "  }",
            "",
            "  enum D {",
            "    FOO(1, \"s\");",
            "",
            "    private final int i;",
            "    private final String s;",
            "",
            "    D(int i, String s) {",
            "      this.i = i;",
            "      this.s = s;",
            "    }",
            "",
            "    @JsonCreator",
            "    public static D of(int i, String s) {",
            "      return FOO;",
            "    }",
            "  }",
            "",
            "  enum E {",
            "    FOO;",
            "",
            "    // BUG: Diagnostic matches: X",
            "    @JsonCreator",
            "    public static E of(String s) {",
            "      return FOO;",
            "    }",
            "  }",
            "",
            "  class F {",
            "    private final String s;",
            "",
            "    F(String s) {",
            "      this.s = s;",
            "    }",
            "",
            "    @JsonCreator",
            "    public static F of(String s) {",
            "      return new F(s);",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(AmbiguousJsonCreator.class, getClass())
        .addInputLines(
            "A.java",
            "import com.fasterxml.jackson.annotation.JsonCreator;",
            "",
            "enum A {",
            "  FOO;",
            "",
            "  @JsonCreator",
            "  public static A of(String s) {",
            "    return FOO;",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import com.fasterxml.jackson.annotation.JsonCreator;",
            "",
            "enum A {",
            "  FOO;",
            "",
            "  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)",
            "  public static A of(String s) {",
            "    return FOO;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
