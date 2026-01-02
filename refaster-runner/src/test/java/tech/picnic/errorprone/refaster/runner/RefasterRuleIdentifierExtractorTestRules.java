package tech.picnic.errorprone.refaster.runner;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Test rules for {@link RefasterRuleIdentifierExtractor}.
 *
 * <p>These rules are designed to test various identifier extraction scenarios:
 *
 * <ul>
 *   <li>Simple method calls
 *   <li>Binary operators
 *   <li>Member selects
 *   <li>Member references
 *   <li>Block templates (statements)
 *   <li>UAnyOf (single and nested)
 * </ul>
 */
final class RefasterRuleIdentifierExtractorTestRules {
  private RefasterRuleIdentifierExtractorTestRules() {}

  /** A simple rule with a method call. */
  static final class SimpleMethodCallRule {
    @BeforeTemplate
    boolean before(String string) {
      return string.isEmpty();
    }

    @AfterTemplate
    boolean after(String string) {
      return string.isEmpty();
    }
  }

  /** A rule with binary operators. */
  static final class BinaryOperatorRule {
    @BeforeTemplate
    boolean before(int a, int b) {
      return a + b > 0;
    }

    @AfterTemplate
    boolean after(int a, int b) {
      return a > -b;
    }
  }

  /** A rule with member select. */
  @SuppressWarnings("SystemOut" /* Test rule for identifier extraction. */)
  static final class MemberSelectRule {
    @BeforeTemplate
    void before() {
      System.out.println("test");
    }

    @AfterTemplate
    void after() {
      System.err.println("test");
    }
  }

  /** A rule with member reference. */
  static final class MemberReferenceRule {
    @BeforeTemplate
    Supplier<Object> before() {
      return Object::new;
    }

    @AfterTemplate
    Supplier<Object> after() {
      return () -> new Object();
    }
  }

  /** A rule with BlockTemplate (statements, not expressions). */
  static final class BlockTemplateRule {
    @BeforeTemplate
    void before(Set<String> set, String element) {
      if (!set.contains(element)) {
        set.add(element);
      }
    }

    @AfterTemplate
    void after(Set<String> set, String element) {
      set.add(element);
    }
  }

  /** A rule with single UAnyOf (should create multiple identifier sets). */
  static final class SingleAnyOfRule {
    @BeforeTemplate
    boolean before(String str) {
      return Refaster.anyOf(str.isEmpty(), str.length() == 0);
    }

    @AfterTemplate
    boolean after(String str) {
      return str.isEmpty();
    }
  }

  /** A rule with nested UAnyOf (double nested). */
  static final class NestedAnyOfRule {
    @BeforeTemplate
    boolean before(String str, int len) {
      return Refaster.anyOf(
          str.length() == len,
          Refaster.anyOf(str.length() == len + 1, str.length() == len - 1));
    }

    @AfterTemplate
    boolean after(String str, int len) {
      return str.length() == len;
    }
  }

  /** A rule with multiple identifiers for tree building tests. */
  static final class MultipleIdentifiersRule {
    @BeforeTemplate
    boolean before(String a, String b, String c) {
      return a.equals(b) && b.equals(c);
    }

    @AfterTemplate
    boolean after(String a, String b, String c) {
      return a.equals(b) && a.equals(c);
    }
  }
}
