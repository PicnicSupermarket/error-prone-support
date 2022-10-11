package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.Set;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class MultimapRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Multimaps.class);
  }

  Set<String> testMultimapKeySet() {
    return ImmutableSetMultimap.of("foo", "bar").keySet();
  }

  int testMultimapSize() {
    return ImmutableSetMultimap.of().size();
  }

  ImmutableSet<Collection<Integer>> testMultimapGet() {
    return ImmutableSet.of(
        ImmutableSetMultimap.of(1, 2).get(1),
        ((Multimap<Integer, Integer>) ImmutableSetMultimap.of(1, 2)).get(1));
  }
}
