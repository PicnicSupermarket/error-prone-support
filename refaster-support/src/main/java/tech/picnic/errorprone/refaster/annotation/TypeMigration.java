package tech.picnic.errorprone.refaster.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a Refaster rule or group of Refaster rules is intended to migrate away from the
 * indicated type.
 */
// XXX: Add support for `#unmigratedFields()`.
// XXX: Consider making this annotation `@Repeatable`, for cases where a single Refaster rules
// collection migrates away from multiple types.
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface TypeMigration {
  /**
   * The type migrated away from.
   *
   * @return The type generally used in the {@link
   *     com.google.errorprone.refaster.annotation.BeforeTemplate} methods of annotated Refaster
   *     rule(s).
   */
  Class<?> of();

  /**
   * The signatures of public methods and constructors that are not (yet) migrated by the annotated
   * Refaster rule(s).
   *
   * @return A possibly empty enumeration of method and constructor signatures, formatted according
   *     to {@link
   *     com.google.errorprone.util.Signatures#prettyMethodSignature(com.sun.tools.javac.code.Symbol.ClassSymbol,
   *     com.sun.tools.javac.code.Symbol.MethodSymbol)}.
   */
  String[] unmigratedMethods() default {};
}
