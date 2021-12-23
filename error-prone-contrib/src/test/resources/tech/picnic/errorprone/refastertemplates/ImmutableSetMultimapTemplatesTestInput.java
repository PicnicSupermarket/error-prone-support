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
    return new ImmutableSetMultimap.Builder<>();
  }

  @Template(EmptyImmutableSetMultimap.class)
  ImmutableSetMultimap<String, Integer> testEmptyImmutableSetMultimap() {
    return ImmutableSetMultimap.<String, Integer>builder().build();
  }

  @Template(PairToImmutableSetMultimap.class)
  ImmutableSetMultimap<String, Integer> testPairToImmutableSetMultimap() {
    return ImmutableSetMultimap.<String, Integer>builder().put("foo", 1).build();
  }

  @Template(EntryToImmutableSetMultimap.class)
  ImmutableSet<ImmutableSetMultimap<String, Integer>> testEntryToImmutableSetMultimap() {
    return ImmutableSet.of(
        ImmutableSetMultimap.<String, Integer>builder().put(Map.entry("foo", 1)).build(),
        Stream.of(Map.entry("foo", 1))
            .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  @Template(IterableToImmutableSetMultimap.class)
  ImmutableSet<ImmutableSetMultimap<String, Integer>> testIterableToImmutableSetMultimap() {
    return ImmutableSet.of(
        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1).entries()),
        ImmutableSetMultimap.<String, Integer>builder()
            .putAll(ImmutableSetMultimap.of("foo", 1))
            .build(),
        ImmutableSetMultimap.<String, Integer>builder()
            .putAll(ImmutableSetMultimap.of("foo", 1).entries())
            .build(),
        ImmutableSetMultimap.of("foo", 1).entries().stream()
            .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)),
        Streams.stream(Iterables.cycle(Map.entry("foo", 1)))
            .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  @Template(StreamCollectToImmutableSetMultimap.class)
  ImmutableSetMultimap<Integer, String> testStreamCollectToImmutableSetMultimap() {
    return Stream.of(1, 2, 3)
        .map(n -> Map.entry(n, n.toString()))
        .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Template(ImmutableSetMultimapCopyOfMultimapsTransformValues.class)
  ImmutableSetMultimap<String, Integer> testImmutableSetMultimapCopyOfMultimapsTransformValues() {
    return ImmutableSetMultimap.of("foo", 1L).entries().stream()
        .collect(toImmutableSetMultimap(Map.Entry::getKey, e -> Math.toIntExact(e.getValue())));
  }

  @Template(ImmutableSetMultimapCopyOfMultimapsTransformValuesTransformation.class)
  ImmutableSet<ImmutableSetMultimap<String, Integer>>
      testImmutableSetMultimapCopyOfMultimapsTransformValuesTransformation() {
    return ImmutableSet.of(
        ImmutableSetMultimap.of("foo", 1L).asMap().entrySet().stream()
            .collect(
                flatteningToImmutableSetMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(Math::toIntExact))),
        Multimaps.asMap((Multimap<String, Long>) ImmutableSetMultimap.of("bar", 2L))
            .entrySet()
            .stream()
            .collect(
                flatteningToImmutableSetMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(n -> Math.toIntExact(n)))),
        Multimaps.asMap(ImmutableListMultimap.of("baz", 3L)).entrySet().stream()
            .collect(
                flatteningToImmutableSetMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(Math::toIntExact))),
        Multimaps.asMap(ImmutableSetMultimap.of("qux", 4L)).entrySet().stream()
            .collect(
                flatteningToImmutableSetMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(n -> Math.toIntExact(n)))),
        Multimaps.asMap(TreeMultimap.<String, Long>create()).entrySet().stream()
            .collect(
                flatteningToImmutableSetMultimap(
                    Map.Entry::getKey, e -> e.getValue().stream().map(Math::toIntExact))));
  }

  @Template(ImmutableSetMultimapCopyOfImmutableSetMultimap.class)
  ImmutableSetMultimap<String, Integer> testImmutableSetMultimapCopyOfImmutableSetMultimap() {
    return ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of("foo", 1));
  }
}
