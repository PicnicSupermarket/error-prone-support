package tech.picnic.errorprone.refasterrules;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.openrewrite.java.Assertions.java;

import com.google.common.io.Resources;
import java.io.IOException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

// XXX: This class currently validates the OpenRewrite recipe generation by applying a single
// recipe. Generalize this setup to cover all generated recipes (for _all_ Refaster rule
// collections), ideally by reusing the `RefasterRulesTest` test resources. (This may introduce
// additional hurdles, as OpenRewrite removes obsolete imports, while Refaster doesn't.)
final class StringRulesRecipesTest implements RewriteTest {
  @Override
  public void defaults(RecipeSpec spec) {
    spec.recipe(new StringRulesRecipes());
  }

  @Test
  void stringValueOf() {
    // XXX: Use text blocks once supported.
    rewriteRun(
        java(
            "import java.util.Objects;\n"
                + '\n'
                + "class Test {\n"
                + "  String test(Object object) {\n"
                + "    return Objects.toString(object);\n"
                + "  }\n"
                + '}',
            "class Test {\n"
                + "  String test(Object object) {\n"
                + "    return String.valueOf(object);\n"
                + "  }\n"
                + '}'));
  }

  @Disabled("Not all rules are currently supported")
  @Test
  void allRules() throws IOException {
    rewriteRun(
        spec ->
            spec.parser(JavaParser.fromJavaVersion().classpath("guava", "refaster-test-support")),
        java(
            loadResource("StringRulesTestInput.java"), loadResource("StringRulesTestOutput.java")));
  }

  private String loadResource(String resource) throws IOException {
    return Resources.toString(Resources.getResource(getClass(), resource), UTF_8);
  }
}
