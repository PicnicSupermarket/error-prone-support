package tech.picnic.errorprone.bugpatterns;

import com.google.common.base.Predicates;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class MissingRefasterAnnotationCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(MissingRefasterAnnotationCheck.class, getClass())
          .expectErrorMessage(
              "X",
              Predicates.containsPattern(
                  "The Refaster template contains a method without Refaster annotation"));

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "RefasterTemplatesWithMissingAnnotations.java",
            "import com.google.errorprone.refaster.annotation.AfterTemplate;",
            "import com.google.errorprone.refaster.annotation.AlsoNegation;",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "import java.util.Map;",
            "",
            "final class RefasterTemplatesWithMissingAnnotations {",
            "  private RefasterTemplatesWithMissingAnnotations() {}",
            "",
            "  // BUG: Diagnostic matches: X",
            "  static final class MethodMissesBeforeTemplateAnnotation {",
            "    @BeforeTemplate",
            "    boolean equalsEmptyString(String string) {",
            "      return string.equals(\"\");",
            "    }",
            "",
            "", // @BeforeTemplate is missing
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
            "",
            "  // BUG: Diagnostic matches: X",
            "  static final class MethodMissesAfterTemplateAnnotation {",
            "    @BeforeTemplate",
            "    boolean equalsEmptyString(String string) {",
            "      return string.equals(\"\");",
            "    }",
            "",
            "    @BeforeTemplate",
            "    boolean lengthEquals0(String string) {",
            "      return string.length() == 0;",
            "    }",
            "",
            "", // @AfterTemplate is missing
            "    boolean optimizedMethod(String string) {",
            "      return string.isEmpty();",
            "    }",
            "  }",
            "",
            "  // BUG: Diagnostic matches: X",
            "  abstract class MethodMissesPlaceholderAnnotation<K, V> {",
            "",
            "", // @Placeholder is missing
            "    abstract V function(K key);",
            "",
            "    @BeforeTemplate",
            "    void before(Map<K, V> map, K key) {",
            "      if (!map.containsKey(key)) {",
            "        map.put(key, function(key));",
            "      }",
            "    }",
            "",
            "    @AfterTemplate",
            "    void after(Map<K, V> map, K key) {",
            "      map.computeIfAbsent(key, (K k) -> function(k));",
            "    }",
            "  }",
            "}")
        .doTest();
  }
}
