package tech.picnic.errorprone.refaster.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Signals that a Refaster rule or group of Refaster rules comes with online documentation.
 *
 * <p>The provided value may be a full URL, or a URL pattern containing either or both of the
 * {@value TOP_LEVEL_CLASS_URL_PLACEHOLDER} and {@value NESTED_CLASS_URL_PLACEHOLDER} placeholders.
 *
 * <p>By default it is assumed that the Refaster rule(s) are documented on the Error Prone Support
 * website. Annotations on nested classes override the documentation URL associated with any
 * enclosing class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface OnlineDocumentation {
  /**
   * The URL placeholder value that will be replaced with the name of the top-level class in which
   * the annotated Refaster template is located.
   */
  String TOP_LEVEL_CLASS_URL_PLACEHOLDER = "${topLevelClassName}";
  /**
   * The URL placeholder value that will be replaced with the name of the nested class in which the
   * annotated Refaster template is located, if applicable.
   *
   * <p>If the Refaster template is not defined in a nested class then this placeholder will be
   * replaced with the empty string. In case the Refaster template is syntactically nested inside a
   * deeper hierarchy of classes, then this placeholder will be replaced with concatenation of the
   * names of all these classes (except the top-level class name), separated by dots.
   */
  String NESTED_CLASS_URL_PLACEHOLDER = "${nestedClassName}";

  /**
   * The URL placeholder value that will be replaced with the name of the top-level class in which
   * the annotated Refaster rule is located.
   */
  String TOP_LEVEL_CLASS_URL_PLACEHOLDER = "${topLevelClassName}";
  /**
   * The URL placeholder value that will be replaced with the name of the nested class in which the
   * annotated Refaster rule is located, if applicable.
   *
   * <p>If the Refaster rule is not defined in a nested class then this placeholder will be replaced
   * with the empty string. In case the Refaster rule is syntactically nested inside a deeper
   * hierarchy of classes, then this placeholder will be replaced with concatenation of the names of
   * all these classes (except the top-level class name), separated by dots.
   */
  String NESTED_CLASS_URL_PLACEHOLDER = "${nestedClassName}";

  /**
   * The URL or URL pattern of the website at which the annotated Refaster rule(s) are documented.
   *
   * @return A non-{@code null} string, optionally containing the {@value
   *     TOP_LEVEL_CLASS_URL_PLACEHOLDER} and {@value NESTED_CLASS_URL_PLACEHOLDER} placeholders.
   */
  String value() default
      "https://error-prone.picnic.tech/refasterrules/"
          + TOP_LEVEL_CLASS_URL_PLACEHOLDER
          + '#'
          + NESTED_CLASS_URL_PLACEHOLDER;
}

