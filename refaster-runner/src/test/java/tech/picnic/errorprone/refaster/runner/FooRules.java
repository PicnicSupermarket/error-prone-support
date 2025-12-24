package tech.picnic.errorprone.refaster.runner;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.Description;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.Severity;

/** An example rule collection used to test {@link CodeTransformers} and {@link Refaster}. */
final class FooRules {
  private FooRules() {}

  /** A simple rule for testing purposes, lacking any custom annotations. */
  static final class StringOfSizeZeroRule {
    @BeforeTemplate
    boolean before(String string) {
      return string.toCharArray().length == 0;
    }

    @AfterTemplate
    boolean after(String string) {
      return string.isEmpty();
    }
  }

  /**
   * A simple rule for testing purposes, matching the same set of expressions as {@link
   * StringOfSizeZeroRule}, but producing a larger replacement string.
   */
  static final class StringOfSizeZeroVerboseRule {
    @BeforeTemplate
    boolean before(String string) {
      return string.toCharArray().length == 0;
    }

    @AfterTemplate
    boolean after(String string) {
      return string.length() + 1 == 1;
    }
  }

  /** A simple rule for testing purposes, having several custom annotations. */
  @Description("A custom description about matching single-char strings")
  @OnlineDocumentation
  @Severity(WARNING)
  static final class StringOfSizeOneRule {
    @BeforeTemplate
    boolean before(String string) {
      return string.toCharArray().length == 1;
    }

    @AfterTemplate
    boolean after(String string) {
      return string.length() == 1;
    }
  }

  /** A rule that uses a member reference to exercise identifier extraction. */
  static final class PredicateTestRule {
    @BeforeTemplate
    boolean before(Predicate<String> predicate, String value) {
      return predicate.test(value);
    }

    @AfterTemplate
    boolean after(Predicate<String> predicate, String value) {
      return predicate.test(value);
    }
  }

  /** A rule that uses a member reference in the template to exercise visitMemberReference. */
  static final class StreamFilterRule {
    @BeforeTemplate
    Stream<String> before(List<String> list) {
      return list.stream().filter(String::isEmpty);
    }

    @AfterTemplate
    Stream<String> after(List<String> list) {
      return list.stream().filter(s -> s.isEmpty());
    }
  }

  /** A nested class with annotations that are inherited by the Refaster rules contained in it. */
  @Description("A custom subgroup description")
  @OnlineDocumentation("https://example.com/rule/${topLevelClassName}#${nestedClassName}")
  @Severity(ERROR)
  static final class ExtraGrouping {
    private ExtraGrouping() {}

    /** A simple rule for testing purposes, inheriting custom annotations. */
    static final class StringOfSizeTwoRule {
      @BeforeTemplate
      boolean before(String string) {
        return string.toCharArray().length == 2;
      }

      @AfterTemplate
      boolean after(String string) {
        return string.length() == 2;
      }
    }

    /** A simple rule for testing purposes, overriding custom annotations. */
    @Description("A custom description about matching three-char strings")
    @OnlineDocumentation("https://example.com/custom")
    @Severity(SUGGESTION)
    static final class StringOfSizeThreeRule {
      @BeforeTemplate
      boolean before(String string) {
        return string.toCharArray().length == 3;
      }

      @AfterTemplate
      boolean after(String string) {
        return string.length() == 3;
      }
    }
  }
}
