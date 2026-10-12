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
    return new ImmutableMultiset.Builder<>();
  }

  ImmutableSet<ImmutableMultiset<Integer>> testImmutableMultisetOf() {
    return ImmutableSet.of(
        ImmutableMultiset.<Integer>builder().build(),
        Stream.<Integer>empty().collect(toImmutableMultiset()));
  }

  @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
  ImmutableSet<ImmutableMultiset<Integer>> testImmutableMultisetCopyOf() {
    return ImmutableSet.of(
        ImmutableMultiset.<Integer>builder().add(new Integer[] {1}).build(),
        Arrays.stream(new Integer[] {2}).collect(toImmutableMultiset()),
        ImmutableMultiset.<Integer>builder().addAll(ImmutableMultiset.of(3).iterator()).build(),
        Streams.stream(ImmutableList.of(4).iterator()).collect(toImmutableMultiset()),
        ImmutableMultiset.<Integer>builder().addAll(ImmutableMultiset.of(5)).build(),
        Streams.stream(ImmutableList.of(6)::iterator).collect(toImmutableMultiset()),
        ImmutableList.of(7).stream().collect(toImmutableMultiset()));
  }

  ImmutableMultiset<Integer> testStreamCollectToImmutableMultiset() {
    return ImmutableMultiset.copyOf(Stream.of(1).iterator());
  }
}
