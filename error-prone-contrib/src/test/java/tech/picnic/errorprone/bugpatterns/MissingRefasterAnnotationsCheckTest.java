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
                  "The Refaster template contains a method without Refaster annotation"));

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "RefasterTemplateStringIsEmpty.java",
            "import com.google.errorprone.refaster.annotation.AfterTemplate;",
            "import com.google.errorprone.refaster.annotation.AlsoNegation;",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "import java.util.Map;",
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
            "  static final class SecondStringIsEmpty {",
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
            "  abstract class ComputeIfAbsent<K, V> {",
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
