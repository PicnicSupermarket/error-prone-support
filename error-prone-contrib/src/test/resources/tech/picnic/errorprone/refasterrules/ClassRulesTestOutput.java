package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.util.function.Function;
import java.util.function.Predicate;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ClassRulesTest implements RefasterRuleCollectionTestCase {
  boolean testClassIsInstance() {
    return CharSequence.class.isInstance("foo");
  }

  ImmutableSet<Boolean> testInstanceof() {
    Class<?> clazz = CharSequence.class;
    return ImmutableSet.of("foo" instanceof CharSequence, clazz.isInstance("bar"));
  }

  Predicate<String> testClassLiteralIsInstancePredicate() {
    return CharSequence.class::isInstance;
  }

  Predicate<String> testClassReferenceIsInstancePredicate() {
    Class<?> clazz = CharSequence.class;
    return clazz::isInstance;
  }

  Function<Number, Integer> testClassLiteralCast() {
    return Integer.class::cast;
  }

  Function<Number, Integer> testClassReferenceCast() {
    return Integer.class::cast;
  }
}
