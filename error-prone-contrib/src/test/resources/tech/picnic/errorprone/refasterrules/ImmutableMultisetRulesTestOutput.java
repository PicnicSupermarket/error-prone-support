package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableMultisetRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Arrays.class, Streams.class);
  }

  ImmutableMultiset.Builder<String> testImmutableMultisetBuilder() {
    return ImmutableMultiset.builder();
  }

  ImmutableSet<ImmutableMultiset<Integer>> testImmutableMultisetOf() {
    return ImmutableSet.of(ImmutableMultiset.of(), ImmutableMultiset.of());
  }

  @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
  ImmutableSet<ImmutableMultiset<Integer>> testImmutableMultisetCopyOf() {
    return ImmutableSet.of(
        ImmutableMultiset.copyOf(new Integer[] {1}),
        ImmutableMultiset.copyOf(new Integer[] {2}),
        ImmutableMultiset.copyOf(ImmutableMultiset.of(3).iterator()),
        ImmutableMultiset.copyOf(ImmutableList.of(4).iterator()),
        ImmutableMultiset.copyOf(ImmutableMultiset.of(5)),
        ImmutableMultiset.copyOf(ImmutableList.of(6)::iterator),
        ImmutableMultiset.copyOf(ImmutableList.of(7)));
  }

  ImmutableMultiset<Integer> testStreamCollectToImmutableMultiset() {
    return Stream.of(1).collect(toImmutableMultiset());
  }
}
