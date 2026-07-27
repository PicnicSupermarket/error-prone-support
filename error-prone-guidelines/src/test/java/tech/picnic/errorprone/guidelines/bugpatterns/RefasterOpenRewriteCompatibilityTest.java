package tech.picnic.errorprone.guidelines.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class RefasterOpenRewriteCompatibilityTest {
  /**
   * A stub of a type that signals that the compilation under analysis is associated with
   * OpenRewrite recipe generation.
   */
  private static final String[] RECIPE_DESCRIPTOR_SOURCE = {
    "package org.openrewrite.java.template;", "", "public @interface RecipeDescriptor {}"
  };

  /** A stub of the recipes generated for the {@code pkg.FooRules} rule collection. */
  private static final String[] FOO_RULES_RECIPES_SOURCE = {
    "package pkg;",
    "",
    "public final class FooRulesRecipes {",
    "  public static final class WithRecipeRecipe {}",
    "",
    "  public static final class AnnotatedWithRecipeRecipe {}",
    "}"
  };

  @Test
  void identification() {
    CompilationTestHelper.newInstance(RefasterOpenRewriteCompatibility.class, getClass())
        .addSourceLines(
            "org/openrewrite/java/template/RecipeDescriptor.java", RECIPE_DESCRIPTOR_SOURCE)
        .addSourceLines("pkg/FooRulesRecipes.java", FOO_RULES_RECIPES_SOURCE)
        .addSourceLines(
            "pkg/FooRules.java",
            "package pkg;",
            "",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "import tech.picnic.errorprone.refaster.annotation.OpenRewriteIncompatible;",
            "",
            "final class FooRules {",
            "  static final class NonRefasterClass {",
            "    String method() {",
            "      return \"\";",
            "    }",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  @OpenRewriteIncompatible",
            "  static final class AnnotatedNonRefasterClass {",
            "    String method() {",
            "      return \"\";",
            "    }",
            "  }",
            "",
            "  static final class WithRecipe {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str.trim();",
            "    }",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  @OpenRewriteIncompatible",
            "  static final class AnnotatedWithRecipe {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str.trim();",
            "    }",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  static final class WithoutRecipe {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str.trim();",
            "    }",
            "  }",
            "",
            "  @OpenRewriteIncompatible",
            "  static final class AnnotatedWithoutRecipe {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str.trim();",
            "    }",
            "  }",
            "}")
        .addSourceLines(
            "pkg/BarRules.java",
            "package pkg;",
            "",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "final class BarRules {",
            "  // BUG: Diagnostic contains:",
            "  static final class RuleInCollectionWithoutRecipes {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str.trim();",
            "    }",
            "  }",
            "}")
        .addSourceLines(
            "pkg/TopLevelRule.java",
            "package pkg;",
            "",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "final class TopLevelRule {",
            "  @BeforeTemplate",
            "  String before(String str) {",
            "    return str.trim();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void identificationWithoutOpenRewriteOnClasspath() {
    CompilationTestHelper.newInstance(RefasterOpenRewriteCompatibility.class, getClass())
        .addSourceLines("pkg/FooRulesRecipes.java", FOO_RULES_RECIPES_SOURCE)
        .addSourceLines(
            "pkg/FooRules.java",
            "package pkg;",
            "",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "import tech.picnic.errorprone.refaster.annotation.OpenRewriteIncompatible;",
            "",
            "final class FooRules {",
            "  @OpenRewriteIncompatible",
            "  static final class AnnotatedWithRecipe {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str.trim();",
            "    }",
            "  }",
            "",
            "  static final class WithoutRecipe {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str.trim();",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(RefasterOpenRewriteCompatibility.class, getClass())
        .addInputLines(
            "org/openrewrite/java/template/RecipeDescriptor.java", RECIPE_DESCRIPTOR_SOURCE)
        .expectUnchanged()
        .addInputLines("pkg/FooRulesRecipes.java", FOO_RULES_RECIPES_SOURCE)
        .expectUnchanged()
        .addInputLines(
            "pkg/FooRules.java",
            "package pkg;",
            "",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "import tech.picnic.errorprone.refaster.annotation.OpenRewriteIncompatible;",
            "",
            "final class FooRules {",
            "  @OpenRewriteIncompatible",
            "  static final class WithRecipe {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str.trim();",
            "    }",
            "  }",
            "",
            "  static final class WithoutRecipe {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str.trim();",
            "    }",
            "  }",
            "}")
        .addOutputLines(
            "pkg/FooRules.java",
            "package pkg;",
            "",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "import tech.picnic.errorprone.refaster.annotation.OpenRewriteIncompatible;",
            "",
            "final class FooRules {",
            "  static final class WithRecipe {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str.trim();",
            "    }",
            "  }",
            "",
            "  @OpenRewriteIncompatible",
            "  static final class WithoutRecipe {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str.trim();",
            "    }",
            "  }",
            "}")
        .doTest();
  }
}
