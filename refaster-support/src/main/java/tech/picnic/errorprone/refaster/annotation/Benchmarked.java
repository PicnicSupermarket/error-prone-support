package tech.picnic.errorprone.refaster.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that benchmark(s) should be generated for the annotated Refaster rule or group of
 * Refaster rules.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Benchmarked {
  // XXX: Make configurable. E.g.
  // - While not supported, don't complain about `Refaster.anyOf` or other `Refaster` utility method
  //   usages.
  // - Specify warmup and measurement iterations.
  // - Specify output time unit.
  // - Value generation hints.f
  // Once configuration is supported, annotations on nested classes should override the configuration specified by outer classes.

  // XXX: Explain use. Allow restriction by name?
  public @interface Param {

  }
}
