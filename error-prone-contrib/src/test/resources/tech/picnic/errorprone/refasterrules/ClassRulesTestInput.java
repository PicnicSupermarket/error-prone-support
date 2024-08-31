package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.util.function.Function;
import java.util.function.Predicate;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ClassRulesTest implements RefasterRuleCollectionTestCase {
  boolean testClassIsInstance() {
    return CharSequence.class.isAssignableFrom("foo".getClass());
  }

  ImmutableSet<Boolean> testInstanceof() {
    Class<?> clazz = CharSequence.class;
    return ImmutableSet.of(CharSequence.class.isInstance("foo"), clazz.isInstance("bar"));
  }

  Predicate<String> testClassLiteralIsInstancePredicate() {
    return s -> s instanceof CharSequence;
  }

  Predicate<String> testClassReferenceIsInstancePredicate() {
    Class<?> clazz = CharSequence.class;
    return s -> clazz.isInstance(s);
  }

  Function<Number, Integer> testClassLiteralCast() {
    return i -> (Integer) i;
  }

  Function<Number, Integer> testClassReferenceCast() {
    return i -> Integer.class.cast(i);
  }
}
