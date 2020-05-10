package tech.picnic.errorprone.refasterrules;

import static com.google.common.base.Predicates.containsPattern;

import com.google.common.base.Predicates;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.checkerframework.checker.regex.qual.Regex;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to code dealing with regular expressions. */
@OnlineDocumentation
final class PatternRules {
  private PatternRules() {}

  /** Prefer {@link Pattern#asPredicate()} over non-JDK alternatives. */
  // XXX: This rule could also replace `s -> pattern.matcher(s).find()`, though the lambda
  // expression may match functional interfaces other than `Predicate`. If we do add such a rule, we
  // should also add a rule that replaces `s -> pattern.matcher(s).matches()` with
  // `pattern.asMatchPredicate()`.
  static final class PatternAsPredicate {
    @BeforeTemplate
    Predicate<CharSequence> before(Pattern pattern) {
      return Predicates.contains(pattern);
    }

    @AfterTemplate
    Predicate<String> after(Pattern pattern) {
      return pattern.asPredicate();
    }
  }

  /** Prefer {@link Pattern#asPredicate()} over non-JDK alternatives. */
  static final class PatternCompileAsPredicate {
    @BeforeTemplate
    Predicate<CharSequence> before(@Regex String pattern) {
      return containsPattern(pattern);
    }

    @AfterTemplate
    Predicate<String> after(@Regex String pattern) {
      return Pattern.compile(pattern).asPredicate();
    }
  }
}
