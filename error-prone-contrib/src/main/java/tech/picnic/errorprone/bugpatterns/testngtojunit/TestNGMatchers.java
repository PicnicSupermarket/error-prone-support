package tech.picnic.errorprone.bugpatterns.testngtojunit;

import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;

// XXX: It would be awesome to have a test for this class as well. It should be very similar to
// `MoreJUnitMatchersTest`.
public final class TestNGMatchers {
  public static final Matcher<AnnotationTree> TESTNG_ANNOTATION =
      isType("org.testng.annotations.Test");

  // XXX: Should `VALUE` be in this name?
  public static final Matcher<MethodTree> TESTNG_VALUE_FACTORY_METHOD =
      hasAnnotation("org.testng.annotations.DataProvider");

  private TestNGMatchers() {}
}
