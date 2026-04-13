package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link StringBuilder}s. */
@OnlineDocumentation
final class StringBuilderRules {
  private StringBuilderRules() {}

  /**
   * Prefer {@link StringBuilder#repeat(CharSequence, int)} over less efficient alternatives.
   *
   * <p><strong>Warning:</strong> this rule is not behavior preserving: while the original code
   * throws a {@link NullPointerException} if the repeated string is {@code null}, the replacement
   * code will repeat the literal string {@code "null"}.
   */
  // XXX: Introduce an Error Prone check that replaces single-character strings with `char`s where
  // possible. Here, that would enable invoking the `StringBuilder#repeat(int, int)` overload.
  static final class StringBuilderRepeat {
    @BeforeTemplate
    StringBuilder before(StringBuilder builder, String cs, int count) {
      return builder.append(cs.repeat(count));
    }

    @AfterTemplate
    StringBuilder after(StringBuilder builder, String cs, int count) {
      return builder.repeat(cs, count);
    }
  }
}
