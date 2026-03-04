package tech.picnic.errorprone.refaster.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a Refaster rule's {@code @AfterTemplate} expression type is not a subtype of all
 * {@code @BeforeTemplate} expression types, meaning the replacement may break source compatibility
 * at call sites that depend on the narrower type.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PossibleSourceIncompatibility {}
