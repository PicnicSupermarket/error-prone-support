package tech.picnic.errorprone.refasterrules;

import static org.openrewrite.java.Assertions.java;

import com.google.errorprone.FileObjects;
import java.io.IOException;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
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
        java( // This would be much better with text blocks
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

  @Test
  @Disabled("Not all rules are currently supported")
  void reuseStringRulesTestInputOutput() throws IOException {
    String rule = "StringRules";
    JavaFileObject before = FileObjects.forResource(getClass(), rule + "TestInput.java");
    JavaFileObject after = FileObjects.forResource(getClass(), rule + "TestOutput.java");
    rewriteRun(
        spec ->
            spec.parser(JavaParser.fromJavaVersion().classpath("guava", "refaster-test-support")),
        java(before.getCharContent(true).toString(), after.getCharContent(true).toString()));
  }
}
