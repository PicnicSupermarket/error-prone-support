package tech.picnic.errorprone.refaster.runner;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.CompositeCodeTransformer;
import com.google.errorprone.refaster.RefasterRule;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.picnic.errorprone.refaster.AnnotatedCompositeCodeTransformer;

final class RefasterRuleSelectorTest {
  private static final ImmutableListMultimap<String, CodeTransformer> CODE_TRANSFORMERS =
      CodeTransformers.getAllCodeTransformers();

  private static Stream<Arguments> indexRuleIdentifiersTestCases() {
    /* { ruleName, expectedIdentifiers } */
    return Stream.of(
        arguments(
            "RefasterRuleIdentifierExtractorTestRules$SimpleMethodCallRule",
            ImmutableSet.of(ImmutableSet.of("&&", "==", "hashCode", "isEmpty"))),
        arguments(
            "RefasterRuleIdentifierExtractorTestRules$BinaryOperatorRule",
            ImmutableSet.of(ImmutableSet.of("+", ">", "&&", "*", "<"))),
        arguments(
            "RefasterRuleIdentifierExtractorTestRules$SingleAnyOfRule",
            ImmutableSet.of(
                ImmutableSet.of("equals", "&&", "hashCode", "=="),
                ImmutableSet.of("isEmpty", "==", "&&", "hashCode"))),
        arguments(
            "RefasterRuleIdentifierExtractorTestRules$NestedAnyOfRule",
            ImmutableSet.of(
                ImmutableSet.of("==", "length"),
                ImmutableSet.of("==", "length", "+"),
                ImmutableSet.of("==", "length", "-"))));
  }

  @MethodSource("indexRuleIdentifiersTestCases")
  @ParameterizedTest
  void indexRuleIdentifiers(
      String ruleName, ImmutableSet<ImmutableSet<String>> expectedIdentifiers) {
    RefasterRule<?, ?> rule = getRefasterRule(ruleName);

    Map<CodeTransformer, ImmutableSet<ImmutableSet<String>>> indexed =
        RefasterRuleSelector.indexRuleIdentifiers(ImmutableSet.of(rule));

    assertThat(indexed)
        .hasEntrySatisfying(
            rule, identifiers -> assertThat(identifiers).hasSameElementsAs(expectedIdentifiers));
  }

  @Test
  void indexRuleIdentifiersWithCompositeCodeTransformer() {
    ImmutableListMultimap<String, CodeTransformer> transformers =
        CodeTransformers.getAllCodeTransformers();

    ImmutableSet<CodeTransformer> testRuleTransformers =
        transformers.entries().stream()
            .filter(e -> e.getKey().startsWith("RefasterRuleIdentifierExtractorTestRules$"))
            .map(Map.Entry::getValue)
            .collect(toImmutableSet());

    assertThat(testRuleTransformers).isNotEmpty();

    CodeTransformer composite = CompositeCodeTransformer.compose(testRuleTransformers);
    Map<CodeTransformer, ImmutableSet<ImmutableSet<String>>> indexed =
        RefasterRuleSelector.indexRuleIdentifiers(ImmutableSet.of(composite));

    RefasterRule<?, ?> simpleRule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$SimpleMethodCallRule");
    ImmutableSet<ImmutableSet<String>> simpleIdentifiers =
        indexed.entrySet().stream()
            .filter(
                e ->
                    e.getKey() instanceof AnnotatedCompositeCodeTransformer annotated
                        && annotated.transformers().contains(simpleRule))
            .findFirst()
            .map(Map.Entry::getValue)
            .orElseThrow();

    assertThat(simpleIdentifiers)
        .containsExactly(ImmutableSet.of("&&", "==", "hashCode", "isEmpty"));
  }

  @Test
  void indexRuleIdentifiersWithAlsoNegation() {
    String ruleName = "RefasterRuleIdentifierExtractorTestRules$AlsoNegationRule";

    ImmutableList<CodeTransformer> rules = ImmutableList.copyOf(getAllRefasterRules(ruleName));

    Map<CodeTransformer, ImmutableSet<ImmutableSet<String>>> identifiersByRule =
        RefasterRuleSelector.indexRuleIdentifiers(rules);

    assertThat(identifiersByRule.get(rules.getFirst()))
        .containsExactly(ImmutableSet.of("&&", "==", "Integer", "valueOf", "isEmpty"));
    assertThat(identifiersByRule.get(rules.getLast()))
        .containsExactly(ImmutableSet.of("||", "Integer", "!", "valueOf", "isEmpty", "!="));
  }

  private static ImmutableSet<RefasterRule<?, ?>> getAllRefasterRules(String ruleName) {
    ImmutableSet.Builder<RefasterRule<?, ?>> builder = ImmutableSet.builder();
    CODE_TRANSFORMERS.get(ruleName).stream()
        .flatMap(
            transformer -> {
              if (transformer instanceof AnnotatedCompositeCodeTransformer annotated) {
                return annotated.transformers().stream();
              }
              return Stream.of(transformer);
            })
        .filter(RefasterRule.class::isInstance)
        .map(RefasterRule.class::cast)
        .forEach(builder::add);
    return builder.build();
  }

  private static RefasterRule<?, ?> getRefasterRule(String ruleName) {
    return getAllRefasterRules(ruleName).stream()
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException("Could not find RefasterRule '%s'".formatted(ruleName)));
  }
}
