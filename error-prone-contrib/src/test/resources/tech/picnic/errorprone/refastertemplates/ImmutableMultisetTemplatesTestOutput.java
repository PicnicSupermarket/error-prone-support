package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.stream.Stream;

final class ImmutableMultisetTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Arrays.class, Streams.class, collectingAndThen(null, null), toList());
  }

  ImmutableMultiset.Builder<String> testImmutableMultisetBuilder() {
    return ImmutableMultiset.builder();
  }

  ImmutableMultiset<ImmutableMultiset<Integer>> testEmptyImmutableMultiset() {
    return ImmutableMultiset.of(ImmutableMultiset.of(), ImmutableMultiset.of());
  }

  ImmutableMultiset<ImmutableMultiset<Integer>> testIterableToImmutableMultiset() {
    return ImmutableMultiset.of(
        ImmutableMultiset.copyOf(ImmutableList.of(1)),
        ImmutableMultiset.copyOf(ImmutableList.of(2)::iterator),
        ImmutableMultiset.copyOf(ImmutableList.of(3).iterator()),
        ImmutableMultiset.copyOf(ImmutableMultiset.of(4)),
        ImmutableMultiset.copyOf(ImmutableMultiset.of(5)::iterator),
        ImmutableMultiset.copyOf(ImmutableMultiset.of(6).iterator()),
        ImmutableMultiset.copyOf(new Integer[] {7}),
        ImmutableMultiset.copyOf(new Integer[] {8}));
  }

  ImmutableSet<ImmutableMultiset<Integer>> testStreamToImmutableMultiset() {
    return ImmutableSet.of(
        Stream.of(1).collect(toImmutableMultiset()), Stream.of(2).collect(toImmutableMultiset()));
  }

  ImmutableMultiset<Integer> testImmutableMultisetCopyOfImmutableMultiset() {
    return ImmutableMultiset.of(1, 2);
  }
}
