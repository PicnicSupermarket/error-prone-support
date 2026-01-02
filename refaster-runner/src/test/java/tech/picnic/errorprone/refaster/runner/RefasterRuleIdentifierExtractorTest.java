package tech.picnic.errorprone.refaster.runner;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.refaster.RefasterRule;
import org.junit.jupiter.api.Test;

final class RefasterRuleIdentifierExtractorTest {
  @Test
  void debugAvailableRules() {
    // Debug test to see what rules are available
    ImmutableListMultimap<String, CodeTransformer> transformers =
        CodeTransformers.getAllCodeTransformers();

    // This test will fail but show us what rules are available
    assertThat(transformers.keySet())
        .contains("RefasterRuleIdentifierExtractorTestRules$SimpleMethodCallRule");
  }

  @Test
  void extractIdentifiersFromSimpleMethodCallRule() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$SimpleMethodCallRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    // The rule has: string.isEmpty()
    // Should extract: [isEmpty, string]
    assertThat(identifiers).hasSize(1);
    ImmutableSet<String> identifierSet = identifiers.iterator().next();
    assertThat(identifierSet).containsExactlyInAnyOrder("isEmpty", "string");
  }

  @Test
  void extractIdentifiersFromBinaryOperatorRule() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$BinaryOperatorRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    // The rule has: a + b > 0
    // Should extract: [+, >, 0, a, b]
    assertThat(identifiers).hasSize(1);
    ImmutableSet<String> identifierSet = identifiers.iterator().next();
    assertThat(identifierSet).containsExactlyInAnyOrder("+", ">", "0", "a", "b");
  }

  @Test
  void extractIdentifiersFromMemberSelectRule() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$MemberSelectRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    // The rule has: System.out.println("test")
    // Should extract: [System, out, println]
    assertThat(identifiers).hasSize(1);
    ImmutableSet<String> identifierSet = identifiers.iterator().next();
    assertThat(identifierSet).containsExactlyInAnyOrder("System", "out", "println");
  }

  @Test
  void extractIdentifiersFromMemberReferenceRule() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$MemberReferenceRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    // The rule has: Object::new
    // Should extract: [<init>, Object]
    assertThat(identifiers).hasSize(1);
    ImmutableSet<String> identifierSet = identifiers.iterator().next();
    assertThat(identifierSet).containsExactlyInAnyOrder("<init>", "Object");
  }

  @Test
  void extractIdentifiersFromFooRules() {
    // Test with existing FooRules to ensure compatibility
    RefasterRule<?, ?> rule = getRefasterRule("FooRules$StringOfSizeZeroRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    // The rule has: string.toCharArray().length == 0
    // Should extract identifiers from this expression
    assertThat(identifiers).hasSize(1);
    ImmutableSet<String> identifierSet = identifiers.iterator().next();
    assertThat(identifierSet).contains("string", "toCharArray", "length", "==", "0");
  }

  @Test
  void extractIdentifiersFromBlockTemplate() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$BlockTemplateRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    // The rule has statements: if (!set.contains(element)) { set.add(element); }
    // Should extract identifiers from statements, not expressions
    assertThat(identifiers).hasSize(1);
    ImmutableSet<String> identifierSet = identifiers.iterator().next();
    assertThat(identifierSet).contains("set", "contains", "element", "add", "!");
  }

  @Test
  void extractIdentifiersFromSingleAnyOf() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$SingleAnyOfRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    // The rule has: Refaster.anyOf(str.isEmpty(), str.length() == 0)
    // Should create TWO identifier sets (one for each alternative)
    assertThat(identifiers).hasSize(2);

    // First alternative: str.isEmpty()
    ImmutableSet<String> firstSet = findSetContaining(identifiers, "isEmpty");
    assertThat(firstSet).containsExactlyInAnyOrder("isEmpty", "str");

    // Second alternative: str.length() == 0
    ImmutableSet<String> secondSet = findSetContaining(identifiers, "length");
    assertThat(secondSet).containsExactlyInAnyOrder("==", "0", "length", "str");
  }

  @Test
  void extractIdentifiersFromNestedAnyOf() {
    RefasterRule<?, ?> rule =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$NestedAnyOfRule");
    ImmutableSet<ImmutableSet<String>> identifiers =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule);

    // The rule has:
    // Refaster.anyOf(
    //   str.length() == len,
    //   Refaster.anyOf(str.length() == len + 1, str.length() == len - 1)
    // )
    // Should create THREE identifier sets:
    // 1. str.length() == len
    // 2. str.length() == len + 1
    // 3. str.length() == len - 1
    assertThat(identifiers).hasSize(3);

    // First alternative: str.length() == len
    ImmutableSet<String> firstSet = findSetContaining(identifiers, "len");
    assertThat(firstSet).contains("==", "length", "str", "len");

    // Second alternative: str.length() == len + 1
    ImmutableSet<String> secondSet = findSetContaining(identifiers, "+");
    assertThat(secondSet).contains("==", "+", "1", "length", "str", "len");

    // Third alternative: str.length() == len - 1
    ImmutableSet<String> thirdSet = findSetContaining(identifiers, "-");
    assertThat(thirdSet).contains("==", "-", "1", "length", "str", "len");
  }

  @Test
  void extractIdentifiersFromMultipleRules() {
    // Test that multiple rules create separate identifier sets
    RefasterRule<?, ?> rule1 =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$SimpleMethodCallRule");
    RefasterRule<?, ?> rule2 =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$BinaryOperatorRule");
    RefasterRule<?, ?> rule3 =
        getRefasterRule("RefasterRuleIdentifierExtractorTestRules$MultipleIdentifiersRule");

    ImmutableSet<ImmutableSet<String>> identifiers1 =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule1);
    ImmutableSet<ImmutableSet<String>> identifiers2 =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule2);
    ImmutableSet<ImmutableSet<String>> identifiers3 =
        RefasterRuleIdentifierExtractor.extractIdentifiers(rule3);

    // Each rule should have its own identifier set(s)
    assertThat(identifiers1).hasSize(1);
    assertThat(identifiers2).hasSize(1);
    assertThat(identifiers3).hasSize(1);

    // Verify the identifiers are different for each rule
    ImmutableSet<String> set1 = identifiers1.iterator().next();
    ImmutableSet<String> set2 = identifiers2.iterator().next();
    ImmutableSet<String> set3 = identifiers3.iterator().next();

    // Rule 1: string.isEmpty()
    assertThat(set1).containsExactlyInAnyOrder("isEmpty", "string");

    // Rule 2: a + b > 0
    assertThat(set2).containsExactlyInAnyOrder("+", ">", "0", "a", "b");

    // Rule 3: a.equals(b) && b.equals(c)
    assertThat(set3).contains("&&", "equals", "a", "b", "c");
  }

  private static ImmutableSet<String> findSetContaining(
      ImmutableSet<ImmutableSet<String>> sets, String identifier) {
    return sets.stream()
        .filter(set -> set.contains(identifier))
        .findFirst()
        .orElseThrow(
            () ->
                new AssertionError(
                    "No set found containing identifier: " + identifier + " in " + sets));
  }

  private static RefasterRule<?, ?> getRefasterRule(String ruleName) {
    // Use the test class's classloader to ensure test rules are found
    ImmutableListMultimap<String, CodeTransformer> transformers =
        CodeTransformers.getAllCodeTransformers();
    
    // Also check if the rule exists - if not, list available rules for debugging
    if (!transformers.containsKey(ruleName)) {
      throw new IllegalStateException(
          "Could not find RefasterRule: "
              + ruleName
              + ". Available rules: "
              + transformers.keySet());
    }
    
    return transformers.get(ruleName).stream()
        .filter(RefasterRule.class::isInstance)
        .findFirst()
        .map(RefasterRule.class::cast)
        .orElseThrow(() -> new IllegalStateException("Could not find RefasterRule: " + ruleName));
  }
}
