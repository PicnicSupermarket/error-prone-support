package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.function.Predicate;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ClassRulesTest implements RefasterRuleCollectionTestCase {
  boolean testClassIsInstance() throws IOException {
    return CharSequence.class.isInstance("foo");
  }

  ImmutableSet<Boolean> testInstanceof() throws IOException {
    Class<?> clazz = CharSequence.class;
    return ImmutableSet.of("foo" instanceof CharSequence, clazz.isInstance("bar"));
  }

  Predicate<String> testClassLiteralIsInstancePredicate() throws IOException {
    return CharSequence.class::isInstance;
  }

  Predicate<String> testClassReferenceIsInstancePredicate() throws IOException {
    Class<?> clazz = CharSequence.class;
    return clazz::isInstance;
  }
}
