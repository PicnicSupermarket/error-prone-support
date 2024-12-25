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
  static final class CharSequenceIsEmpty {
    @BeforeTemplate
    boolean before(CharSequence ch) {
      return Refaster.anyOf(ch.length() == 0, ch.length() <= 0, ch.length() < 1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(CharSequence ch) {
      return ch.isEmpty();
    }
  }
}
