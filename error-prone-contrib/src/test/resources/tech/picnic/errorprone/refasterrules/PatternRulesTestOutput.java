package tech.picnic.errorprone.refasterrules;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class PatternRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Predicates.class);
  }

  Predicate<?> testPatternAsPredicate() {
    return Pattern.compile("foo").asPredicate();
  }

  Predicate<?> testPatternCompileAsPredicate() {
    return Pattern.compile("foo").asPredicate();
  }
}
