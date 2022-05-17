package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class RefasterCheckTest {
  /** The names of all Refaster template groups defined in this module. */
  private static final ImmutableSet<String> TEMPLATE_GROUPS =
      ImmutableSet.of(
          "AssertJ",
          "AssertJBigDecimal",
          "AssertJBigInteger",
          "AssertJBoolean",
          "AssertJByte",
          "AssertJCharSequence",
          "AssertJDouble",
          "AssertJEnumerable",
          "AssertJException",
          "AssertJFloat",
          "AssertJInteger",
          "AssertJLong",
          "AssertJNumber",
          "AssertJMap",
          "AssertJObject",
          "AssertJOptional",
          "AssertJShort",
          "AssertJString",
          "AssertJThrowingCallable",
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
   * Matches the parts of the fully-qualified name of a template class that should be removed in
   * order to produce the associated {@link #TEMPLATE_GROUPS template group name}.
   */
  private static final Pattern TEMPLATE_FQCN_TRIM_FOR_GROUP_NAME =
      Pattern.compile(".*\\.|Templates\\$.*");
  /**
   * A mapping from template group names to associated template names.
   *
   * <p>In effect, the values correspond to nested classes that represent individual Refaster
   * templates, while the keys correspond to the associated top-level "aggregator" classes.
   */
  private static final ImmutableSetMultimap<String, String> TEMPLATES_BY_GROUP =
      indexTemplateNamesByGroup(RefasterCheck.ALL_CODE_TRANSFORMERS.get().keySet());

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
        .map(e -> arguments(e.getKey(), e.getValue()));
  }

  /**
   * Verifies that {@link RefasterCheck#loadAllCodeTransformers} finds at least one code transformer
   * for all of the {@link #TEMPLATE_GROUPS}.
   *
   * <p>This test is just as much about ensuring that {@link #TEMPLATE_GROUPS} is exhaustive, so
   * that in turn {@link #replacement}'s coverage is exhaustive.
   */
  @Test
  void loadAllCodeTransformers() {
    assertThat(TEMPLATES_BY_GROUP.keySet()).hasSameElementsAs(TEMPLATE_GROUPS);
  }

  /**
   * Verifies for each of the {@link #TEMPLATE_GROUPS} that the associated code transformers have
   * the desired effect.
   */
  @MethodSource("templateGroupsUnderTest")
  @ParameterizedTest
  void replacement(String group) {
    verifyRefactoring(group, namePattern(group));
  }

  /**
   * Verifies that all loaded Refaster templates are covered by at least one test.
   *
   * <p>Note that this doesn't guarantee full coverage: this test cannot ascertain that all {@link
   * com.google.errorprone.refaster.Refaster#anyOf} branches are tested. Idem for {@link
   * com.google.errorprone.refaster.annotation.BeforeTemplate} methods in case there are multiple .
   */
  @MethodSource("templatesUnderTest")
  @ParameterizedTest
  void coverage(String group, String template) {
    assertThatCode(() -> verifyRefactoring(group, namePattern(group, template)))
        .withFailMessage(
            "Template %s does not affect the tests for group %s; is it tested?", template, group)
        .isInstanceOf(AssertionError.class)
        .hasMessageFindingMatch("^(diff|expected):");
  }

  private static ImmutableSetMultimap<String, String> indexTemplateNamesByGroup(
      ImmutableSet<String> templateNames) {
    return templateNames.stream()
        .collect(
            toImmutableSetMultimap(
                n -> TEMPLATE_FQCN_TRIM_FOR_GROUP_NAME.matcher(n).replaceAll(""), identity()));
  }

  private static String namePattern(String groupName, String excludedTemplate) {
    return "(?!" + Pattern.quote(excludedTemplate) + ')' + namePattern(groupName);
  }

  private static String namePattern(String groupName) {
    return Pattern.compile(Pattern.quote(groupName)) + "Templates.*";
  }

  private void verifyRefactoring(String groupName, String templateNamePattern) {
    createRestrictedRefactoringTestHelper(templateNamePattern)
        .addInput(groupName + "TemplatesTestInput.java")
        .addOutput(groupName + "TemplatesTestOutput.java")
        .doTest(TestMode.TEXT_MATCH);
  }

  private BugCheckerRefactoringTestHelper createRestrictedRefactoringTestHelper(
      String namePattern) {
    return BugCheckerRefactoringTestHelper.newInstance(RefasterCheck.class, getClass())
        .setArgs("-XepOpt:Refaster:NamePattern=" + namePattern);
  }
}
