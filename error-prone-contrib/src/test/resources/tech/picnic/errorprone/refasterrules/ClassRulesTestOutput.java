package tech.picnic.errorprone.refasterrules;

import java.util.function.Function;
import java.util.function.Predicate;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ClassRulesTest implements RefasterRuleCollectionTestCase {
  boolean testClassIsInstanceWithClassAndObject() {
    return CharSequence.class.isInstance("foo");
  }

  boolean testRefasterIsInstance() {
    return "foo" instanceof CharSequence;
  }

  Predicate<String> testClassIsInstance() {
    return CharSequence.class::isInstance;
  }

  Predicate<String> testClassIsInstanceWithClass() {
    Class<?> clazz = CharSequence.class;
    return clazz::isInstance;
  }

  Function<Number, Integer> testClassCast() {
    return Integer.class::cast;
  }
}
