package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssortedTemplates.CheckIndex;
import tech.picnic.errorprone.refastertemplates.AssortedTemplates.CreateEnumMap;
import tech.picnic.errorprone.refastertemplates.AssortedTemplates.DisjointCollections;
import tech.picnic.errorprone.refastertemplates.AssortedTemplates.DisjointSets;
import tech.picnic.errorprone.refastertemplates.AssortedTemplates.IterableIsEmpty;
import tech.picnic.errorprone.refastertemplates.AssortedTemplates.IteratorGetNextOrDefault;
import tech.picnic.errorprone.refastertemplates.AssortedTemplates.LogicalImplication;
import tech.picnic.errorprone.refastertemplates.AssortedTemplates.MapGetOrNull;
import tech.picnic.errorprone.refastertemplates.AssortedTemplates.MapKeyStream;
import tech.picnic.errorprone.refastertemplates.AssortedTemplates.MapValueStream;
import tech.picnic.errorprone.refastertemplates.AssortedTemplates.SplitToStream;
import tech.picnic.errorprone.refastertemplates.AssortedTemplates.StreamToImmutableEnumSet;
import tech.picnic.errorprone.refastertemplates.AssortedTemplates.UnboundedSingleElementStream;

@TemplateCollection(AssortedTemplates.class)
final class AssortedTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        HashMap.class,
        HashSet.class,
        Iterables.class,
        Preconditions.class,
        Sets.class,
        Splitter.class,
        Streams.class,
        toImmutableSet());
  }

  @Template(CheckIndex.class)
  int testCheckIndex() {
    return Preconditions.checkElementIndex(0, 1);
  }

  @Template(CreateEnumMap.class)
  Map<RoundingMode, String> testCreateEnumMap() {
    return new HashMap<>();
  }

  @Template(MapGetOrNull.class)
  String testMapGetOrNull() {
    return ImmutableMap.of(1, "foo").getOrDefault("bar", null);
  }

  @Template(StreamToImmutableEnumSet.class)
  ImmutableSet<BoundType> testStreamToImmutableEnumSet() {
    return Stream.of(BoundType.OPEN).collect(toImmutableSet());
  }

  @Template(IteratorGetNextOrDefault.class)
  ImmutableSet<String> testIteratorGetNextOrDefault() {
    return ImmutableSet.of(
        ImmutableList.of("a").iterator().hasNext()
            ? ImmutableList.of("a").iterator().next()
            : "foo",
        Streams.stream(ImmutableList.of("b").iterator()).findFirst().orElse("bar"),
        Streams.stream(ImmutableList.of("c").iterator()).findAny().orElse("baz"));
  }

  // XXX: Only the first statement is rewritten. Make smarter.
  @Template(LogicalImplication.class)
  ImmutableSet<Boolean> testLogicalImplication() {
    return ImmutableSet.of(
        toString().isEmpty() || (!toString().isEmpty() && true),
        !toString().isEmpty() || (toString().isEmpty() && true),
        3 < 4 || (3 >= 4 && true),
        3 >= 4 || (3 < 4 && true));
  }

  @Template(UnboundedSingleElementStream.class)
  Stream<String> testUnboundedSingleElementStream() {
    return Streams.stream(Iterables.cycle("foo"));
  }

  @Template(DisjointSets.class)
  ImmutableSet<Boolean> testDisjointSets() {
    return ImmutableSet.of(
        Sets.intersection(ImmutableSet.of(1), ImmutableSet.of(2)).isEmpty(),
        ImmutableSet.of(3).stream().noneMatch(ImmutableSet.of(4)::contains));
  }

  @Template(DisjointCollections.class)
  ImmutableSet<Boolean> testDisjointCollections() {
    return ImmutableSet.of(
        Collections.disjoint(ImmutableSet.copyOf(ImmutableList.of(1)), ImmutableList.of(2)),
        Collections.disjoint(new HashSet<>(ImmutableList.of(3)), ImmutableList.of(4)),
        Collections.disjoint(ImmutableList.of(5), ImmutableSet.copyOf(ImmutableList.of(6))),
        Collections.disjoint(ImmutableList.of(7), new HashSet<>(ImmutableList.of(8))));
  }

  @Template(IterableIsEmpty.class)
  boolean testIterableIsEmpty() {
    return !ImmutableList.of().iterator().hasNext();
  }

  @Template(MapKeyStream.class)
  Stream<String> testMapKeyStream() {
    return ImmutableMap.of("foo", 1).entrySet().stream().map(Map.Entry::getKey);
  }

  @Template(MapValueStream.class)
  Stream<Integer> testMapValueStream() {
    return ImmutableMap.of("foo", 1).entrySet().stream().map(Map.Entry::getValue);
  }

  @Template(SplitToStream.class)
  ImmutableSet<Stream<String>> testSplitToStream() {
    return ImmutableSet.of(
        Streams.stream(Splitter.on(':').split("foo")),
        Splitter.on(',').splitToList(new StringBuilder("bar")).stream());
  }
}
