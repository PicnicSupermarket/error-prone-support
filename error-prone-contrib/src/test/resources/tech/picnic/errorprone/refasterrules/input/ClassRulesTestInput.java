package tech.picnic.errorprone.refasterrules.input;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.function.Predicate;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ClassRulesTest implements RefasterRuleCollectionTestCase {
  boolean testClassIsInstance() throws IOException {
    return CharSequence.class.isAssignableFrom("foo".getClass());
  }

  ImmutableSet<Boolean> testInstanceof() throws IOException {
    Class<?> clazz = CharSequence.class;
    return ImmutableSet.of(CharSequence.class.isInstance("foo"), clazz.isInstance("bar"));
  }

  Predicate<String> testClassLiteralIsInstancePredicate() throws IOException {
    return s -> s instanceof CharSequence;
  }

  Predicate<String> testClassReferenceIsInstancePredicate() throws IOException {
    Class<?> clazz = CharSequence.class;
    return s -> clazz.isInstance(s);
  }
}
