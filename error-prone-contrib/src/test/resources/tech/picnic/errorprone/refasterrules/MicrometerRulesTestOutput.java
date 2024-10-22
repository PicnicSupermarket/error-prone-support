package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class MicrometerRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(ImmutableList.class);
  }

  ImmutableSet<Iterable<Tag>> testTagsOf1() {
    return ImmutableSet.of(Tags.of(Tag.of("foo", "bar")), Tags.of(Tag.of("foo", "bar")));
  }

  ImmutableSet<Iterable<Tag>> testTagsOf2() {
    return ImmutableSet.of(
        Tags.of(Tag.of("foo", "bar"), Tag.of("foo1", "bar1")),
        Tags.of(Tag.of("foo", "bar"), Tag.of("foo1", "bar1")));
  }

  ImmutableSet<Iterable<Tag>> testTagsOf3() {
    return ImmutableSet.of(
        Tags.of(Tag.of("foo", "bar"), Tag.of("foo1", "bar1"), Tag.of("foo2", "bar2")),
        Tags.of(Tag.of("foo", "bar"), Tag.of("foo1", "bar1"), Tag.of("foo2", "bar2")));
  }

  ImmutableSet<Iterable<Tag>> testTagsOf4() {
    return ImmutableSet.of(
        Tags.of(
            Tag.of("foo", "bar"),
            Tag.of("foo1", "bar1"),
            Tag.of("foo2", "bar2"),
            Tag.of("foo3", "bar3")),
        Tags.of(
            Tag.of("foo", "bar"),
            Tag.of("foo1", "bar1"),
            Tag.of("foo2", "bar2"),
            Tag.of("foo3", "bar3")));
  }

  ImmutableSet<Iterable<Tag>> testTagsOf5() {
    return ImmutableSet.of(
        Tags.of(
            Tag.of("foo", "bar"),
            Tag.of("foo1", "bar1"),
            Tag.of("foo2", "bar2"),
            Tag.of("foo3", "bar3"),
            Tag.of("foo4", "bar4")),
        Tags.of(
            Tag.of("foo", "bar"),
            Tag.of("foo1", "bar1"),
            Tag.of("foo2", "bar2"),
            Tag.of("foo3", "bar3"),
            Tag.of("foo4", "bar4")));
  }
}
