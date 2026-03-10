package tech.picnic.errorprone.refasterrules;

import java.util.function.Function;
import java.util.function.Predicate;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ClassRulesTest implements RefasterRuleCollectionTestCase {
  boolean testClassIsInstance() {
    return CharSequence.class.isInstance("foo");
  }

  boolean testInstanceof() {
    return "foo" instanceof CharSequence;
  }

  Predicate<String> testClassLiteralIsInstancePredicate() {
    return CharSequence.class::isInstance;
  }

  Predicate<String> testClassReferenceIsInstancePredicate() {
    Class<?> clazz = CharSequence.class;
    return clazz::isInstance;
  }

  Function<Number, Integer> testClassReferenceCast() {
    return Integer.class::cast;
  }
}
