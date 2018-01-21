package tech.picnic.errorprone.bugpatterns;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class AnnotationAttributeMatcherTest {
  @Test
  public void testWithoutListings() {
    AnnotationAttributeMatcher matcher =
        AnnotationAttributeMatcher.create(Optional.empty(), Optional.empty());
    assertTrue(matcher.matches("foo", "bar"));
  }

  @Test
  public void testWithSingleFullAnnotationWhitelist() {
    AnnotationAttributeMatcher matcher =
        AnnotationAttributeMatcher.create(Optional.of(ImmutableList.of("foo")), Optional.empty());
    assertTrue(matcher.matches("foo", "bar"));
    assertTrue(matcher.matches("foo", "baz"));
    assertFalse(matcher.matches("quux", "bar"));
  }

  @Test
  public void testWithSingleAnnotationAttributeWhitelist() {
    AnnotationAttributeMatcher matcher =
        AnnotationAttributeMatcher.create(
            Optional.of(ImmutableList.of("foo#bar")), Optional.empty());
    assertTrue(matcher.matches("foo", "bar"));
    assertFalse(matcher.matches("foo", "baz"));
    assertFalse(matcher.matches("quux", "bar"));
  }

  @Test
  public void testWithSingleFullAnnotationBlacklist() {
    AnnotationAttributeMatcher matcher =
        AnnotationAttributeMatcher.create(Optional.empty(), Optional.of(ImmutableList.of("foo")));
    assertFalse(matcher.matches("foo", "bar"));
    assertFalse(matcher.matches("foo", "baz"));
    assertTrue(matcher.matches("quux", "bar"));
  }

  @Test
  public void testWithSingleAnnotationAttributeBlacklist() {
    AnnotationAttributeMatcher matcher =
        AnnotationAttributeMatcher.create(
            Optional.empty(), Optional.of(ImmutableList.of("foo#bar")));
    assertFalse(matcher.matches("foo", "bar"));
    assertTrue(matcher.matches("foo", "baz"));
    assertTrue(matcher.matches("quux", "bar"));
  }

  @Test
  public void testWithComplicatedConfiguration() {
    AnnotationAttributeMatcher matcher =
        AnnotationAttributeMatcher.create(
            Optional.of(ImmutableList.of("foo", "bar", "baz", "baz#1", "baz#2", "quux#1")),
            Optional.of(ImmutableList.of("foo", "baz#2")));
    assertFalse(matcher.matches("foo", "1"));
    assertFalse(matcher.matches("foo", "2"));
    assertTrue(matcher.matches("bar", "1"));
    assertTrue(matcher.matches("bar", "2"));
    assertTrue(matcher.matches("baz", "1"));
    assertFalse(matcher.matches("baz", "2"));
    assertTrue(matcher.matches("quux", "1"));
    assertFalse(matcher.matches("quux", "2"));
  }
}
