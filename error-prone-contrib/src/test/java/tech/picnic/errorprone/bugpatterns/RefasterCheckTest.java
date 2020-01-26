package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.ErrorProneFlags;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class RefasterCheckTest {
  /** The names of all Refaster template groups defined in this module. */
  private static final ImmutableSet<String> TEMPLATE_GROUPS =
      ImmutableSet.of(
          "AssertJ",
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
          "LongStream",
          "MapEntry",
          "Multimap",
          "Null",
          "Optional",
          "Primitive",
          "Reactor",
          "RxJava2Adapter",
          "Stream",
          "String",
          "TestNGToAssertJ",
          "Time");
  /**
   * A mapping from template group names to associated template names, as constructed by {@link
   * RefasterCheck} (i.e., by the code under test).
   *
   * <p>In effect, the values correspond to nested classes that represent individual Refaster
   * templates, while the keys correspond to the associated top-level "aggregator" classes.
   */
  private static final ImmutableSetMultimap<String, String> TEMPLATES_BY_GROUP =
      loadTemplateNames();

  /** Returns every known template group name as a parameterized test argument. */
  @SuppressWarnings("UnusedMethod" /* Used as a `@MethodSource`. */)
  private static Stream<Arguments> templateGroupsUnderTest() {
    // XXX: Drop the filter once we have added tests for AssertJ!
    return TEMPLATES_BY_GROUP.keySet().stream().filter(not("AssertJ"::equals)).map(Arguments::of);
  }

  /**
   * Returns every known (template group name, template name) pair as a parameterized test argument.
   */
  @SuppressWarnings("UnusedMethod" /* Used as a `@MethodSource`. */)
  private static Stream<Arguments> templatesUnderTest() {
    // XXX: Drop the filter once we have added tests for AssertJ!
    return TEMPLATES_BY_GROUP.entries().stream()
        .filter(e -> !"AssertJ".equals(e.getKey()))
        .map(e -> Arguments.of(e.getKey(), e.getValue()));
  }

  /**
   * Verifies that {@link RefasterCheck#loadAllCodeTransformers} finds at least one code transformer
   * for all of the {@link #TEMPLATE_GROUPS}.
   *
   * <p>This test is just as much about ensuring that {@link #TEMPLATE_GROUPS} is exhaustive, so
   * that in turn {@link #testReplacement}'s coverage is exhaustive.
   */
  @Test
  public void testLoadAllCodeTransformers() {
    assertThat(TEMPLATES_BY_GROUP.keySet()).containsExactlyInAnyOrderElementsOf(TEMPLATE_GROUPS);
  }

  /**
   * Verifies for each of the {@link #TEMPLATE_GROUPS} that the associated code transformers have
   * the desired effect.
   */
  @ParameterizedTest
  @MethodSource("templateGroupsUnderTest")
  // XXX: Until https://github.com/google/error-prone/pull/1239 is merged and released this test
  // will only pass when using the Picnic Error Prone fork.
  @DisabledIfSystemProperty(named = "error-prone-version", matches = "2\\.3\\.4")
  public void testReplacement(String group) {
    verifyRefactoring(group, namePattern(group));
  }

  /**
   * Verifies that all loaded Refaster templates are covered by at least one test.
   *
   * <p>Note that this doesn't guarantee full coverage: this test cannot ascertain that all {@link
   * com.google.errorprone.refaster.Refaster#anyOf} branches are tested. Idem for {@link
   * com.google.errorprone.refaster.annotation.BeforeTemplate} methods in case there are multiple .
   */
  @ParameterizedTest
  @MethodSource("templatesUnderTest")
  // XXX: Until https://github.com/google/error-prone/pull/1239 is merged and released this test
  // will only pass when using the Picnic Error Prone fork.
  @DisabledIfSystemProperty(named = "error-prone-version", matches = "2\\.3\\.4")
  public void testCoverage(String group, String template) {
    assertThatCode(() -> verifyRefactoring(group, namePattern(group, template)))
        .withFailMessage(
            "Template %s does not affect the tests for group %s; is it tested?", template, group)
        .isInstanceOf(AssertionError.class)
        .hasMessageFindingMatch("^(diff|expected):");
  }

  private static ImmutableSetMultimap<String, String> loadTemplateNames() {
    Pattern toTrim = Pattern.compile(".*\\.|Templates\\$.*");

    return RefasterCheck.loadAllCodeTransformers().keySet().stream()
        .collect(toImmutableSetMultimap(n -> toTrim.matcher(n).replaceAll(""), identity()));
  }

  private static String namePattern(String groupName, String excludedTemplate) {
    return "(?!" + Pattern.quote(excludedTemplate) + ')' + namePattern(groupName);
  }

  private static String namePattern(String groupName) {
    return Pattern.compile(groupName) + "Templates.*";
  }

  private void verifyRefactoring(String groupName, String templateNamePattern) {
    createRestrictedRefactoringTestHelper(templateNamePattern)
        .addInput(groupName + "TemplatesTestInput.java")
        .addOutput(groupName + "TemplatesTestOutput.java")
        .doTest(TestMode.TEXT_MATCH);
  }

  private BugCheckerRefactoringTestHelper createRestrictedRefactoringTestHelper(
      String namePattern) {
    return BugCheckerRefactoringTestHelper.newInstance(
        new RefasterCheck(
            ErrorProneFlags.fromMap(ImmutableMap.of("Refaster:NamePattern", namePattern))),
        getClass());
  }
}
