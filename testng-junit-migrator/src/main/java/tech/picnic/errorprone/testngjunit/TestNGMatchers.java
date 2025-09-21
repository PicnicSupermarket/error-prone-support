package tech.picnic.errorprone.testngjunit;

import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.TestNgMatchers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;

/**
 * A collection of TestNG-specific helper methods and {@link Matcher}s.
 *
 * <p>These constants and methods are additions to the ones found in {@link TestNgMatchers}.
 */
final class TestNGMatchers {
  /**
   * Matches the TestNG {@code Test} annotation specifically. As {@link
   * TestNgMatchers#hasTestNgAnnotation(ClassTree)} also other TestNG annotations.
   */
  public static final Matcher<AnnotationTree> TESTNG_TEST_ANNOTATION =
      isType("org.testng.annotations.Test");

  /** Matches the TestNG {@code DataProvider} annotation specifically. */
  public static final Matcher<MethodTree> TESTNG_VALUE_FACTORY_METHOD =
      hasAnnotation("org.testng.annotations.DataProvider");

  private TestNGMatchers() {}
}
