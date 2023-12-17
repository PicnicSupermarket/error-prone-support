package tech.picnic.errorprone.refasterrules;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class StringRulesRecipesTest implements RewriteTest {

  @Override
  public void defaults(RecipeSpec spec) {
    spec.recipe(new StringRulesRecipes());
  }

  @Test
  void stringValueOf() {
    rewriteRun(
        // language=java
        java(
            "import java.util.Objects;\n"
                + "class Test {\n"
                + "    String test(Object object) {\n"
                + "        return Objects.toString(object);\n"
                + "    }\n"
                + "}",
            "class Test {\n"
                + "    String test(Object object) {\n"
                + "        return String.valueOf(object);\n"
                + "    }\n"
                + "}"));
  }
}
