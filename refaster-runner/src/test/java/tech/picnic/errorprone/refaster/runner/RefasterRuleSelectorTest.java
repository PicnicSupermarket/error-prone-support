package tech.picnic.errorprone.refaster.runner;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.MoreCollectors.toOptional;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.CompositeCodeTransformer;
import com.google.errorprone.refaster.RefasterRule;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import tech.picnic.errorprone.refaster.AnnotatedCompositeCodeTransformer;

final class RefasterRuleSelectorTest {
  private static final ImmutableListMultimap<String, CodeTransformer> CODE_TRANSFORMERS =
      CodeTransformers.getAllCodeTransformers();

  @Test
  void indexRuleIdentifiersWithSingleRule() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$SimpleMethodCallRule");
    Map<CodeTransformer, ImmutableSet<ImmutableSet<String>>> indexed =
        RefasterRuleSelector.indexRuleIdentifiers(ImmutableSet.of(rule));

    assertThat(indexed).hasSize(1);
    assertThat(indexed.get(rule))
        .containsExactly(ImmutableSet.of("&&", "==", "hashCode", "isEmpty"));
  }

  @Test
  void indexRuleIdentifiersWithMultipleRules() {
    RefasterRule<?, ?> rule1 =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$SimpleMethodCallRule");
    RefasterRule<?, ?> rule2 =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$BinaryOperatorRule");
    Map<CodeTransformer, ImmutableSet<ImmutableSet<String>>> indexed =
        RefasterRuleSelector.indexRuleIdentifiers(ImmutableSet.of(rule1, rule2));

    assertThat(indexed.get(rule1))
        .containsExactly(ImmutableSet.of("&&", "==", "hashCode", "isEmpty"));
    assertThat(indexed.get(rule2)).containsExactly(ImmutableSet.of("+", ">", "&&", "*", "<"));
  }

  @Test
  void indexRuleIdentifiersWithSingleAnyOf() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$SingleAnyOfRule");
    Map<CodeTransformer, ImmutableSet<ImmutableSet<String>>> indexed =
        RefasterRuleSelector.indexRuleIdentifiers(ImmutableSet.of(rule));

    assertThat(indexed).hasSize(1);
    ImmutableSet<ImmutableSet<String>> identifiers = indexed.get(rule);

    assertThat(identifiers)
        .containsOnly(
            ImmutableSet.of("equals", "&&", "hashCode", "=="),
            ImmutableSet.of("isEmpty", "==", "&&", "hashCode"));
  }

  @Test
  void indexRuleIdentifiersWithNestedAnyOf() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$NestedAnyOfRule");
    Map<CodeTransformer, ImmutableSet<ImmutableSet<String>>> indexed =
        RefasterRuleSelector.indexRuleIdentifiers(ImmutableSet.of(rule));

    ImmutableSet<ImmutableSet<String>> identifiers = indexed.get(rule);

    assertThat(identifiers)
        .containsOnly(
            ImmutableSet.of("==", "length"),
            ImmutableSet.of("==", "length", "+"),
            ImmutableSet.of("==", "length", "-"));
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

    assertThat(indexed).hasSameSizeAs(testRuleTransformers);
    assertThat(simpleIdentifiers)
        .containsExactly(ImmutableSet.of("&&", "==", "hashCode", "isEmpty"));
  }

  // XXX: Add AlsoNegation example, with boolean where you need negation.
  // One complex expression. with an !
  // Parameterized test for the RefasterRules.
  private static RefasterRule<?, ?> getRefasterRule(String ruleName) {
    return CODE_TRANSFORMERS.get(ruleName).stream()
        .flatMap(
            transformer -> {
              if (transformer instanceof AnnotatedCompositeCodeTransformer annotated) {
                return annotated.transformers().stream();
              }
              return Stream.of(transformer);
            })
        .collect(toOptional())
        .filter(RefasterRule.class::isInstance)
        .map(RefasterRule.class::cast)
        .orElseThrow(
            () ->
                new IllegalStateException("Could not find RefasterRule '%s'".formatted(ruleName)));
  }
}
