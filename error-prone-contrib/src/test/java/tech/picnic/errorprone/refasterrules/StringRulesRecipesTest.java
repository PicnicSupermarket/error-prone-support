package tech.picnic.errorprone.refasterrules;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.condition.JRE.JAVA_27;
import static org.openrewrite.java.Assertions.java;

import com.google.common.io.Resources;
import java.io.IOException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

// XXX: This class currently validates the OpenRewrite recipe generation by applying a single
// recipe. Generalize this setup to cover all generated recipes (for _all_ Refaster rule
// collections), ideally by reusing the `RefasterRulesTest` test resources. (This may introduce
// additional hurdles, as OpenRewrite removes obsolete imports, while Refaster doesn't.)
// XXX: Revisit this condition once OpenRewrite provides a Java parser compatible with JDK 27+.
// See https://github.com/openrewrite/rewrite/issues/8991.
@DisabledForJreRange(
    min = JAVA_27,
    disabledReason = "OpenRewrite's Java parsers rely on javac internals removed in JDK 27")
final class StringRulesRecipesTest implements RewriteTest {
  @Override
  public void defaults(RecipeSpec spec) {
    spec.recipe(new StringRulesRecipes());
  }

  // XXX: Instead of suppressing `S2699`, consider registering
  // `org.openrewrite.test.RewriteTest#rewriteRun` and related methods with
  // https://github.com/SonarSource/sonar-java.
  @SuppressWarnings("java:S2699" /* The `rewriteRun` method performs an assertion. */)
  @Test
  void stringValueOf() {
    rewriteRun(
        java(
            """
            import java.util.Objects;

            class Test {
              String test(Object object) {
                return Objects.toString(object);
              }
            }
            """,
            """
            class Test {
              String test(Object object) {
                return String.valueOf(object);
              }
            }
            """));
  }

  // XXX: Instead of suppressing `S2699`, consider registering
  // `org.openrewrite.test.RewriteTest#rewriteRun` and related methods with
  // https://github.com/SonarSource/sonar-java.
  @Disabled("Not all rules are currently supported")
  @SuppressWarnings("java:S2699" /* The `rewriteRun` method performs an assertion. */)
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
