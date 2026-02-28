package tech.picnic.errorprone.refaster.runner;

import static com.google.common.collect.MoreCollectors.toOptional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.refaster.RefasterRule;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.picnic.errorprone.refaster.AnnotatedCompositeCodeTransformer;

final class RefasterRuleIdentifierExtractorTest {
  private static final ImmutableListMultimap<String, CodeTransformer> CODE_TRANSFORMERS =
      CodeTransformers.getAllCodeTransformers();

  private static Stream<Arguments> extractIdentifiersTestCases() {
    /* { ruleName, expectedIdentifiers } */
    return Stream.of(
        arguments(
            "SimpleMethodCallRule",
            ImmutableSet.of(ImmutableSet.of("&&", "==", "hashCode", "isEmpty"))),
        arguments("BinaryOperatorRule", ImmutableSet.of(ImmutableSet.of("+", ">", "&&", "*", "<"))),
        arguments("MemberSelectRule", ImmutableSet.of(ImmutableSet.of("append"))),
        arguments("MemberReferenceRule", ImmutableSet.of(ImmutableSet.of("<init>", "Object"))),
        arguments(
            "BlockTemplateRule",
            ImmutableSet.of(ImmutableSet.of("contains", "add", "!", "&&", "size", "<", "remove"))),
        arguments(
            "SingleAnyOfRule",
            ImmutableSet.of(
                ImmutableSet.of("equals", "&&", "hashCode", "=="),
                ImmutableSet.of("isEmpty", "==", "&&", "hashCode"))),
        arguments(
            "NestedAnyOfRule",
            ImmutableSet.of(
                ImmutableSet.of("==", "length"),
                ImmutableSet.of("==", "length", "+"),
                ImmutableSet.of("==", "length", "-"))),
        arguments(
            "MultipleBeforeTemplatesRule",
            ImmutableSet.of(
                ImmutableSet.of("isEmpty", "&&", "capacity", ">"),
                ImmutableSet.of("!", "isEmpty", "&&", "capacity", "=="))),
        arguments(
            "StaticImportAndFieldRule",
            ImmutableSet.of(
                ImmutableSet.of(
                    "ImmutableList", "of", "equals", "&&", "size", "==", "Integer", "MAX_VALUE"))));
  }

  @MethodSource("extractIdentifiersTestCases")
  @ParameterizedTest
  void extractIdentifiers(String ruleName, ImmutableSet<ImmutableSet<String>> expectedIdentifiers) {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$" + ruleName);
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    assertThat(identifiers).hasSameElementsAs(expectedIdentifiers);
  }

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
            () -> new IllegalStateException("Could not find RefasterRule '%s".formatted(ruleName)));
  }
}
