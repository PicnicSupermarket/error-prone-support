package tech.picnic.errorprone.refaster.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes the intent of a Refaster rule or group of Refaster rules.
 *
 * <p>Annotations on nested classes override the description associated with any enclosing class.
 */
// XXX: This becomes easier to use once we can use text blocks.
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Description {
  /**
   * A description of the annotated Refaster rule(s).
   *
   * @return A non-{@code null} string.
   */
  String value();
}
