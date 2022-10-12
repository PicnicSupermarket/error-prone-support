package tech.picnic.errorprone.refaster.annotation;

import com.google.errorprone.BugPattern.SeverityLevel;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes the severity of a Refaster rule or group of Refaster rules.
 *
 * <p>The default severity is the severity assigned to the {@code Refaster} bug checker, which may
 * be controlled explicitly by running Error Prone with e.g. {@code -Xep:Refaster:WARN}. Annotations
 * on nested classes override the severity associated with any enclosing class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Severity {
  /**
   * The expected severity of any match of the annotated Refaster rule(s).
   *
   * @return An Error Prone severity level.
   */
  SeverityLevel value();
}
