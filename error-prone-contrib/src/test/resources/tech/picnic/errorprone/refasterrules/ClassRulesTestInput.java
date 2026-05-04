package tech.picnic.errorprone.refasterrules;

import java.util.function.Function;
import java.util.function.Predicate;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ClassRulesTest implements RefasterRuleCollectionTestCase {
  boolean testClassIsInstanceWithClassAndObject() {
    return CharSequence.class.isAssignableFrom("foo".getClass());
  }

  boolean testRefasterIsInstance() {
    return CharSequence.class.isInstance("foo");
  }

  Predicate<String> testClassIsInstance() {
    return s -> s instanceof CharSequence;
  }

  Predicate<String> testClassIsInstanceWithClass() {
    Class<?> clazz = CharSequence.class;
    return s -> clazz.isInstance(s);
  }

  Function<Number, Integer> testClassCast() {
    return i -> Integer.class.cast(i);
  }
}
