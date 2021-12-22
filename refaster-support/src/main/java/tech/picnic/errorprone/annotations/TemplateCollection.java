package tech.picnic.errorprone.annotations;

import java.lang.annotation.ElementType;
import javax.annotation.meta.TypeQualifierDefault;

/**
 * Links the `*TemplatesTest{Input,Output} file to the collection of Refaster templates that are
 * tested in the class annotated with this annotation. As a value it should reference a class that
 * contains a collection of Refaster templates.
 *
 * <p>As a result of this annotation, we can perform additional validations. For instance, make sure
 * that the order of the templates matches the order in which the Refaster templates are defined.
 */
@TypeQualifierDefault(ElementType.TYPE)
public @interface TemplateCollection {
  /**
   * The class of the Refaster template that should be applied to the method body of the test
   * method.
   *
   * @return class of the Refaster template
   */
  Class<?> value();
}
