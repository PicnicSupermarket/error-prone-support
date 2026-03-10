package tech.picnic.errorprone.refaster.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Indicates that application of the annotated Refaster rule may yield uncompilable code. */
// XXX: Document the presence of this annotation on the website.
// XXX: Consider adding a severity level or likelihood assessment to this annotation.
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PossibleSourceIncompatibility {}
