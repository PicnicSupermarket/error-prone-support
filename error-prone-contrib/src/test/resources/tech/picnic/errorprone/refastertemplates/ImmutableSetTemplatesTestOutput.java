package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.ImmutableSetTemplates.EmptyImmutableSet;
import tech.picnic.errorprone.refastertemplates.ImmutableSetTemplates.ImmutableSetBuilder;
import tech.picnic.errorprone.refastertemplates.ImmutableSetTemplates.ImmutableSetCopyOfImmutableSet;
import tech.picnic.errorprone.refastertemplates.ImmutableSetTemplates.ImmutableSetCopyOfSetView;
import tech.picnic.errorprone.refastertemplates.ImmutableSetTemplates.IterableToImmutableSet;
import tech.picnic.errorprone.refastertemplates.ImmutableSetTemplates.SingletonImmutableSet;
import tech.picnic.errorprone.refastertemplates.ImmutableSetTemplates.StreamToImmutableSet;

@TemplateCollection(ImmutableSetTemplates.class)
final class ImmutableSetTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class,
        Collections.class,
        Streams.class,
        collectingAndThen(null, null),
        toList(),
        toSet());
  }

  @Template(ImmutableSetBuilder.class)
  ImmutableSet.Builder<String> testImmutableSetBuilder() {
    return ImmutableSet.builder();
  }

  @Template(EmptyImmutableSet.class)
  ImmutableSet<ImmutableSet<Integer>> testEmptyImmutableSet() {
    return ImmutableSet.of(ImmutableSet.of(), ImmutableSet.of());
  }

  @Template(SingletonImmutableSet.class)
  Set<String> testSingletonImmutableSet() {
    return ImmutableSet.of("foo");
  }

  @Template(IterableToImmutableSet.class)
  ImmutableSet<ImmutableSet<Integer>> testIterableToImmutableSet() {
    return ImmutableSet.of(
        ImmutableSet.copyOf(ImmutableList.of(1)),
        ImmutableSet.copyOf(ImmutableList.of(2)::iterator),
        ImmutableSet.copyOf(ImmutableList.of(3).iterator()),
        ImmutableSet.copyOf(ImmutableSet.of(4)),
        ImmutableSet.copyOf(ImmutableSet.of(5)::iterator),
        ImmutableSet.copyOf(ImmutableSet.of(6).iterator()),
        ImmutableSet.copyOf(new Integer[] {7}),
        ImmutableSet.copyOf(new Integer[] {8}));
  }

  @Template(StreamToImmutableSet.class)
  ImmutableSet<ImmutableSet<Integer>> testStreamToImmutableSet() {
    return ImmutableSet.of(
        Stream.of(1).collect(toImmutableSet()),
        Stream.of(2).collect(toImmutableSet()),
        Stream.of(3).collect(toImmutableSet()),
        Stream.of(4).collect(toImmutableSet()));
  }

  @Template(ImmutableSetCopyOfImmutableSet.class)
  ImmutableSet<Integer> testImmutableSetCopyOfImmutableSet() {
    return ImmutableSet.of(1, 2);
  }

  @Template(ImmutableSetCopyOfSetView.class)
  ImmutableSet<Integer> testImmutableSetCopyOfSetView() {
    return Sets.difference(ImmutableSet.of(1), ImmutableSet.of(2)).immutableCopy();
  }
}
