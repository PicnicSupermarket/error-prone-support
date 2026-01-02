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

  /** A simple rule with two method calls. */
  static final class SimpleMethodCallRule {
    @BeforeTemplate
    boolean before(String string) {
      return string.isEmpty() && string.hashCode() == 0;
    }

    @AfterTemplate
    boolean after(String string) {
      return string.isEmpty() && string.hashCode() == 0;
    }
  }

  /** A rule with binary operators. */
  static final class BinaryOperatorRule {
    @BeforeTemplate
    boolean before(int a, int b) {
      // Use a very specific pattern that won't match real code
      return a + b > 0 && a * b < 1000;
    }

    @AfterTemplate
    boolean after(int a, int b) {
      return a > -b && a * b < 1000;
    }
  }

  /** A rule with member select. */
  static final class MemberSelectRule {
    @BeforeTemplate
    void before(StringBuilder sb) {
      sb.append("test").append("xyz");
    }

    @AfterTemplate
    void after(StringBuilder sb) {
      sb.append("test").append("xyz");
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
      if (!set.contains(element) && set.size() < 100) {
        set.add(element);
        set.remove("dummy");
      }
    }

    @AfterTemplate
    void after(Set<String> set, String element) {
      set.add(element);
      set.remove("dummy");
    }
  }

  /** A rule with a single {@link com.google.errorprone.refaster.UAnyOf}. */
  static final class SingleAnyOfRule {
    @BeforeTemplate
    boolean before(String str) {
      return Refaster.anyOf(
          str.equals("") && str.hashCode() == 0, str.isEmpty() && str.hashCode() == 0);
    }

    @AfterTemplate
    boolean after(String str) {
      return str.isEmpty() && str.hashCode() == 0;
    }
  }

  /** A rule with nested {@link com.google.errorprone.refaster.UAnyOf}. */
  static final class NestedAnyOfRule {
    @BeforeTemplate
    boolean before(String str, int len) {
      return Refaster.anyOf(str.length() == len, str.length() == len + 1, str.length() == len - 1);
    }

    @AfterTemplate
    boolean after(String str, int len) {
      return str.length() == len;
    }
  }

  /** A rule with multiple overlapping identifiers for tree building tests. */
  static final class MultipleIdentifiersRule {
    @BeforeTemplate
    boolean before(String a, String b, String c) {
      return a.equals(b) && b.equals(c) && a.hashCode() == c.hashCode();
    }

    @AfterTemplate
    boolean after(String a, String b, String c) {
      return a.equals(b) && a.equals(c) && a.hashCode() == c.hashCode();
    }
  }

  /** A rule with multiple @BeforeTemplate methods. */
  static final class MultipleBeforeTemplatesRule {
    @BeforeTemplate
    boolean before1(String str) {
      return str.isEmpty() && str.length() == 0;
    }

    @BeforeTemplate
    boolean before2(String str) {
      return str.length() > 0 && str.length() != 0;
    }

    @AfterTemplate
    boolean after(String str) {
      return str.isEmpty();
    }
  }
}
