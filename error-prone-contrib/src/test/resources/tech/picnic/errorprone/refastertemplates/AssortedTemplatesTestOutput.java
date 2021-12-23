package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.toImmutableEnumSet;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
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
    return Objects.checkIndex(0, 1);
  }

  @Template(CreateEnumMap.class)
  Map<RoundingMode, String> testCreateEnumMap() {
    return new EnumMap<>(RoundingMode.class);
  }

  @Template(MapGetOrNull.class)
  String testMapGetOrNull() {
    return ImmutableMap.of(1, "foo").get("bar");
  }

  @Template(StreamToImmutableEnumSet.class)
  ImmutableSet<BoundType> testStreamToImmutableEnumSet() {
    return Stream.of(BoundType.OPEN).collect(toImmutableEnumSet());
  }

  @Template(IteratorGetNextOrDefault.class)
  ImmutableSet<String> testIteratorGetNextOrDefault() {
    return ImmutableSet.of(
        Iterators.getNext(ImmutableList.of("a").iterator(), "foo"),
        Iterators.getNext(ImmutableList.of("b").iterator(), "bar"),
        Iterators.getNext(ImmutableList.of("c").iterator(), "baz"));
  }

  // XXX: Only the first statement is rewritten. Make smarter.
  @Template(LogicalImplication.class)
  ImmutableSet<Boolean> testLogicalImplication() {
    return ImmutableSet.of(
        toString().isEmpty() || true,
        !toString().isEmpty() || (toString().isEmpty() && true),
        3 < 4 || (3 >= 4 && true),
        3 >= 4 || (3 < 4 && true));
  }

  @Template(UnboundedSingleElementStream.class)
  Stream<String> testUnboundedSingleElementStream() {
    return Stream.generate(() -> "foo");
  }

  @Template(DisjointSets.class)
  ImmutableSet<Boolean> testDisjointSets() {
    return ImmutableSet.of(
        Collections.disjoint(ImmutableSet.of(1), ImmutableSet.of(2)),
        Collections.disjoint(ImmutableSet.of(3), ImmutableSet.of(4)));
  }

  @Template(DisjointCollections.class)
  ImmutableSet<Boolean> testDisjointCollections() {
    return ImmutableSet.of(
        Collections.disjoint(ImmutableList.of(1), ImmutableList.of(2)),
        Collections.disjoint(ImmutableList.of(3), ImmutableList.of(4)),
        Collections.disjoint(ImmutableList.of(5), ImmutableList.of(6)),
        Collections.disjoint(ImmutableList.of(7), ImmutableList.of(8)));
  }

  @Template(IterableIsEmpty.class)
  boolean testIterableIsEmpty() {
    return Iterables.isEmpty(ImmutableList.of());
  }

  @Template(MapKeyStream.class)
  Stream<String> testMapKeyStream() {
    return ImmutableMap.of("foo", 1).keySet().stream();
  }

  @Template(MapValueStream.class)
  Stream<Integer> testMapValueStream() {
    return ImmutableMap.of("foo", 1).values().stream();
  }

  @Template(SplitToStream.class)
  ImmutableSet<Stream<String>> testSplitToStream() {
    return ImmutableSet.of(
        Splitter.on(':').splitToStream("foo"),
        Splitter.on(',').splitToStream(new StringBuilder("bar")));
  }
}
