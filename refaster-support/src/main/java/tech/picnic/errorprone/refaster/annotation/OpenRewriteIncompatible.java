package tech.picnic.errorprone.refaster.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that no OpenRewrite recipe is generated for the annotated Refaster rule.
 *
 * <p>Such rules are available to Error Prone users, but not to users of the OpenRewrite recipes
 * derived from this project's Refaster rules. This is generally the result of the rule's use of a
 * construct that the {@code rewrite-templating} annotation processor does not (yet) support.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface OpenRewriteIncompatible {}
