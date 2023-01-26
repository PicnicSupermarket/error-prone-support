package tech.picnic.errorprone.bugpatterns.testngtojunit;

import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;

public final class TestNGUtil {
  public static final Matcher<AnnotationTree> TESTNG_ANNOTATION =
      isType("org.testng.annotations.Test");

  public static final Matcher<MethodTree> VALUE_FACTORY_METHOD =
      hasAnnotation("org.testng.annotations.DataProvider");

  private TestNGUtil() {}
}
