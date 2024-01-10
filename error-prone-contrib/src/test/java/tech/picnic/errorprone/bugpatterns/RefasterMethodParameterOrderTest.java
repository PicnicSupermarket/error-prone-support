package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class RefasterMethodParameterOrderTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(RefasterMethodParameterOrder.class, getClass())
        .addSourceLines(
            "A.java",
            "import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "import com.google.errorprone.refaster.annotation.AfterTemplate;",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "import com.google.errorprone.refaster.annotation.Placeholder;",
            "import com.google.errorprone.refaster.annotation.UseImportPolicy;",
            "import java.util.Map;",
            "",
            "class A {",
            "  class UnusedLexicographicallyOrderedParameters {",
            "    @BeforeTemplate",
            "    void singleParam(int a) {}",
            "",
            "    @BeforeTemplate",
            "    void twoParams(int a, int b) {}",
            "",
            "    @Placeholder",
            "    void notATemplateMethod(int b, int a) {}",
            "  }",
            "",
            "  class NonParameterValueIdentifierIsIgnored<K, V> {",
            "    @AfterTemplate",
            "    @UseImportPolicy(value = STATIC_IMPORT_ALWAYS)",
            "    void after(Map<K, V> map, V value) {",
            "      assertThat(map).containsValue(value);",
            "    }",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  class UnusedLexicographicallyUnorderedParameters {",
            "    @BeforeTemplate",
            "    void foo(int a, int b) {}",
            "",
            "    @BeforeTemplate",
            "    void bar(int b, int a) {}",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    // XXX: Drop the package declaration once OpenRewrite properly handles Refaster rules in the
    // unnamed package. See https://github.com/openrewrite/rewrite-templating/pull/64.
    BugCheckerRefactoringTestHelper.newInstance(RefasterMethodParameterOrder.class, getClass())
        .addInputLines(
            "pkg/A.java",
            "package pkg;",
            "",
            "import com.google.errorprone.refaster.annotation.AfterTemplate;",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "class A {",
            "  class UnusedUnsortedParameters {",
            "    @BeforeTemplate",
            "    void before(int b, int a) {}",
            "  }",
            "",
            "  class UnsortedParametersWithoutAfterTemplate {",
            "    @BeforeTemplate",
            "    int before(int a, int b, int c, int d) {",
            "      return b + a + d + b + c;",
            "    }",
            "  }",
            "",
            "  class UnsortedParametersWithMultipleMethodsAndParameterCounts {",
            "    @BeforeTemplate",
            "    int before(int b, int a, int g, int f, int d) {",
            "      return f + a + g + b + d;",
            "    }",
            "",
            "    @AfterTemplate",
            "    int after(int a, int b) {",
            "      return b + a;",
            "    }",
            "",
            "    @AfterTemplate",
            "    int after2(int a, int d, int f) {",
            "      return d + a + f;",
            "    }",
            "  }",
            "}")
        .addOutputLines(
            "pkg/A.java",
            "package pkg;",
            "",
            "import com.google.errorprone.refaster.annotation.AfterTemplate;",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "class A {",
            "  class UnusedUnsortedParameters {",
            "    @BeforeTemplate",
            "    void before(int a, int b) {}",
            "  }",
            "",
            "  class UnsortedParametersWithoutAfterTemplate {",
            "    @BeforeTemplate",
            "    int before(int b, int a, int d, int c) {",
            "      return b + a + d + b + c;",
            "    }",
            "  }",
            "",
            "  class UnsortedParametersWithMultipleMethodsAndParameterCounts {",
            "    @BeforeTemplate",
            "    int before(int d, int a, int f, int b, int g) {",
            "      return f + a + g + b + d;",
            "    }",
            "",
            "    @AfterTemplate",
            "    int after(int a, int b) {",
            "      return b + a;",
            "    }",
            "",
            "    @AfterTemplate",
            "    int after2(int d, int a, int f) {",
            "      return d + a + f;",
            "    }",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
