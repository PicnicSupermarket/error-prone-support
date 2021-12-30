package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.MoreCollectors.onlyElement;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.util.MoreAnnotations.getValue;

import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type.ClassType;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;

public final class RefasterTestsValidationsCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(RefasterTestValidationsCheck.class, getClass());

  static final ImmutableSet<String> TEMPLATE_GROUPS =
      ImmutableSet.of(
          // "AssertJ", --> This isn't a template, discuss with Stephan why that is in here.
          "AssertJBigDecimal",
          "AssertJBigInteger",
          "AssertJBoolean",
          "AssertJByte",
          "AssertJCharSequence",
          "AssertJDouble",
          "AssertJEnumerable",
          "AssertJFloat",
          "AssertJInteger",
          "AssertJLong",
          "AssertJNumber",
          //          "AssertJObject",
          //          "AssertJOptional",
          //          "AssertJShort",
          //          "AssertJString",
          //          "Assorted",
          //          "BigDecimal",
          //          "Collection",
          //          "Comparator",
          //          "DoubleStream",
          //          "Equality",
          //          "ImmutableList",
          //          "ImmutableListMultimap",
          //          "ImmutableMap",
          //          "ImmutableMultiset",
          //          "ImmutableSet",
          //          "ImmutableSetMultimap",
          //          "ImmutableSortedMap",
          //          "ImmutableSortedMultiset",
          //          "ImmutableSortedSet",
          //          "IntStream",
          //          "JUnit",
          //          "LongStream",
          //          "MapEntry",
          //          "Mockito",
          //          "Multimap",
          //          "Null",
          //          "Optional",
          //          "Primitive",
          //          "Reactor",
          //          "RxJava2Adapter",
          "Stream",
          "String",
          "TestNGToAssertJ",
          "Time",
          "WebClient");

  /**
   * Returns every known (template group name, template name) pair as a parameterized test argument.
   */
  @SuppressWarnings("UnusedMethod" /* Used as a `@MethodSource`. */)
  private static Stream<Arguments> templatesUnderTest() {
    return TEMPLATE_GROUPS.stream().map(Arguments::arguments);
  }

  @Test
  void classHasTemplateCollectionAnnotation() {
    compilationTestHelper
        .addSourceLines(
            "StringTemplatesTest.java",
            "import tech.picnic.errorprone.annotations.Template;",
            "",
            "// BUG: Diagnostic contains:",
            "final class StringTemplatesTest {",
            "  @Template(String.class)",
            "  void testString() { }",
            "}")
        .doTest();
  }

  @Test
  void classHasCorrectTemplateCollectionAnnotationValue() {
    compilationTestHelper
        .addSourceLines("StringTemplates.java", "package pkg; public class StringTemplates { }")
        .addSourceLines(
            "StringTemplatesTest.java",
            "import tech.picnic.errorprone.annotations.Template;",
            "import tech.picnic.errorprone.annotations.TemplateCollection;",
            "",
            "@TemplateCollection(String.class)",
            "// BUG: Diagnostic contains:",
            "final class StringTemplatesTest {",
            "  @Template(String.class)",
            "  void testString() { }",
            "}")
        .doTest();
  }

  @Test
  void methodHasPrefixTest() {
    compilationTestHelper
        .addSourceLines("StringTemplates.java", "package pkg; public class StringTemplates { }")
        .addSourceLines(
            "SimpleTemplatesTest.java",
            "package pkg;",
            "",
            "import tech.picnic.errorprone.annotations.TemplateCollection;",
            "",
            "@TemplateCollection(StringTemplates.class)",
            "final class StringTemplatesTest {",
            "  // BUG: Diagnostic contains:",
            "  void simpleMethodWithoutPrefix() { }",
            "}")
        .doTest();
  }

  @Test
  void methodHasTemplateAnnotation() {
    compilationTestHelper
        .addSourceLines("StringTemplates.java", "package pkg; public class StringTemplates { }")
        .addSourceLines(
            "SimpleTemplatesTest.java",
            "package pkg;",
            "",
            "import tech.picnic.errorprone.annotations.TemplateCollection;",
            "",
            "@TemplateCollection(StringTemplates.class)",
            "final class StringTemplatesTest {",
            "  // BUG: Diagnostic contains:",
            "  void testSimpleMethod() { }",
            "}")
        .doTest();
  }

  @Test
  void methodHasCorrectTemplateAnnotationValue() {
    compilationTestHelper
        .addSourceLines("StringTemplates.java", "package pkg; public class StringTemplates { }")
        .addSourceLines(
            "SimpleTemplatesTest.java",
            "package pkg;",
            "",
            "import tech.picnic.errorprone.annotations.TemplateCollection;",
            "import tech.picnic.errorprone.annotations.Template;",
            "",
            "@TemplateCollection(StringTemplates.class)",
            "final class StringTemplatesTest {",
            "  @Template(Integer.class)",
            "  // BUG: Diagnostic contains:",
            "  void testString() { }",
            "}")
        .doTest();
  }

  @Test
  void omitNumberSuffixOfMethodName() {
    compilationTestHelper
        .addSourceLines("StringTemplates.java", "package pkg; public class StringTemplates { }")
        .addSourceLines(
            "SimpleTemplatesTest.java",
            "package pkg;",
            "",
            "import tech.picnic.errorprone.annotations.TemplateCollection;",
            "import tech.picnic.errorprone.annotations.Template;",
            "",
            "@TemplateCollection(StringTemplates.class)",
            "final class StringTemplatesTest {",
            "  @Template(String.class)",
            "  void testString3() { }",
            "}")
        .doTest();
  }

  @Test
  void correctTestCollection() {
    compilationTestHelper
        .addSourceLines("StringTemplates.java", "package pkg; public class StringTemplates { }")
        .addSourceLines(
            "SimpleTemplatesTest.java",
            "package pkg;",
            "",
            "import tech.picnic.errorprone.annotations.TemplateCollection;",
            "import tech.picnic.errorprone.annotations.Template;",
            "",
            "@TemplateCollection(StringTemplates.class)",
            "final class StringTemplatesTest {",
            "  @Template(String.class)",
            "  void testString() { }",
            "}")
        .doTest();
  }

  @ParameterizedTest
  @MethodSource("templatesUnderTest")
  void validateAllTestMethods(String group) {
    verifyRefactoring(group);
  }

  private void verifyRefactoring(String groupName) {
    BugCheckerRefactoringTestHelper.newInstance(RefasterTestValidationsCheck.class, getClass())
        .addInput(groupName + "TemplatesTestInput.java")
        .expectUnchanged()
        .doTest(TestMode.TEXT_MATCH);
  }

  /**
   * A {@link BugChecker} that validates the *Templates{Input,Output} test resources for the
   * Refaster templates.
   */
  @BugPattern(
      name = "RefasterTestValidations",
      summary = "Flag incorrect set up of tests resources for Refaster templates.",
      linkType = LinkType.NONE,
      severity = ERROR)
  public static final class RefasterTestValidationsCheck extends BugChecker
      implements ClassTreeMatcher, MethodTreeMatcher {
    private static final long serialVersionUID = 1L;
    private static final String TEST_METHOD_PREFIX = "test";
    private static final Pattern LAST_TEST_OCCURRENCE = Pattern.compile("(?s)Test(?!.*?Test)");

    @Override
    public Description matchClass(ClassTree tree, VisitorState state) {
      // XXX: Check package name to decide whether we should analyze, instead of using the
      // `TemplatesTest` suffix.
      String className = tree.getSimpleName().toString();
      if (!className.contains("TemplatesTest")) {
        return Description.NO_MATCH;
      }

      if (!ASTHelpers.hasAnnotation(tree, TemplateCollection.class, state)
          || templateAnnotationHasIncorrectValue(
              ASTHelpers.getSymbol(tree), className, TemplateCollection.class)) {
        return describeMatch(
            tree,
            SuggestedFix.prefixWith(
                tree,
                String.format("@TemplateCollection(\"%s\".class)\n", dropTestSuffix(className))));
      }
      return Description.NO_MATCH;
    }

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      // XXX: Verify that you are in a class that is a test for a collection?
      if (ASTHelpers.hasAnnotation(tree, Override.class, state)) {
        return Description.NO_MATCH;
      }

      MethodSymbol symbol = ASTHelpers.getSymbol(tree);
      @Var String methodName = symbol.getSimpleName().toString();
      methodName = dropTrailingNumbers(methodName);
      boolean containsPrefix = methodName.startsWith(TEST_METHOD_PREFIX);
      if (!containsPrefix) {
        return describeMatch(
            tree,
            SuggestedFixes.renameMethod(
                tree, TEST_METHOD_PREFIX + capitalizeFirstLetter(methodName), state));
      }

      boolean hasTemplateAnnotation = ASTHelpers.hasAnnotation(tree, Template.class, state);
      if (!hasTemplateAnnotation
          || templateAnnotationHasIncorrectValue(symbol, methodName, Template.class)) {
        String expectedTemplateName = methodName.replaceFirst(TEST_METHOD_PREFIX, "");
        return describeMatch(
            tree,
            SuggestedFix.prefixWith(
                tree, String.format("@Template(\"%s\".class)\n", expectedTemplateName)));
      }
      return Description.NO_MATCH;
    }

    private static boolean templateAnnotationHasIncorrectValue(
        Symbol symbol, String nameToCompare, Class<?> clazz) {
      String fullyQualifiedClassOfAnnotationValue =
          getClassFromAnnotation(symbol, clazz.getName()).toString();
      String expectedAnnotationValue =
          fullyQualifiedClassOfAnnotationValue.substring(
              fullyQualifiedClassOfAnnotationValue.lastIndexOf('.') + 1);
      String finalName =
          symbol instanceof MethodSymbol
              ? nameToCompare.replace(TEST_METHOD_PREFIX, "")
              : dropTestSuffix(nameToCompare);
      return !finalName.endsWith(expectedAnnotationValue);
    }

    private static ClassType getClassFromAnnotation(Symbol symbol, String name) {
      Attribute.Compound templateClass =
          symbol.getRawAttributes().stream()
              .filter(a -> a.type.tsym.getQualifiedName().contentEquals(name))
              .collect(onlyElement());
      return (ClassType) getValue(templateClass, "value").orElseThrow().getValue();
    }

    private static String capitalizeFirstLetter(String s) {
      return Ascii.toUpperCase(s.substring(0, 1)) + s.substring(1);
    }

    private static String dropTrailingNumbers(String methodName) {
      return methodName.replaceAll("\\d*$", "");
    }

    private static String dropTestSuffix(String text) {
      return LAST_TEST_OCCURRENCE.matcher(text).replaceFirst("");
    }
  }
}
