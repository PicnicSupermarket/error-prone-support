package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with Micrometer. */
// XXX: Consider replacing the `TagsOf[N]` rules with a bug checker, so that various other
// expressions (e.g. those creating other collection types, those passing in tags some other way, or
// those passing in more tags) can be replaced as wel.
@OnlineDocumentation
final class MicrometerRules {
  private MicrometerRules() {}

  /** Prefer using {@link Tags} over other immutable collections. */
  static final class TagsOf1 {
    @BeforeTemplate
    ImmutableCollection<Tag> before(Tag tag) {
      return Refaster.anyOf(ImmutableSet.of(tag), ImmutableList.of(tag));
    }

    @AfterTemplate
    Iterable<Tag> after(Tag tag) {
      return Tags.of(tag);
    }
  }

  /** Prefer using {@link Tags} over other immutable collections. */
  static final class TagsOf2 {
    @BeforeTemplate
    ImmutableCollection<Tag> before(Tag tag1, Tag tag2) {
      return Refaster.anyOf(ImmutableSet.of(tag1, tag2), ImmutableList.of(tag1, tag2));
    }

    @AfterTemplate
    Iterable<Tag> after(Tag tag1, Tag tag2) {
      return Tags.of(tag1, tag2);
    }
  }

  /** Prefer using {@link Tags} over other immutable collections. */
  static final class TagsOf3 {
    @BeforeTemplate
    ImmutableCollection<Tag> before(Tag tag1, Tag tag2, Tag tag3) {
      return Refaster.anyOf(ImmutableSet.of(tag1, tag2, tag3), ImmutableList.of(tag1, tag2, tag3));
    }

    @AfterTemplate
    Iterable<Tag> after(Tag tag1, Tag tag2, Tag tag3) {
      return Tags.of(tag1, tag2, tag3);
    }
  }

  /** Prefer using {@link Tags} over other immutable collections. */
  static final class TagsOf4 {
    @BeforeTemplate
    ImmutableCollection<Tag> before(Tag tag1, Tag tag2, Tag tag3, Tag tag4) {
      return Refaster.anyOf(
          ImmutableSet.of(tag1, tag2, tag3, tag4), ImmutableList.of(tag1, tag2, tag3, tag4));
    }

    @AfterTemplate
    Iterable<Tag> after(Tag tag1, Tag tag2, Tag tag3, Tag tag4) {
      return Tags.of(tag1, tag2, tag3, tag4);
    }
  }

  /** Prefer using {@link Tags} over other immutable collections. */
  static final class TagsOf5 {
    @BeforeTemplate
    ImmutableCollection<Tag> before(Tag tag1, Tag tag2, Tag tag3, Tag tag4, Tag tag5) {
      return Refaster.anyOf(
          ImmutableSet.of(tag1, tag2, tag3, tag4, tag5),
          ImmutableList.of(tag1, tag2, tag3, tag4, tag5));
    }

    @AfterTemplate
    Iterable<Tag> after(Tag tag1, Tag tag2, Tag tag3, Tag tag4, Tag tag5) {
      return Tags.of(tag1, tag2, tag3, tag4, tag5);
    }
  }
}
