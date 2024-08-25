package tech.picnic.errorprone.refasterrules.input;

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

  ImmutableMultiset<ImmutableMultiset<Integer>> testEmptyImmutableMultiset() {
    return ImmutableMultiset.of(
        ImmutableMultiset.<Integer>builder().build(),
        Stream.<Integer>empty().collect(toImmutableMultiset()));
  }

  ImmutableMultiset<ImmutableMultiset<Integer>> testIterableToImmutableMultiset() {
    return ImmutableMultiset.of(
        ImmutableList.of(1).stream().collect(toImmutableMultiset()),
        Streams.stream(ImmutableList.of(2)::iterator).collect(toImmutableMultiset()),
        Streams.stream(ImmutableList.of(3).iterator()).collect(toImmutableMultiset()),
        ImmutableMultiset.<Integer>builder().addAll(ImmutableMultiset.of(4)).build(),
        ImmutableMultiset.<Integer>builder().addAll(ImmutableMultiset.of(5)::iterator).build(),
        ImmutableMultiset.<Integer>builder().addAll(ImmutableMultiset.of(6).iterator()).build(),
        ImmutableMultiset.<Integer>builder().add(new Integer[] {7}).build(),
        Arrays.stream(new Integer[] {8}).collect(toImmutableMultiset()));
  }

  ImmutableMultiset<Integer> testStreamToImmutableMultiset() {
    return ImmutableMultiset.copyOf(Stream.of(1).iterator());
  }
}
