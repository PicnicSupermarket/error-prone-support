package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;

/** Refaster rules related to expressions dealing with Micrometer. */
// XXX: Consider replacing the `TagsOfN` rules with a bug checker, so that various other expressions
// (e.g. those creating other collection types, those passing in tags some other way, or those
// passing in more tags) can be replaced as well.
@OnlineDocumentation
final class MicrometerRules {
  private MicrometerRules() {}

  /** Prefer {@link Tags#of(Tag...)} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
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
  @PossibleSourceIncompatibility
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
  @PossibleSourceIncompatibility
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
  @PossibleSourceIncompatibility
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
  @PossibleSourceIncompatibility
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

  /** Prefer {@link Tags#of(Tag...)} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
  static final class TagsOf6 {
    @BeforeTemplate
    ImmutableList<Tag> before(Tag e1, Tag e2, Tag e3, Tag e4, Tag e5, Tag e6) {
      return ImmutableList.of(e1, e2, e3, e4, e5, e6);
    }

    @AfterTemplate
    Tags after(Tag e1, Tag e2, Tag e3, Tag e4, Tag e5, Tag e6) {
      return Tags.of(e1, e2, e3, e4, e5, e6);
    }
  }

  /** Prefer {@link Tags#of(Tag...)} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
  static final class TagsOf7 {
    @BeforeTemplate
    ImmutableList<Tag> before(Tag e1, Tag e2, Tag e3, Tag e4, Tag e5, Tag e6, Tag e7) {
      return ImmutableList.of(e1, e2, e3, e4, e5, e6, e7);
    }

    @AfterTemplate
    Tags after(Tag e1, Tag e2, Tag e3, Tag e4, Tag e5, Tag e6, Tag e7) {
      return Tags.of(e1, e2, e3, e4, e5, e6, e7);
    }
  }

  /** Prefer {@link Tags#of(Tag...)} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
  @SuppressWarnings("java:S107" /* Can't avoid many method parameters here. */)
  static final class TagsOf8 {
    @BeforeTemplate
    ImmutableList<Tag> before(Tag e1, Tag e2, Tag e3, Tag e4, Tag e5, Tag e6, Tag e7, Tag e8) {
      return ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8);
    }

    @AfterTemplate
    Tags after(Tag e1, Tag e2, Tag e3, Tag e4, Tag e5, Tag e6, Tag e7, Tag e8) {
      return Tags.of(e1, e2, e3, e4, e5, e6, e7, e8);
    }
  }

  /** Prefer {@link Tags#of(Tag...)} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
  @SuppressWarnings("java:S107" /* Can't avoid many method parameters here. */)
  static final class TagsOf9 {
    @BeforeTemplate
    ImmutableList<Tag> before(
        Tag e1, Tag e2, Tag e3, Tag e4, Tag e5, Tag e6, Tag e7, Tag e8, Tag e9) {
      return ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9);
    }

    @AfterTemplate
    Tags after(Tag e1, Tag e2, Tag e3, Tag e4, Tag e5, Tag e6, Tag e7, Tag e8, Tag e9) {
      return Tags.of(e1, e2, e3, e4, e5, e6, e7, e8, e9);
    }
  }

  /** Prefer {@link Tags#of(Tag...)} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
  @SuppressWarnings("java:S107" /* Can't avoid many method parameters here. */)
  static final class TagsOf10 {
    @BeforeTemplate
    ImmutableList<Tag> before(
        Tag e1, Tag e2, Tag e3, Tag e4, Tag e5, Tag e6, Tag e7, Tag e8, Tag e9, Tag e10) {
      return ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
    }

    @AfterTemplate
    Tags after(Tag e1, Tag e2, Tag e3, Tag e4, Tag e5, Tag e6, Tag e7, Tag e8, Tag e9, Tag e10) {
      return Tags.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
    }
  }

  /** Prefer {@link Tags#of(Tag...)} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
  @SuppressWarnings("java:S107" /* Can't avoid many method parameters here. */)
  static final class TagsOf11 {
    @BeforeTemplate
    ImmutableList<Tag> before(
        Tag e1, Tag e2, Tag e3, Tag e4, Tag e5, Tag e6, Tag e7, Tag e8, Tag e9, Tag e10, Tag e11) {
      return ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11);
    }

    @AfterTemplate
    Tags after(
        Tag e1, Tag e2, Tag e3, Tag e4, Tag e5, Tag e6, Tag e7, Tag e8, Tag e9, Tag e10, Tag e11) {
      return Tags.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11);
    }
  }

  /** Prefer {@link Tags#of(Tag...)} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
  static final class TagsOf6Varargs {
    @BeforeTemplate
    ImmutableSet<Tag> before(Tag e1, Tag e2, Tag e3, Tag e4, Tag e5, Tag e6, @Repeated Tag tags) {
      return ImmutableSet.of(e1, e2, e3, e4, e5, e6, Refaster.asVarargs(tags));
    }

    @AfterTemplate
    Tags after(Tag e1, Tag e2, Tag e3, Tag e4, Tag e5, Tag e6, @Repeated Tag tags) {
      return Tags.of(e1, e2, e3, e4, e5, e6, tags);
    }
  }

  /** Prefer {@link Tags#of(Tag...)} over less idiomatic alternatives. */
  @PossibleSourceIncompatibility
  @SuppressWarnings("java:S107" /* Can't avoid many method parameters here. */)
  static final class TagsOf12Varargs {
    @BeforeTemplate
    ImmutableList<Tag> before(
        Tag e1,
        Tag e2,
        Tag e3,
        Tag e4,
        Tag e5,
        Tag e6,
        Tag e7,
        Tag e8,
        Tag e9,
        Tag e10,
        Tag e11,
        Tag e12,
        @Repeated Tag tags) {
      return ImmutableList.of(
          e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, Refaster.asVarargs(tags));
    }

    @AfterTemplate
    Tags after(
        Tag e1,
        Tag e2,
        Tag e3,
        Tag e4,
        Tag e5,
        Tag e6,
        Tag e7,
        Tag e8,
        Tag e9,
        Tag e10,
        Tag e11,
        Tag e12,
        @Repeated Tag tags) {
      return Tags.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, tags);
    }
  }
}
