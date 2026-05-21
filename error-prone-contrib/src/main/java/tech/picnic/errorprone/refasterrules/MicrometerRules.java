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
// XXX: Consider replacing the `TagsOfN` rules with a bug checker, so that various other expressions
// (e.g. those creating other collection types, those passing in tags some other way, or those
// passing in more tags) can be replaced as well.
@OnlineDocumentation
final class MicrometerRules {
  private MicrometerRules() {}

  /** Prefer {@link Tags#of(Tag...)} over less idiomatic alternatives. */
  static final class TagsOf1 {
    @BeforeTemplate
    ImmutableCollection<Tag> before(Tag e1) {
      return Refaster.anyOf(ImmutableSet.of(e1), ImmutableList.of(e1));
    }

    @AfterTemplate
    Tags after(Tag e1) {
      return Tags.of(e1);
    }
  }

  /** Prefer {@link Tags#of(Tag...)} over less idiomatic alternatives. */
  static final class TagsOf2 {
    @BeforeTemplate
    ImmutableCollection<Tag> before(Tag e1, Tag e2) {
      return Refaster.anyOf(ImmutableSet.of(e1, e2), ImmutableList.of(e1, e2));
    }

    @AfterTemplate
    Tags after(Tag e1, Tag e2) {
      return Tags.of(e1, e2);
    }
  }

  /** Prefer {@link Tags#of(Tag...)} over less idiomatic alternatives. */
  static final class TagsOf3 {
    @BeforeTemplate
    ImmutableCollection<Tag> before(Tag e1, Tag e2, Tag e3) {
      return Refaster.anyOf(ImmutableSet.of(e1, e2, e3), ImmutableList.of(e1, e2, e3));
    }

    @AfterTemplate
    Tags after(Tag e1, Tag e2, Tag e3) {
      return Tags.of(e1, e2, e3);
    }
  }

  /** Prefer {@link Tags#of(Tag...)} over less idiomatic alternatives. */
  static final class TagsOf4 {
    @BeforeTemplate
    ImmutableCollection<Tag> before(Tag e1, Tag e2, Tag e3, Tag e4) {
      return Refaster.anyOf(ImmutableSet.of(e1, e2, e3, e4), ImmutableList.of(e1, e2, e3, e4));
    }

    @AfterTemplate
    Tags after(Tag e1, Tag e2, Tag e3, Tag e4) {
      return Tags.of(e1, e2, e3, e4);
    }
  }

  /** Prefer {@link Tags#of(Tag...)} over less idiomatic alternatives. */
  static final class TagsOf5 {
    @BeforeTemplate
    ImmutableCollection<Tag> before(Tag e1, Tag e2, Tag e3, Tag e4, Tag e5) {
      return Refaster.anyOf(
          ImmutableSet.of(e1, e2, e3, e4, e5), ImmutableList.of(e1, e2, e3, e4, e5));
    }

    @AfterTemplate
    Tags after(Tag e1, Tag e2, Tag e3, Tag e4, Tag e5) {
      return Tags.of(e1, e2, e3, e4, e5);
    }
  }
}
