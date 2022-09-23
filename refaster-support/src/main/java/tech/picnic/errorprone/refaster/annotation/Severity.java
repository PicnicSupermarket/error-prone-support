package tech.picnic.errorprone.refaster.annotation;

import com.google.errorprone.BugPattern.SeverityLevel;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes the severity of a Refaster template or group of Refaster templates.
 *
 * <p>The default severity is {@link SeverityLevel#SUGGESTION}. Annotations on nested classes
 * override the severity associated with any enclosing class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Severity {
  /**
   * The expected severity of any match of the annotated Refaster template(s).
   *
   * @return An Error Prone severity level.
   */
  SeverityLevel value();
}
