package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Predicates.containsPattern;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MissingRefasterAnnotationTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(MissingRefasterAnnotation.class, getClass())
        .expectErrorMessage(
            "X",
            containsPattern("The Refaster rule contains a method without any Refaster annotations"))
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
            "  static final class ValidRefasterRule {",
            "    @BeforeTemplate",
            "    void unusedPureFunctionCall(Object o) {",
            "      o.toString();",
            "    }",
            "  }",
            "",
            "  static final class NotARefasterRule {",
            "    @Override",
            "    public String toString() {",
            "      return \"This is not a Refaster rule\";",
            "    }",
            "  }",
            "}")
        .doTest();
  }
}
