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
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.ImmutableMultisetTemplates.EmptyImmutableMultiset;
import tech.picnic.errorprone.refastertemplates.ImmutableMultisetTemplates.ImmutableMultisetBuilder;
import tech.picnic.errorprone.refastertemplates.ImmutableMultisetTemplates.ImmutableMultisetCopyOfImmutableMultiset;
import tech.picnic.errorprone.refastertemplates.ImmutableMultisetTemplates.IterableToImmutableMultiset;
import tech.picnic.errorprone.refastertemplates.ImmutableMultisetTemplates.StreamToImmutableMultiset;

@TemplateCollection(ImmutableMultisetTemplates.class)
final class ImmutableMultisetTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Arrays.class, Streams.class, collectingAndThen(null, null), toList());
  }

  @Template(ImmutableMultisetBuilder.class)
  ImmutableMultiset.Builder<String> testImmutableMultisetBuilder() {
    return new ImmutableMultiset.Builder<>();
  }

  @Template(EmptyImmutableMultiset.class)
  ImmutableMultiset<ImmutableMultiset<Integer>> testEmptyImmutableMultiset() {
    return ImmutableMultiset.of(
        ImmutableMultiset.<Integer>builder().build(),
        Stream.<Integer>empty().collect(toImmutableMultiset()));
  }

  @Template(IterableToImmutableMultiset.class)
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

  @Template(StreamToImmutableMultiset.class)
  ImmutableSet<ImmutableMultiset<Integer>> testStreamToImmutableMultiset() {
    return ImmutableSet.of(
        ImmutableMultiset.copyOf(Stream.of(1).iterator()),
        Stream.of(2).collect(collectingAndThen(toList(), ImmutableMultiset::copyOf)));
  }

  @Template(ImmutableMultisetCopyOfImmutableMultiset.class)
  ImmutableMultiset<Integer> testImmutableMultisetCopyOfImmutableMultiset() {
    return ImmutableMultiset.copyOf(ImmutableMultiset.of(1, 2));
  }
}
