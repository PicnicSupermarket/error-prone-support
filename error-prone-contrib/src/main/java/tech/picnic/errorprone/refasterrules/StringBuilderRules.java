package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link StringBuilder}s. */
@OnlineDocumentation
final class StringBuilderRules {
  private StringBuilderRules() {}

  /** Prefer {@link StringBuilder#repeat(CharSequence, int)} over less efficient alternatives. */
  static final class StringBuilderRepeat {
    @BeforeTemplate
    StringBuilder before(StringBuilder sb, String str, int count) {
      return sb.append(str.repeat(count));
    }

    @AfterTemplate
    StringBuilder after(StringBuilder sb, String str, int count) {
      return sb.repeat(str, count);
    }
  }
}
