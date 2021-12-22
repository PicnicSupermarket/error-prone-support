package tech.picnic.errorprone.annotations;

import java.lang.annotation.ElementType;
import javax.annotation.meta.TypeQualifierDefault;

/**
 * Links the test method to the Refaster template under test. This allows for more extensive
 * automatic verification of the tests. For instance, verify that every template has a test and the
 * provided Refaster template _does_ change the content of the test method when it is applied.
 */
@TypeQualifierDefault(ElementType.METHOD)
public @interface Template {
  /**
   * The class of the Refaster template that should be applied to the method body of the test
   * method.
   *
   * @return class of the Refaster template
   */
  Class<?> value();
}
