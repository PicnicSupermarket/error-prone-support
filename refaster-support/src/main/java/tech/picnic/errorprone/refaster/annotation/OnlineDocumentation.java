package tech.picnic.errorprone.refaster.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Signals that a Refaster template or group of Refaster templates comes with online documentation.
 *
 * <p>The provided value may be a full URL, or a URL pattern containing a single {@code %s}
 * placeholder. If a placeholder is present, then it will be replaced with the name of the
 * associated Refaster template any time the URL is rendered.
 *
 * <p>By default it is assumed that the Refaster template(s) are documented on the Error Prone
 * Support website. Annotations on nested classes override the documentation URL associated with any
 * enclosing class.
 */
// XXX: Is the `%s` protocol sufficiently generic for non-Picnic use cases?
// XXX: The documentation is misleading, in that the generated anchor isn't mentioned.
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface OnlineDocumentation {
  /**
   * The URL or URL pattern of the website at which the annotated Refaster template(s) are
   * documented.
   *
   * @return A non-{@code null} string.
   */
  // XXX: This default is Error Prone Support-specific. Appropriate? (The alternative is to repeat
  // this URL pattern in many places.) If we drop this, also update the class documentation.
  String value() default "https://error-prone.picnic.tech/refastertemplates/%s";
}
