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
            "import org.bson.conversions.Bson;",
            "",
            "class A {",
            "",
            "  void m() {",
            "    Bson allowed = Filters.eq(\"a\", \"b\");",
            "    // BUG: Diagnostic contains:",
            "    Bson textSearch = Filters.text(\"Some text\");",
            "    // BUG: Diagnostic contains:",
            "    Bson textSearch2 = Filters.text(\"Some text\", new TextSearchOptions().caseSensitive(true));",
            "  }",
            "}")
        .doTest();
  }
}
