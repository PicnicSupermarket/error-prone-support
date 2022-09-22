package tech.picnic.errorprone.refaster.annotation;

import com.google.errorprone.BugPattern.SeverityLevel;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// XXX: By grouping multiple properties this annotation does not lend itself to overriding. Perhaps
// have separate annotations for each?
// XXX: ^ Additional argument: the current setup "requires" defaults, which then causes duplication
// with `AnnotatedCompositeCodeTransformer`.
// XXX: The name `TemplateCollection` isn't appropriate if used directly on a Refaster template.
// Find a more neutral name.
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface TemplateCollection {
  // XXX: This default is Error Prone Support-specific. Appropriate?
  String linkPattern() default "https://error-prone.picnic.tech/refastertemplates/%s";

  SeverityLevel severity() default SeverityLevel.SUGGESTION;

  String description() default "Refactoring opportunity";
}
