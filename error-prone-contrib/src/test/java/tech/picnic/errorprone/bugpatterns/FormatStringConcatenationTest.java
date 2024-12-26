package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class FormatStringConcatenationTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(FormatStringConcatenation.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import static com.google.common.base.Preconditions.checkArgument;
            import static com.google.common.base.Preconditions.checkNotNull;
            import static com.google.common.base.Preconditions.checkState;
            import static com.google.common.base.Verify.verify;
            import static org.assertj.core.api.Assertions.assertThat;
            import static org.assertj.core.api.SoftAssertions.assertSoftly;

            import java.util.Formatter;
            import java.util.Locale;
            import org.assertj.core.api.Assertions;
            import org.assertj.core.api.BDDAssertions;
            import org.assertj.core.api.Fail;
            import org.assertj.core.api.ThrowableAssertAlternative;
            import org.assertj.core.api.WithAssertions;
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            import org.slf4j.Marker;

            class A {
              private static final Logger LOG = LoggerFactory.getLogger(A.class);

              void negative() {
                hashCode();
                equals(new A());
                equals(toString());
                equals(0);
                equals("str");
                equals("str" + 0);
                equals(0 + 0);
                equals(0 - 0);
                equals("str " + toString());
              }

              void assertj() {
                assertThat(0).overridingErrorMessage(toString());
                assertThat(0).overridingErrorMessage("str");
                assertThat(0).overridingErrorMessage("str " + 0);
                assertThat(0).overridingErrorMessage("str %s", 2 * 3);
                assertThat(0).overridingErrorMessage("str %s", toString());
                // BUG: Diagnostic contains:
                assertThat(0).overridingErrorMessage("str " + hashCode() / 2);
                // BUG: Diagnostic contains:
                assertThat(0).overridingErrorMessage(("str " + toString()));
                // BUG: Diagnostic contains:
                assertThat(0).overridingErrorMessage("str " + toString());
                // BUG: Diagnostic contains:
                assertThat(0).overridingErrorMessage("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                assertThat(0).withFailMessage("str " + toString());
                // BUG: Diagnostic contains:
                assertThat(0).withFailMessage("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                assertSoftly(softly -> softly.fail("str " + toString()));
                // BUG: Diagnostic contains:
                assertSoftly(softly -> softly.fail("%s " + toString(), "arg"));
                assertSoftly(softly -> softly.fail("str " + toString(), new Throwable()));

                // BUG: Diagnostic contains:
                assertThat("").isEqualTo("str " + toString());
                // BUG: Diagnostic contains:
                assertThat("").isEqualTo("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                assertThat(new Error()).hasMessage("str " + toString());
                // BUG: Diagnostic contains:
                assertThat(new Error()).hasMessage("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                assertThat(new Error()).hasMessageContaining("str " + toString());
                // BUG: Diagnostic contains:
                assertThat(new Error()).hasMessageContaining("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                assertThat(new Error()).hasMessageEndingWith("str " + toString());
                // BUG: Diagnostic contains:
                assertThat(new Error()).hasMessageEndingWith("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                assertThat(new Error()).hasMessageStartingWith("str " + toString());
                // BUG: Diagnostic contains:
                assertThat(new Error()).hasMessageStartingWith("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                assertThat(new Error()).hasRootCauseMessage("str " + toString());
                // BUG: Diagnostic contains:
                assertThat(new Error()).hasRootCauseMessage("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                assertThat(new Error()).hasStackTraceContaining("str " + toString());
                // BUG: Diagnostic contains:
                assertThat(new Error()).hasStackTraceContaining("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                assertThat(0).as("str " + toString());
                // BUG: Diagnostic contains:
                assertThat(0).as("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                assertThat(0).describedAs("str " + toString());
                // BUG: Diagnostic contains:
                assertThat(0).describedAs("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                ((ThrowableAssertAlternative) null).withMessage("str " + toString());
                // BUG: Diagnostic contains:
                ((ThrowableAssertAlternative) null).withMessage("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                ((ThrowableAssertAlternative) null).withMessageContaining("str " + toString());
                // BUG: Diagnostic contains:
                ((ThrowableAssertAlternative) null).withMessageContaining("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                ((ThrowableAssertAlternative) null).withMessageEndingWith("str " + toString());
                // BUG: Diagnostic contains:
                ((ThrowableAssertAlternative) null).withMessageEndingWith("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                ((ThrowableAssertAlternative) null).withMessageStartingWith("str " + toString());
                // BUG: Diagnostic contains:
                ((ThrowableAssertAlternative) null).withMessageStartingWith("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                ((ThrowableAssertAlternative) null).withStackTraceContaining("str " + toString());
                // BUG: Diagnostic contains:
                ((ThrowableAssertAlternative) null).withStackTraceContaining("%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                ((WithAssertions) null).fail("str " + toString());
                // BUG: Diagnostic contains:
                ((WithAssertions) null).fail("%s " + toString(), "arg");
                ((WithAssertions) null).fail("str " + toString(), new Throwable());

                // BUG: Diagnostic contains:
                Assertions.fail("str " + toString());
                // BUG: Diagnostic contains:
                Assertions.fail("%s " + toString(), "arg");
                Assertions.fail("str " + toString(), new Throwable());

                // BUG: Diagnostic contains:
                BDDAssertions.fail("str " + toString());
                // BUG: Diagnostic contains:
                BDDAssertions.fail("%s " + toString(), "arg");
                BDDAssertions.fail("str " + toString(), new Throwable());

                // BUG: Diagnostic contains:
                Fail.fail("str " + toString());
                // BUG: Diagnostic contains:
                Fail.fail("%s " + toString(), "arg");
                Fail.fail("str " + toString(), new Throwable());
              }

              void guava() {
                checkArgument(true);
                checkArgument(true, toString());
                checkArgument(true, "str");
                checkArgument(true, "str " + 0);
                checkArgument(true, "str %s", 2 * 3);
                checkArgument(true, "str %s", toString());
                // BUG: Diagnostic contains:
                checkArgument(true, "str " + hashCode() / 2);
                // BUG: Diagnostic contains:
                checkArgument(true, ("str " + toString()));
                // BUG: Diagnostic contains:
                checkArgument(true, "str " + toString());
                // BUG: Diagnostic contains:
                checkArgument(true, "%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                checkNotNull(true, "str " + toString());
                // BUG: Diagnostic contains:
                checkNotNull(true, "%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                checkState(true, "str " + toString());
                // BUG: Diagnostic contains:
                checkState(true, "%s " + toString(), "arg");

                // BUG: Diagnostic contains:
                verify(true, "str " + toString());
                // BUG: Diagnostic contains:
                verify(true, "%s " + toString(), "arg");
              }

              void jdk() {
                String.format("str");
                String.format("str " + 0);
                String.format("str {}", 2 * 3);
                String.format("str {}", toString());
                // BUG: Diagnostic contains:
                String.format("str " + hashCode() / 2);
                // BUG: Diagnostic contains:
                String.format(("str " + toString()));
                // BUG: Diagnostic contains:
                String.format("str " + toString());
                // BUG: Diagnostic contains:
                String.format("{} " + toString(), "arg");

                String.format(Locale.ROOT, "str");
                String.format(Locale.ROOT, "str " + 0);
                String.format(Locale.ROOT, "str {}", 2 * 3);
                String.format(Locale.ROOT, "str {}", toString());
                // BUG: Diagnostic contains:
                String.format(Locale.ROOT, ("str " + toString()));
                // BUG: Diagnostic contains:
                String.format(Locale.ROOT, "str " + toString());
                // BUG: Diagnostic contains:
                String.format(Locale.ROOT, "{} " + toString(), "arg");

                // BUG: Diagnostic contains:
                new Formatter().format("str " + toString());
                // BUG: Diagnostic contains:
                new Formatter().format("{} " + toString(), "arg");

                // BUG: Diagnostic contains:
                new Formatter().format(Locale.ROOT, "str " + toString());
                // BUG: Diagnostic contains:
                new Formatter().format(Locale.ROOT, "{} " + toString(), "arg");
              }

              void slf4j() {
                LOG.debug("str");
                LOG.debug("str " + 0);
                LOG.debug("str {}", 2 * 3);
                LOG.debug("str {}", toString());
                // BUG: Diagnostic contains:
                LOG.debug("str " + hashCode() / 2);
                // BUG: Diagnostic contains:
                LOG.debug(("str " + toString()));
                // BUG: Diagnostic contains:
                LOG.debug("str " + toString());
                // BUG: Diagnostic contains:
                LOG.debug("{} " + toString(), "arg");

                LOG.debug((Marker) null, "str");
                LOG.debug((Marker) null, "str " + 0);
                LOG.debug((Marker) null, "str {}", 2 * 3);
                LOG.debug((Marker) null, "str {}", toString());
                // BUG: Diagnostic contains:
                LOG.debug((Marker) null, ("str " + toString()));
                // BUG: Diagnostic contains:
                LOG.debug((Marker) null, "str " + toString());
                // BUG: Diagnostic contains:
                LOG.debug((Marker) null, "{} " + toString(), "arg");

                // BUG: Diagnostic contains:
                LOG.error("str " + toString());
                // BUG: Diagnostic contains:
                LOG.error("{} " + toString(), "arg");

                // BUG: Diagnostic contains:
                LOG.error((Marker) null, "str " + toString());
                // BUG: Diagnostic contains:
                LOG.error((Marker) null, "{} " + toString(), "arg");

                // BUG: Diagnostic contains:
                LOG.info("str " + toString());
                // BUG: Diagnostic contains:
                LOG.info("{} " + toString(), "arg");

                // BUG: Diagnostic contains:
                LOG.info((Marker) null, "str " + toString());
                // BUG: Diagnostic contains:
                LOG.info((Marker) null, "{} " + toString(), "arg");

                // BUG: Diagnostic contains:
                LOG.trace("str " + toString());
                // BUG: Diagnostic contains:
                LOG.trace("{} " + toString(), "arg");

                // BUG: Diagnostic contains:
                LOG.trace((Marker) null, "str " + toString());
                // BUG: Diagnostic contains:
                LOG.trace((Marker) null, "{} " + toString(), "arg");

                // BUG: Diagnostic contains:
                LOG.warn("str " + toString());
                // BUG: Diagnostic contains:
                LOG.warn("{} " + toString(), "arg");

                // BUG: Diagnostic contains:
                LOG.warn((Marker) null, "str " + toString());
                // BUG: Diagnostic contains:
                LOG.warn((Marker) null, "{} " + toString(), "arg");
              }
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(FormatStringConcatenation.class, getClass())
        .addInputLines(
            "A.java",
            """
            import static com.google.common.base.Preconditions.checkArgument;
            import static org.assertj.core.api.Assertions.assertThat;

            import java.util.Locale;
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            import org.slf4j.Marker;

            class A {
              private static final Logger LOG = LoggerFactory.getLogger(A.class);

              void assertj() {
                assertThat(0).overridingErrorMessage(toString() + " str");
                assertThat(0).overridingErrorMessage("str " + toString());
                assertThat(0).overridingErrorMessage(toString() + toString());
                assertThat(0).overridingErrorMessage("str " + toString() + " word " + new A().hashCode());
                assertThat(0).overridingErrorMessage("str " + (toString() + " word ") + (hashCode() / 2));

                // Flagged but not auto-fixed.
                assertThat(0).overridingErrorMessage("%s " + toString(), "arg");
              }

              void guava() {
                checkArgument(true, "str " + toString());

                // Flagged but not auto-fixed.
                checkArgument(true, "%s " + toString(), "arg");
              }

              void jdk() {
                String.format("str " + toString());
                String.format(Locale.ROOT, "str " + toString());

                // Flagged but not auto-fixed.
                String.format("{} " + toString(), "arg");
                String.format(Locale.ROOT, "{} " + toString(), "arg");
              }

              void slf4j() {
                LOG.debug("str " + toString());
                LOG.debug((Marker) null, "str " + toString());

                // Flagged but not auto-fixed.
                LOG.debug("{} " + toString(), "arg");
                LOG.debug((Marker) null, "{} " + toString(), "arg");
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import static com.google.common.base.Preconditions.checkArgument;
            import static org.assertj.core.api.Assertions.assertThat;

            import java.util.Locale;
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            import org.slf4j.Marker;

            class A {
              private static final Logger LOG = LoggerFactory.getLogger(A.class);

              void assertj() {
                assertThat(0).overridingErrorMessage("%s str", toString());
                assertThat(0).overridingErrorMessage("str %s", toString());
                assertThat(0).overridingErrorMessage("%s%s", toString(), toString());
                assertThat(0).overridingErrorMessage("str %s word %s", toString(), new A().hashCode());
                assertThat(0).overridingErrorMessage("str %s word %s", toString(), hashCode() / 2);

                // Flagged but not auto-fixed.
                assertThat(0).overridingErrorMessage("%s " + toString(), "arg");
              }

              void guava() {
                checkArgument(true, "str %s", toString());

                // Flagged but not auto-fixed.
                checkArgument(true, "%s " + toString(), "arg");
              }

              void jdk() {
                String.format("str %s", toString());
                String.format(Locale.ROOT, "str %s", toString());

                // Flagged but not auto-fixed.
                String.format("{} " + toString(), "arg");
                String.format(Locale.ROOT, "{} " + toString(), "arg");
              }

              void slf4j() {
                LOG.debug("str {}", toString());
                LOG.debug((Marker) null, "str {}", toString());

                // Flagged but not auto-fixed.
                LOG.debug("{} " + toString(), "arg");
                LOG.debug((Marker) null, "{} " + toString(), "arg");
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
