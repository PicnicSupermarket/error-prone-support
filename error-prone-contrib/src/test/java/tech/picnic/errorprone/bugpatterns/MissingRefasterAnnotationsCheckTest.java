package tech.picnic.errorprone.bugpatterns;

import com.google.common.base.Predicates;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class MissingRefasterAnnotationsCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(MissingRefasterAnnotationsCheck.class, getClass())
          .expectErrorMessage(
              "X",
              Predicates.containsPattern(
                  "The Refaster template contains a method without annotation"));

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "RefasterTemplateStringIsEmpty.java",
            "import com.google.errorprone.refaster.annotation.AfterTemplate;",
            "import com.google.errorprone.refaster.annotation.AlsoNegation;",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "final class RefasterTemplateStringIsEmpty {",
            "  private RefasterTemplateStringIsEmpty() {}",
            "",
            "  // BUG: Diagnostic matches: X",
            "  static final class StringIsEmpty {",
            "    @BeforeTemplate",
            "    boolean equalsEmptyString(String string) {",
            "      return string.equals(\"\");",
            "    }",
            "",
            "    // Here the @BeforeTemplate is missing",
            "    boolean lengthEquals0(String string) {",
            "      return string.length() == 0;",
            "    }",
            "",
            "    @AfterTemplate",
            "    @AlsoNegation",
            "    boolean optimizedMethod(String string) {",
            "      return string.isEmpty();",
            "    }",
            "  }",
            "}")
        .doTest();
  }
}
