package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

final class RefasterRuleNamingTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(RefasterRuleNaming.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "    final class StringRules {",
            "      private StringRules() {}",
            "",
            "      /** Prefer {@link String#isEmpty()} over alternatives that consult the string's length. */",
            "      static final class IntegerIsEmpty {",
            "        @BeforeTemplate",
            "        boolean before(String str) {",
            "          return Refaster.anyOf(str.length() == 0, str.length() <= 0, str.length() < 1);",
            "        }",
            "",
            "        @AfterTemplate",
            "        boolean after(String str) {",
            "          return str.isEmpty();",
            "        }",
            "      }",
            "",
            "      /** Prefer {@link Strings#isNullOrEmpty(String)} over the more verbose alternative. */",
            "      static final class IntegerIsNullOrEmpty {", // StringIsNullOrEmpty
            "        @BeforeTemplate",
            "        boolean before(@Nullable String str) {",
            "          return str == null || str.isEmpty();",
            "        }",
            "",
            "        @AfterTemplate",
            "        @AlsoNegation",
            "        boolean after(String str) {",
            "          return Strings.isNullOrEmpty(str);",
            "        }",
            "      }")
        .doTest();
  }

  @Disabled
  @Test
  void identificationNegative() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "final class A {",
            "  @BeforeTemplate",
            "  String before(String str) {",
            "    return str;",
            "  }",
            "",
            "  String nonRefasterMethod(String str) {",
            "    return str;",
            "  }",
            "",
            "  static final class Inner {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str;",
            "    }",
            "  }",
            "}")
        .doTest();
  }
}
