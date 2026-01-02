package tech.picnic.errorprone.refaster.runner;

import static com.google.common.base.Preconditions.checkState;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.refaster.RefasterRule;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import tech.picnic.errorprone.refaster.AnnotatedCompositeCodeTransformer;

final class RefasterRuleIdentifierExtractorTest {
  @Test
  void extractIdentifiersFromSimpleMethodCallRule() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$SimpleMethodCallRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    assertThat(identifiers).containsExactly(ImmutableSet.of("&&", "==", "hashCode", "isEmpty"));
  }

  @Test
  void extractIdentifiersFromBinaryOperatorRule() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$BinaryOperatorRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    assertThat(identifiers).containsExactly(ImmutableSet.of("+", ">", "&&", "*", "<"));
  }

  @Test
  void extractIdentifiersFromMemberSelectRule() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$MemberSelectRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    assertThat(identifiers).containsExactly(ImmutableSet.of("append"));
  }

  @Test
  void extractIdentifiersFromMemberReferenceRule() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$MemberReferenceRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    assertThat(identifiers).containsExactly(ImmutableSet.of("<init>", "Object"));
  }

  @Test
  void extractIdentifiersFromFooRules() {
    RefasterRule<?, ?> rule = getRefasterRule("FooRules$StringOfSizeZeroRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    assertThat(identifiers).containsExactly(ImmutableSet.of("toCharArray", "length", "=="));
  }

  @Test
  void extractIdentifiersFromBlockTemplate() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$BlockTemplateRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    assertThat(identifiers)
        .containsExactly(ImmutableSet.of("contains", "add", "!", "&&", "size", "<", "remove"));
  }

  @Test
  void extractIdentifiersFromSingleAnyOf() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$SingleAnyOfRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    assertThat(identifiers)
        .containsOnly(
            ImmutableSet.of("equals", "&&", "hashCode", "=="),
            ImmutableSet.of("isEmpty", "==", "&&", "hashCode"));
  }

  @Test
  void extractIdentifiersFromNestedAnyOf() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$NestedAnyOfRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    assertThat(identifiers)
        .containsOnly(
            ImmutableSet.of("==", "length"),
            ImmutableSet.of("==", "length", "+"),
            ImmutableSet.of("==", "length", "-"));
  }

  @Test
  void extractIdentifiersFromMultipleBeforeTemplates() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$MultipleBeforeTemplatesRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    assertThat(identifiers)
        .containsOnly(
            ImmutableSet.of("isEmpty", "&&", "length", "=="),
            ImmutableSet.of("length", ">", "&&", "!="));
  }

  private static RefasterRule<?, ?> getRefasterRule(String ruleName) {
    ImmutableListMultimap<String, CodeTransformer> transformers =
        CodeTransformers.getAllCodeTransformers();

    checkState(
        transformers.containsKey(ruleName),
        "Could not find RefasterRule: %s. Available rules: %s",
        ruleName,
        transformers.keySet());

    return transformers.get(ruleName).stream()
        .flatMap(
            transformer -> {
              if (transformer instanceof AnnotatedCompositeCodeTransformer annotated) {
                return annotated.transformers().stream();
              }
              return Stream.of(transformer);
            })
        .findFirst()
        .filter(RefasterRule.class::isInstance)
        .map(RefasterRule.class::cast)
        .orElseThrow(
            () -> new IllegalStateException("Could not find RefasterRule '%s".formatted(ruleName)));
  }
}
