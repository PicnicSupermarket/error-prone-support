package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Streams;
import com.google.common.collect.TreeMultimap;
import java.util.Map;
import java.util.stream.Stream;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.ImmutableSetMultimapTemplates.EmptyImmutableSetMultimap;
import tech.picnic.errorprone.refastertemplates.ImmutableSetMultimapTemplates.EntryToImmutableSetMultimap;
import tech.picnic.errorprone.refastertemplates.ImmutableSetMultimapTemplates.ImmutableSetMultimapBuilder;
import tech.picnic.errorprone.refastertemplates.ImmutableSetMultimapTemplates.ImmutableSetMultimapCopyOfImmutableSetMultimap;
import tech.picnic.errorprone.refastertemplates.ImmutableSetMultimapTemplates.ImmutableSetMultimapCopyOfMultimapsTransformValues;
import tech.picnic.errorprone.refastertemplates.ImmutableSetMultimapTemplates.ImmutableSetMultimapCopyOfMultimapsTransformValuesTransformation;
import tech.picnic.errorprone.refastertemplates.ImmutableSetMultimapTemplates.IterableToImmutableSetMultimap;
import tech.picnic.errorprone.refastertemplates.ImmutableSetMultimapTemplates.PairToImmutableSetMultimap;
import tech.picnic.errorprone.refastertemplates.ImmutableSetMultimapTemplates.StreamCollectToImmutableSetMultimap;

@TemplateCollection(ImmutableSetMultimapTemplates.class)
final class ImmutableSetMultimapTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class, flatteningToImmutableSetMultimap(null, null));
  }

  @Template(ImmutableSetMultimapBuilder.class)
  ImmutableSetMultimap.Builder<String, Integer> testImmutableSetMultimapBuilder() {
    return ImmutableSetMultimap.builder();
  }

  @Template(EmptyImmutableSetMultimap.class)
  ImmutableSetMultimap<String, Integer> testEmptyImmutableSetMultimap() {
    return ImmutableSetMultimap.of();
  }

  @Template(PairToImmutableSetMultimap.class)
  ImmutableSetMultimap<String, Integer> testPairToImmutableSetMultimap() {
    return ImmutableSetMultimap.of("foo", 1);
  }

  @Template(EntryToImmutableSetMultimap.class)
  ImmutableSet<ImmutableSetMultimap<String, Integer>> testEntryToImmutableSetMultimap() {
    return ImmutableSet.of(
        ImmutableSetMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
        ImmutableSetMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()));
  }

  @Template(IterableToImmutableSetMultimap.class)
  ImmutableSet<ImmutableSetMultimap<String, Integer>> testIterableToImmutableSetMultimap() {
    return ImmutableSet.of(
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1)),
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1)),
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1).entries()),
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1).entries()),
        ImmutableSetMultimap.copyOf(Iterables.cycle(Map.entry("foo", 1))));
  }

  @Template(StreamCollectToImmutableSetMultimap.class)
  ImmutableSetMultimap<Integer, String> testStreamCollectToImmutableSetMultimap() {
    return Stream.of(1, 2, 3).collect(toImmutableSetMultimap(n -> n, n -> n.toString()));
  }

  @Template(ImmutableSetMultimapCopyOfMultimapsTransformValues.class)
  ImmutableSetMultimap<String, Integer> testImmutableSetMultimapCopyOfMultimapsTransformValues() {
    return ImmutableSetMultimap.copyOf(
        Multimaps.transformValues(ImmutableSetMultimap.of("foo", 1L), e -> Math.toIntExact(e)));
  }

  @Template(ImmutableSetMultimapCopyOfMultimapsTransformValuesTransformation.class)
  ImmutableSet<ImmutableSetMultimap<String, Integer>>
      testImmutableSetMultimapCopyOfMultimapsTransformValuesTransformation() {
    return ImmutableSet.of(
        ImmutableSetMultimap.copyOf(
            Multimaps.transformValues(ImmutableSetMultimap.of("foo", 1L), Math::toIntExact)),
        ImmutableSetMultimap.copyOf(
            Multimaps.transformValues(
                (Multimap<String, Long>) ImmutableSetMultimap.of("bar", 2L),
                n -> Math.toIntExact(n))),
        ImmutableSetMultimap.copyOf(
            Multimaps.transformValues(ImmutableListMultimap.of("baz", 3L), Math::toIntExact)),
        ImmutableSetMultimap.copyOf(
            Multimaps.transformValues(ImmutableSetMultimap.of("qux", 4L), n -> Math.toIntExact(n))),
        ImmutableSetMultimap.copyOf(
            Multimaps.transformValues(TreeMultimap.<String, Long>create(), Math::toIntExact)));
  }

  @Template(ImmutableSetMultimapCopyOfImmutableSetMultimap.class)
  ImmutableSetMultimap<String, Integer> testImmutableSetMultimapCopyOfImmutableSetMultimap() {
    return ImmutableSetMultimap.of("foo", 1);
  }
}
