package tech.picnic.errorprone.refaster.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes the intent of a Refaster template or group of Refaster templates.
 *
 * <p>Annotations on nested classes override the description associated with any enclosing class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Description {
  /**
   * A description of the annotated Refaster template(s).
   *
   * @return A non-{@code null} string.
   */
  String value();
}
