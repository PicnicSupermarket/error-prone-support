package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public final class AnnotationAttributeMatcherTest {
  @Test
  void withoutListings() {
    AnnotationAttributeMatcher matcher =
        AnnotationAttributeMatcher.create(Optional.empty(), ImmutableList.of());
    assertThat(matcher.matches("foo", "bar")).isTrue();
  }

  @Test
  void withSingleFullAnnotationWhitelist() {
    AnnotationAttributeMatcher matcher =
        AnnotationAttributeMatcher.create(Optional.of(ImmutableList.of("foo")), ImmutableList.of());
    assertThat(matcher.matches("foo", "bar")).isTrue();
    assertThat(matcher.matches("foo", "baz")).isTrue();
    assertThat(matcher.matches("quux", "bar")).isFalse();
  }

  @Test
  void withSingleAnnotationAttributeWhitelist() {
    AnnotationAttributeMatcher matcher =
        AnnotationAttributeMatcher.create(
            Optional.of(ImmutableList.of("foo#bar")), ImmutableList.of());
    assertThat(matcher.matches("foo", "bar")).isTrue();
    assertThat(matcher.matches("foo", "baz")).isFalse();
    assertThat(matcher.matches("quux", "bar")).isFalse();
  }

  @Test
  void withSingleFullAnnotationBlacklist() {
    AnnotationAttributeMatcher matcher =
        AnnotationAttributeMatcher.create(Optional.empty(), ImmutableList.of("foo"));
    assertThat(matcher.matches("foo", "bar")).isFalse();
    assertThat(matcher.matches("foo", "baz")).isFalse();
    assertThat(matcher.matches("quux", "bar")).isTrue();
  }

  @Test
  void withSingleAnnotationAttributeBlacklist() {
    AnnotationAttributeMatcher matcher =
        AnnotationAttributeMatcher.create(Optional.empty(), ImmutableList.of("foo#bar"));
    assertThat(matcher.matches("foo", "bar")).isFalse();
    assertThat(matcher.matches("foo", "baz")).isTrue();
    assertThat(matcher.matches("quux", "bar")).isTrue();
  }

  @Test
  void withComplicatedConfiguration() {
    AnnotationAttributeMatcher matcher =
        AnnotationAttributeMatcher.create(
            Optional.of(ImmutableList.of("foo", "bar", "baz", "baz#1", "baz#2", "quux#1")),
            ImmutableList.of("foo", "baz#2"));
    assertThat(matcher.matches("foo", "1")).isFalse();
    assertThat(matcher.matches("foo", "2")).isFalse();
    assertThat(matcher.matches("bar", "1")).isTrue();
    assertThat(matcher.matches("bar", "2")).isTrue();
    assertThat(matcher.matches("baz", "1")).isTrue();
    assertThat(matcher.matches("baz", "2")).isFalse();
    assertThat(matcher.matches("quux", "1")).isTrue();
    assertThat(matcher.matches("quux", "2")).isFalse();
  }
}
