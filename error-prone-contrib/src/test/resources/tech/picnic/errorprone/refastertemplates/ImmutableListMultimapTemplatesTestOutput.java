package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableListMultimap.flatteningToImmutableListMultimap;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static java.util.function.Function.identity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
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
import tech.picnic.errorprone.refastertemplates.ImmutableListMultimapTemplates.EmptyImmutableListMultimap;
import tech.picnic.errorprone.refastertemplates.ImmutableListMultimapTemplates.EntryToImmutableListMultimap;
import tech.picnic.errorprone.refastertemplates.ImmutableListMultimapTemplates.ImmutableListMultimapBuilder;
import tech.picnic.errorprone.refastertemplates.ImmutableListMultimapTemplates.ImmutableListMultimapCopyOfImmutableListMultimap;
import tech.picnic.errorprone.refastertemplates.ImmutableListMultimapTemplates.ImmutableListMultimapCopyOfMultimapsTransformValues;
import tech.picnic.errorprone.refastertemplates.ImmutableListMultimapTemplates.ImmutableListMultimapCopyOfMultimapsTransformValuesTransformation;
import tech.picnic.errorprone.refastertemplates.ImmutableListMultimapTemplates.IndexIterableToImmutableListMultimap;
import tech.picnic.errorprone.refastertemplates.ImmutableListMultimapTemplates.IterableToImmutableListMultimap;
import tech.picnic.errorprone.refastertemplates.ImmutableListMultimapTemplates.PairToImmutableListMultimap;
import tech.picnic.errorprone.refastertemplates.ImmutableListMultimapTemplates.StreamOfMapEntriesToImmutableListMultimap;

@TemplateCollection(ImmutableListMultimapTemplates.class)
final class ImmutableListMultimapTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Streams.class, flatteningToImmutableListMultimap(null, null), identity());
  }

  @Template(ImmutableListMultimapBuilder.class)
  ImmutableSet<ImmutableMultimap.Builder<String, Integer>> testImmutableListMultimapBuilder() {
    return ImmutableSet.of(
        ImmutableListMultimap.builder(),
        ImmutableListMultimap.builder(),
        ImmutableListMultimap.builder());
  }

  @Template(EmptyImmutableListMultimap.class)
  ImmutableSet<ImmutableMultimap<String, Integer>> testEmptyImmutableListMultimap() {
    return ImmutableSet.of(ImmutableListMultimap.of(), ImmutableListMultimap.of());
  }

  @Template(PairToImmutableListMultimap.class)
  ImmutableSet<ImmutableMultimap<String, Integer>> testPairToImmutableListMultimap() {
    return ImmutableSet.of(ImmutableListMultimap.of("foo", 1), ImmutableListMultimap.of("bar", 2));
  }

  @Template(EntryToImmutableListMultimap.class)
  ImmutableList<ImmutableMultimap<String, Integer>> testEntryToImmutableListMultimap() {
    return ImmutableList.of(
        ImmutableListMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()),
        ImmutableListMultimap.of(Map.entry("foo", 1).getKey(), Map.entry("foo", 1).getValue()));
  }

  @Template(IterableToImmutableListMultimap.class)
  ImmutableList<ImmutableMultimap<String, Integer>> testIterableToImmutableListMultimap() {
    return ImmutableList.of(
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1).entries()),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1).entries()),
        ImmutableListMultimap.copyOf(Iterables.cycle(Map.entry("foo", 1))),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
        ImmutableListMultimap.copyOf(ImmutableListMultimap.of("foo", 1)),
        ImmutableListMultimap.copyOf(Iterables.cycle(Map.entry("foo", 1))));
  }

  @Template(StreamOfMapEntriesToImmutableListMultimap.class)
  ImmutableListMultimap<Integer, String> testStreamOfMapEntriesToImmutableListMultimap() {
    return Stream.of(1, 2, 3).collect(toImmutableListMultimap(n -> n, n -> n.toString()));
  }

  @Template(IndexIterableToImmutableListMultimap.class)
  ImmutableSet<ImmutableListMultimap<Integer, Integer>> testIndexIterableToImmutableListMultimap() {
    return ImmutableSet.of(
        Multimaps.index(ImmutableList.of(1), n -> n * 2),
        Multimaps.index(ImmutableList.of(2)::iterator, Integer::valueOf),
        Multimaps.index(ImmutableList.of(3).iterator(), n -> n.intValue()));
  }

  @Template(ImmutableListMultimapCopyOfMultimapsTransformValues.class)
  ImmutableListMultimap<String, Integer> testImmutableListMultimapCopyOfMultimapsTransformValues() {
    return ImmutableListMultimap.copyOf(
        Multimaps.transformValues(ImmutableListMultimap.of("foo", 1L), v -> Math.toIntExact(v)));
  }

  @Template(ImmutableListMultimapCopyOfMultimapsTransformValuesTransformation.class)
  ImmutableSet<ImmutableListMultimap<String, Integer>>
      testImmutableListMultimapCopyOfMultimapsTransformValuesTransformation() {
    return ImmutableSet.of(
        ImmutableListMultimap.copyOf(
            Multimaps.transformValues(ImmutableSetMultimap.of("foo", 1L), Math::toIntExact)),
        ImmutableListMultimap.copyOf(
            Multimaps.transformValues(
                (Multimap<String, Long>) ImmutableSetMultimap.of("bar", 2L),
                n -> Math.toIntExact(n))),
        ImmutableListMultimap.copyOf(
            Multimaps.transformValues(ImmutableListMultimap.of("baz", 3L), Math::toIntExact)),
        ImmutableListMultimap.copyOf(
            Multimaps.transformValues(ImmutableSetMultimap.of("qux", 4L), n -> Math.toIntExact(n))),
        ImmutableListMultimap.copyOf(
            Multimaps.transformValues(TreeMultimap.<String, Long>create(), Math::toIntExact)));
  }

  @Template(ImmutableListMultimapCopyOfImmutableListMultimap.class)
  ImmutableListMultimap<String, Integer> testImmutableListMultimapCopyOfImmutableListMultimap() {
    return ImmutableListMultimap.of("foo", 1);
  }
}
