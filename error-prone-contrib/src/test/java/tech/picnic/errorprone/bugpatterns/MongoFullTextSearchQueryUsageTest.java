package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MongoFullTextSearchQueryUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(MongoFullTextSearchQueryUsage.class, getClass())
        .addSourceLines(
            "A.java",
            "import com.mongodb.client.model.Filters;",
            "import com.mongodb.client.model.TextSearchOptions;",
            "",
            "class A {",
            "  void m() {",
            "    Filters.eq(\"foo\", \"bar\");",
            "    // BUG: Diagnostic contains:",
            "    Filters.text(\"foo\");",
            "    // BUG: Diagnostic contains:",
            "    Filters.text(\"foo\", new TextSearchOptions());",
            "    // BUG: Diagnostic contains:",
            "    Filters.text(\"foo\", new TextSearchOptions().caseSensitive(true));",
            "  }",
            "}")
        .doTest();
  }
}
