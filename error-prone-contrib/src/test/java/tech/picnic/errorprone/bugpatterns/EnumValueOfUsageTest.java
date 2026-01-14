package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class EnumValueOfUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(EnumValueOfUsage.class, getClass())
        .expectErrorMessage(
            "INVALID_VALUE",
            m ->
                m.contains("is not a valid value for `Test.A`, possible values: [ONE, TWO, THREE]"))
        .expectErrorMessage(
            "MISSING_VALUE", m -> m.contains("might generate values which are missing in `Test.A`"))
        .expectErrorMessage(
            "AVOID_RAW", m -> m.contains("Avoid passing unchecked arguments to `valueOf`"))
        .addSourceLines(
            "Test.java",
            "class Test {",
            "  void unsafeCases(String raw, B b) {",
            "    // BUG: Diagnostic matches: INVALID_VALUE",
            "    A.valueOf(\"FOUR\");",
            "    // BUG: Diagnostic matches: INVALID_VALUE",
            "    A.valueOf(A.class, \"FOUR\");",
            "    // BUG: Diagnostic matches: AVOID_RAW",
            "    A.valueOf(raw);",
            "    // BUG: Diagnostic matches: AVOID_RAW",
            "    A.valueOf(A.class, raw);",
            "    // BUG: Diagnostic matches: MISSING_VALUE",
            "    A.valueOf(b.name());",
            "    // BUG: Diagnostic matches: MISSING_VALUE",
            "    A.valueOf(A.class, b.name());",
            "    var name =",
            "        switch (b) {",
            "          // BUG: Diagnostic matches: MISSING_VALUE",
            "          case FOUR -> A.valueOf(b.name());",
            "          // BUG: Diagnostic matches: MISSING_VALUE",
            "          case FIVE -> A.valueOf(A.class, b.name());",
            "          default -> null;",
            "        };",
            "    var toString =",
            "        switch (b) {",
            "          // BUG: Diagnostic matches: MISSING_VALUE",
            "          case FOUR -> A.valueOf(b.toString());",
            "          // BUG: Diagnostic matches: MISSING_VALUE",
            "          case FIVE -> A.valueOf(A.class, b.toString());",
            "          default -> null;",
            "        };",
            "    var defaultCase =",
            "        switch (b) {",
            "          case ONE, FOUR -> null;",
            "          // BUG: Diagnostic matches: MISSING_VALUE",
            "          default -> A.valueOf(b.name());",
            "        };",
            "  }",
            "",
            "  // Following cases are marked as no match",
            "  void safeCases(B b) {",
            "    A.valueOf(\"ONE\");",
            "    A.valueOf(A.class, \"TWO\");",
            "    var a =",
            "        switch (b) {",
            "          case ONE, THREE -> A.valueOf(A.class, b.name());",
            "          case TWO -> A.valueOf(b.name());",
            "          case FOUR -> A.valueOf(A.ONE.name());",
            "          default -> null;",
            "        };",
            "    var defaultCase =",
            "        switch (b) {",
            "          case FOUR, FIVE -> null;",
            "          default -> A.valueOf(b.name());",
            "        };",
            "  }",
            "",
            "  // Following cases are ignored for the sake of brevity",
            "  void ignoredCases() {",
            "    A.valueOf(rawMethod());",
            "    A.valueOf(A.class, rawMethod());",
            "    java.util.List.of(\"\").stream().map(A::valueOf);",
            "  }",
            "",
            "  String rawMethod() {",
            "    return \"FIVE\";",
            "  }",
            "",
            "  enum A {",
            "    ONE,",
            "    TWO,",
            "    THREE",
            "  }",
            "",
            "  enum B {",
            "    ONE,",
            "    TWO,",
            "    THREE,",
            "    FOUR,",
            "    FIVE",
            "  }",
            "}")
        .doTest();
  }
}
