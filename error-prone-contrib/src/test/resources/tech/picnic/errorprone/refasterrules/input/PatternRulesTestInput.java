package tech.picnic.errorprone.refasterrules.input;

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
    return Predicates.contains(Pattern.compile("foo"));
  }

  Predicate<?> testPatternCompileAsPredicate() {
    return Predicates.containsPattern("foo");
  }
}
