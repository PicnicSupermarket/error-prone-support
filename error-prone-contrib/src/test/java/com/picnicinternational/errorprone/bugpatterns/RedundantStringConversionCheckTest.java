package com.picnicinternational.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class RedundantStringConversionCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(RedundantStringConversionCheck.class, getClass());
  private final CompilationTestHelper customizedCompilationTestHelper =
      CompilationTestHelper.newInstance(RedundantStringConversionCheck.class, getClass())
          .setArgs(
              ImmutableList.of(
                  "-XepOpt:RedundantStringConversionCheck:ExtraConversionMethods=java.lang.Enum#name(),A.B#toString(int)"));
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(new RedundantStringConversionCheck(), getClass());

  // XXX: Here and in the tests below: add tests for the static `#toString` methods on boxed types.
  @Test
  public void testIdentificationOfIdentityTransformation() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "class A {",
            "  private final Object o = new Object();",
            "  private final String s = o.toString();",
            "",
            "  String[] m() {",
            "    return new String[] {",
            "      o.toString(),",
            "      // BUG: Diagnostic contains:",
            "      s.toString(),",
            "      String.valueOf(s),",
            "    };",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void testIdentificationWithinConcatenatingAssignment() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import java.math.BigInteger;",
            "",
            "class A {",
            "  private final BigInteger i = BigInteger.ZERO;",
            "  private String s = i.toString();",
            "",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    s += this.toString();",
            "    s += super.toString();",
            "    // BUG: Diagnostic contains:",
            "    s += i.toString();",
            "    s += i.toString(16);",
            "    // BUG: Diagnostic contains:",
            "    s += String.valueOf(i);",
            "    // BUG: Diagnostic contains:",
            "    s += String.valueOf((String) null);",
            "    // BUG: Diagnostic contains:",
            "    s += String.valueOf(null);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void testIdentificationWithinConcatenation() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import java.math.BigInteger;",
            "",
            "class A {",
            "  private final BigInteger i = BigInteger.ZERO;",
            "  private final String s = i.toString();",
            "",
            "  String[] m() {",
            "    return new String[] {",
            "      // BUG: Diagnostic contains:",
            "      s + this.toString(),",
            "      s + super.toString(),",
            "      // BUG: Diagnostic contains:",
            "      s + i.toString(),",
            "      s + i.toString(16),",
            "      // BUG: Diagnostic contains:",
            "      s + String.valueOf(i),",
            "      // BUG: Diagnostic contains:",
            "      s + String.valueOf((String) null),",
            "      // BUG: Diagnostic contains:",
            "      s + String.valueOf(null),",
            "",
            "      42 + this.toString(),",
            "      42 + super.toString(),",
            "      42 + i.toString(),",
            "      42 + i.toString(16),",
            "      42 + String.valueOf(i),",
            "      // BUG: Diagnostic contains:",
            "      42 + String.valueOf((String) null),",
            "      42 + String.valueOf(null),",
            "",
            "      // BUG: Diagnostic contains:",
            "      this.toString() + s,",
            "      super.toString() + s,",
            "      // BUG: Diagnostic contains:",
            "      i.toString() + s,",
            "      i.toString(16) + s,",
            "      // BUG: Diagnostic contains:",
            "      String.valueOf(i) + s,",
            "      // BUG: Diagnostic contains:",
            "      String.valueOf((String) null) + s,",
            "      // BUG: Diagnostic contains:",
            "      String.valueOf(null) + s,",
            "",
            "      this.toString() + 42,",
            "      super.toString() + 42,",
            "      i.toString() + 42,",
            "      i.toString(16) + 42,",
            "      String.valueOf(i) + 42,",
            "      // BUG: Diagnostic contains:",
            "      String.valueOf((String) null) + 42,",
            "      String.valueOf(null) + 42,",
            "",
            "      // BUG: Diagnostic contains:",
            "      this.toString() + this.toString(),",
            "      super.toString() + super.toString(),",
            "      // BUG: Diagnostic contains:",
            "      i.toString() + i.toString(),",
            "      i.toString(16) + i.toString(16),",
            "      // BUG: Diagnostic contains:",
            "      String.valueOf(i) + String.valueOf(i),",
            "      // BUG: Diagnostic contains:",
            "      String.valueOf((String) null) + String.valueOf((String) null),",
            "      // BUG: Diagnostic contains:",
            "      String.valueOf(null) + String.valueOf(null),",
            "    };",
            "  }",
            "}")
        .doTest();
  }

  // XXX: Test StringBuilder methods.

  // XXX: Also test the other formatter methods.
  @Test
  public void testIdentificationWithinFormatterMethod() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import java.util.Formattable;",
            "import java.util.Locale;",
            "",
            "class A {",
            "  private final Locale locale = Locale.ROOT;",
            "  private final Formattable f = (formatter, flags, width, precision) -> {};",
            "  private final Object o = new Object();",
            "  private final String s = o.toString();",
            "",
            "  void m() {",
            "    String.format(s, f);",
            "    String.format(s, o);",
            "    String.format(s, s);",
            "    String.format(s, f.toString());",
            "    // BUG: Diagnostic contains:",
            "    String.format(s, o.toString());",
            "    // BUG: Diagnostic contains:",
            "    String.format(s, String.valueOf(o));",
            "",
            "    String.format(locale, s, f);",
            "    String.format(locale, s, o);",
            "    String.format(locale, s, s);",
            "    String.format(locale, s, f.toString());",
            "    // BUG: Diagnostic contains:",
            "    String.format(locale, s, o.toString());",
            "    // BUG: Diagnostic contains:",
            "    String.format(locale, s, String.valueOf(o));",
            "",
            "    String.format(o.toString(), o);",
            "    // BUG: Diagnostic contains:",
            "    String.format(s.toString(), o);",
            "    String.format(locale.toString(), s, o);",
            "    String.format(locale, o.toString(), o);",
            "    // BUG: Diagnostic contains:",
            "    String.format(locale, s.toString(), o);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void testIdentificationWithinGuavaGuardMethod() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static com.google.common.base.Preconditions.checkState;",
            "import static com.google.common.base.Preconditions.checkArgument;",
            "import static com.google.common.base.Preconditions.checkNotNull;",
            "import static com.google.common.base.Verify.verify;",
            "import static com.google.common.base.Verify.verifyNotNull;",
            "",
            "import java.util.Formattable;",
            "",
            "class A {",
            "  private final Formattable f = (formatter, flags, width, precision) -> {};",
            "  private final Object o = new Object();",
            "  private final String s = o.toString();",
            "",
            "  void m() {",
            "    checkState(true, s, f);",
            "    // BUG: Diagnostic contains:",
            "    checkState(true, s, f.toString());",
            "    checkState(true, f.toString(), f);",
            "    // BUG: Diagnostic contains:",
            "    checkState(true, s.toString(), f);",
            "",
            "    checkArgument(true, s, f);",
            "    // BUG: Diagnostic contains:",
            "    checkArgument(true, s, f.toString());",
            "    checkArgument(true, f.toString(), f);",
            "    // BUG: Diagnostic contains:",
            "    checkArgument(true, s.toString(), f);",
            "",
            "    checkNotNull(o, s, f);",
            "    // BUG: Diagnostic contains:",
            "    checkNotNull(o, s, f.toString());",
            "    checkNotNull(o, f.toString(), f);",
            "    // BUG: Diagnostic contains:",
            "    checkNotNull(o, s.toString(), f);",
            "    checkNotNull(o.toString(), s, f);",
            "",
            "    verify(true, s, f);",
            "    // BUG: Diagnostic contains:",
            "    verify(true, s, f.toString());",
            "    verify(true, f.toString(), f);",
            "    // BUG: Diagnostic contains:",
            "    verify(true, s.toString(), f);",
            "",
            "    verifyNotNull(o, s, f);",
            "    // BUG: Diagnostic contains:",
            "    verifyNotNull(o, s, f.toString());",
            "    verifyNotNull(o, f.toString(), f);",
            "    // BUG: Diagnostic contains:",
            "    verifyNotNull(o, s.toString(), f);",
            "    verifyNotNull(o.toString(), s, f);",
            "  }",
            "}")
        .doTest();
  }

  // XXX: Also test the other log methods.
  @Test
  public void testIdentificationWithinSlf4jLoggerMethod() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import java.util.Formattable;",
            "import org.slf4j.Logger;",
            "import org.slf4j.LoggerFactory;",
            "import org.slf4j.Marker;",
            "import org.slf4j.MarkerFactory;",
            "",
            "class A {",
            "  private static final Logger LOG = LoggerFactory.getLogger(A.class);",
            "  private final Marker marker = MarkerFactory.getMarker(A.class.getName());",
            "  private final Formattable f = (formatter, flags, width, precision) -> {};",
            "  private final Object o = new Object();",
            "  private final String s = f.toString();",
            "  private final Throwable t = new Throwable();",
            "",
            "  void m() {",
            "    LOG.info(s, f);",
            "    // BUG: Diagnostic contains:",
            "    LOG.info(s, f.toString());",
            "    LOG.info(s, t.toString());",
            "    LOG.info(s, o, t.toString());",
            "    // BUG: Diagnostic contains:",
            "    LOG.info(s, t.toString(), o);",
            "",
            "    LOG.info(marker, s, f);",
            "    // BUG: Diagnostic contains:",
            "    LOG.info(marker, s, f.toString());",
            "    LOG.info(marker, s, t.toString());",
            "    LOG.info(marker, s, o, t.toString());",
            "    // BUG: Diagnostic contains:",
            "    LOG.info(marker, s, t.toString(), o);",
            "",
            "    LOG.info(f.toString(), f);",
            "    // BUG: Diagnostic contains:",
            "    LOG.info(s.toString(), f);",
            "    LOG.info(t.toString(), f);",
            "    LOG.info(marker.toString(), s, f);",
            "    LOG.info(marker, o.toString(), f);",
            "    // BUG: Diagnostic contains:",
            "    LOG.info(marker, s.toString(), f);",
            "    LOG.info(marker, t.toString(), f);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void testIdentificationOfCustomConversionMethod() {
    customizedCompilationTestHelper
        .addSourceLines(
            "A.java",
            "import java.util.Locale;",
            "import java.math.RoundingMode;",
            "",
            "class A {",
            "  static class B {",
            "    String name() {",
            "      return toString();",
            "    }",
            "",
            "    static String toString(int i) {",
            "      return Integer.toString(i);",
            "    }",
            "  }",
            "",
            "  private final B b = new B();",
            "  private final String s = b.toString();",
            "",
            "  String[] m() {",
            "    return new String[] {",
            "      s + b.name(),",
            "      // BUG: Diagnostic contains:",
            "      s + RoundingMode.UP.name(),",
            "      // BUG: Diagnostic contains:",
            "      s + mode().name(),",
            "      s + A.toString(42),",
            "      // BUG: Diagnostic contains:",
            "      s + B.toString(42),",
            "    };",
            "  }",
            "",
            "  RoundingMode mode() {",
            "    return RoundingMode.UP;",
            "  }",
            "",
            "  static String toString(int i) {",
            "    return Integer.toString(i);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void testReplacement() throws IOException {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import java.util.Locale;",
            "",
            "class A {",
            "  private final Locale locale = Locale.ROOT;",
            "  private final Object o = new Object();",
            "  private final String s = o.toString();",
            "",
            "  void m() {",
            "    String v1 = s.toString();",
            "    String v2 = \"foo\".toString();",
            "    String v3 = v2 + super.toString();",
            "    String v4 = 42 + String.valueOf((String) null);",
            "    String.format(\"%s\", o.toString());",
            "  }",
            "}")
        .addOutputLines(
            "out/A.java",
            "import java.util.Locale;",
            "",
            "class A {",
            "  private final Locale locale = Locale.ROOT;",
            "  private final Object o = new Object();",
            "  private final String s = o.toString();",
            "",
            "  void m() {",
            "    String v1 = s;",
            "    String v2 = \"foo\";",
            "    String v3 = v2 + super.toString();",
            "    String v4 = 42 + (String) null;",
            "    String.format(\"%s\", o);",
            "  }",
            "}")
        .doTest();
  }
}
