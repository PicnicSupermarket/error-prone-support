package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.MoreCollectors.onlyElement;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.util.MoreAnnotations.getValue;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;

public final class RefasterTestAnnotationsCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(TestChecker2.class, getClass());

  static final ImmutableSet<String> TEMPLATE_GROUPS =
      ImmutableSet.of(
          // "AssertJ", --> This isn't a template
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
          "AssertJObject",
          "AssertJOptional",
          "AssertJShort",
          "AssertJString",
          "Assorted",
          "BigDecimal",
          "Collection",
          "Comparator",
          "DoubleStream",
          "Equality",
          "ImmutableList",
          "ImmutableListMultimap",
          "ImmutableMap",
          "ImmutableMultiset",
          "ImmutableSet",
          "ImmutableSetMultimap",
          "ImmutableSortedMap",
          "ImmutableSortedMultiset",
          "ImmutableSortedSet",
          "IntStream",
          "JUnit",
          "LongStream",
          "MapEntry",
          "Mockito",
          "Multimap",
          "Null",
          "Optional",
          "Primitive",
          "Reactor",
          "RxJava2Adapter",
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
  void classHasCorrectTemplateCollectionAnnotation() {
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
  void verifyMethodHasTestPrefix() {
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
  void dontUseNumberSuffixOfMethodName() {
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
  void methodIsCorrectlyAnnotated() {
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
    createRestrictedRefactoringTestHelper()
        .addInput(groupName + "TemplatesTestInput.java")
        .expectUnchanged()
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }

  private BugCheckerRefactoringTestHelper createRestrictedRefactoringTestHelper() {
    return BugCheckerRefactoringTestHelper.newInstance(TestChecker2.class, getClass());
  }

  /** A {@link BugChecker} which simply delegates to {@link TestChecker2}. */
  @BugPattern(
      name = "TestChecker2",
      summary = "Flag incorrect set up of tests",
      linkType = LinkType.NONE,
      severity = ERROR)
  public static final class TestChecker2 extends BugChecker
      implements BugChecker.ClassTreeMatcher, BugChecker.MethodTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchClass(ClassTree tree, VisitorState state) {
      // XXX: Check package name to decide whether we should analyze, instead of using the
      // `TemplatesTest` suffix.
      if (!tree.getSimpleName().toString().contains("TemplatesTest")) {
        return Description.NO_MATCH;
      }

      if (!ASTHelpers.hasAnnotation(tree, TemplateCollection.class, state)
          || templateAnnotationHasIncorrectValue(
              ASTHelpers.getSymbol(tree),
              tree.getSimpleName().toString(),
              TemplateCollection.class)) {
        Symbol.ClassSymbol symbol = ASTHelpers.getSymbol(tree);
        return describeMatch(
            tree,
            SuggestedFix.prefixWith(
                tree,
                "@TemplateCollection("
                    + replaceLast(symbol.getSimpleName().toString(), "Test", "")
                    + ".class) \n"));
      }
      return Description.NO_MATCH;
    }

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      // XXX: Verify that you are in a class that is a test for a collection?
      if (ASTHelpers.hasAnnotation(tree, Override.class, state)) {
        return Description.NO_MATCH;
      }

      Symbol.MethodSymbol symbol = ASTHelpers.getSymbol(tree);
      @Var String methodName = symbol.getSimpleName().toString();
      methodName = removeTrailingNumbersIfPresent(methodName);
      boolean containsPrefix = methodName.startsWith("test");
      if (!containsPrefix) {
        return describeMatch(
            tree,
            SuggestedFixes.renameMethod(tree, "test" + capitalizeFirstLetter(methodName), state));
      }

      boolean hasTemplateAnnotation = ASTHelpers.hasAnnotation(tree, Template.class, state);
      if (!hasTemplateAnnotation
          || templateAnnotationHasIncorrectValue(symbol, methodName, Template.class)) {
        return describeMatch(
            tree,
            SuggestedFix.prefixWith(
                tree, "@Template(" + methodName.replace("test", "") + ".class) \n"));
      }
      return Description.NO_MATCH;
    }

    private static boolean templateAnnotationHasIncorrectValue(
        Symbol symbol, String nameToCompare, Class<?> clazz) {
      String fullyQualifiedAnnotationValue =
          getClassFromAnnotation(symbol, clazz.getName()).toString();
      String annotationValue =
          fullyQualifiedAnnotationValue.substring(
              fullyQualifiedAnnotationValue.lastIndexOf('.') + 1);
      String finalName =
          symbol instanceof Symbol.MethodSymbol
              ? nameToCompare.replace("test", "")
              : replaceLast(nameToCompare, "Test", "");
      return !finalName.endsWith(annotationValue);
    }

    private static Type.ClassType getClassFromAnnotation(Symbol symbol, String name) {
      Attribute.Compound templateClass =
          symbol.getRawAttributes().stream()
              .filter(a -> a.type.tsym.getQualifiedName().contentEquals(name))
              .collect(onlyElement());
      return (Type.ClassType) getValue(templateClass, "value").orElseThrow().getValue();
    }

    private static String capitalizeFirstLetter(String s) {
      return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private static String removeTrailingNumbersIfPresent(String methodName) {
      return methodName.replaceAll("\\d*$", "");
    }

    private static String replaceLast(String text, String regex, String replacement) {
      return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
    }
  }
}
