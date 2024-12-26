package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link CharSequence}s. */
@OnlineDocumentation
final class CharSequenceRules {
  private CharSequenceRules() {}

  /**
   * Prefer {@link CharSequence#isEmpty()} over alternatives that consult the char sequence's
   * length.
   */
  // XXX: Drop this rule once we (and OpenRewrite) no longer support projects targeting Java 14 or
  // below.
  static final class CharSequenceIsEmpty {
    @BeforeTemplate
    boolean before(CharSequence charSequence) {
      return Refaster.anyOf(
          charSequence.length() == 0, charSequence.length() <= 0, charSequence.length() < 1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(CharSequence charSequence) {
      return charSequence.isEmpty();
    }
  }
}
