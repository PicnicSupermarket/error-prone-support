package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.micrometer.core.instrument.Tag;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class MicrometerRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(ImmutableList.class);
  }

  ImmutableSet<Iterable<Tag>> testTagsOf1() {
    return ImmutableSet.of(
        ImmutableSet.of(Tag.of("foo", "v1")), ImmutableList.of(Tag.of("bar", "v2")));
  }

  ImmutableSet<Iterable<Tag>> testTagsOf2() {
    return ImmutableSet.of(
        ImmutableSet.of(Tag.of("foo", "v1"), Tag.of("bar", "v2")),
        ImmutableList.of(Tag.of("baz", "v3"), Tag.of("qux", "v4")));
  }

  ImmutableSet<Iterable<Tag>> testTagsOf3() {
    return ImmutableSet.of(
        ImmutableSet.of(Tag.of("foo", "v1"), Tag.of("bar", "v2"), Tag.of("baz", "v3")),
        ImmutableList.of(Tag.of("qux", "v4"), Tag.of("quux", "v5"), Tag.of("corge", "v6")));
  }

  ImmutableSet<Iterable<Tag>> testTagsOf4() {
    return ImmutableSet.of(
        ImmutableSet.of(
            Tag.of("foo", "v1"), Tag.of("bar", "v2"), Tag.of("baz", "v3"), Tag.of("qux", "v4")),
        ImmutableList.of(
            Tag.of("quux", "v5"),
            Tag.of("corge", "v6"),
            Tag.of("grault", "v7"),
            Tag.of("garply", "v8")));
  }

  ImmutableSet<Iterable<Tag>> testTagsOf5() {
    return ImmutableSet.of(
        ImmutableSet.of(
            Tag.of("foo", "v1"),
            Tag.of("bar", "v2"),
            Tag.of("baz", "v3"),
            Tag.of("qux", "v4"),
            Tag.of("quux", "v5")),
        ImmutableList.of(
            Tag.of("corge", "v6"),
            Tag.of("grault", "v7"),
            Tag.of("garply", "v8"),
            Tag.of("waldo", "v9"),
            Tag.of("fred", "v10")));
  }
}
