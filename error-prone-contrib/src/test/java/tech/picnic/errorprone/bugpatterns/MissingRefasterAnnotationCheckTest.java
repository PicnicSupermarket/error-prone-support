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
                  "The Refaster template contains a method without any Refaster annotations"));

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.errorprone.refaster.annotation.AfterTemplate;",
            "import com.google.errorprone.refaster.annotation.AlsoNegation;",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "import java.util.Map;",
            "",
            "class A {",
            "  // BUG: Diagnostic matches: X",
            "  static final class MethodLacksBeforeTemplateAnnotation {",
            "    @BeforeTemplate",
            "    boolean before1(String string) {",
            "      return string.equals(\"\");",
            "    }",
            "",
            "    // @BeforeTemplate is missing",
            "    boolean before2(String string) {",
            "      return string.length() == 0;",
            "    }",
            "",
            "    @AfterTemplate",
            "    @AlsoNegation",
            "    boolean after(String string) {",
            "      return string.isEmpty();",
            "    }",
            "  }",
            "",
            "  // BUG: Diagnostic matches: X",
            "  static final class MethodLacksAfterTemplateAnnotation {",
            "    @BeforeTemplate",
            "    boolean before(String string) {",
            "      return string.equals(\"\");",
            "    }",
            "",
            "    // @AfterTemplate is missing",
            "    boolean after(String string) {",
            "      return string.isEmpty();",
            "    }",
            "  }",
            "",
            "  // BUG: Diagnostic matches: X",
            "  abstract class MethodLacksPlaceholderAnnotation<K, V> {",
            "    // @Placeholder is missing",
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
            "      map.computeIfAbsent(key, k -> function(k));",
            "    }",
            "  }",
            "",
            "  static final class ValidRefasterTemplate {",
            "    @BeforeTemplate",
            "    void unusedPureFunctionCall(Object o) {",
            "      o.toString();",
            "    }",
            "  }",
            "",
            "  static final class NotARefasterTemplate {",
            "    @Override",
            "    public String toString() {",
            "      return \"This is not a Refaster template\";",
            "    }",
            "  }",
            "}")
        .doTest();
  }
}
