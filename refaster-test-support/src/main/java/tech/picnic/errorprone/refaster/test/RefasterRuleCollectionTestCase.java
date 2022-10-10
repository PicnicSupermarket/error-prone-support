package tech.picnic.errorprone.refaster.test;

import com.google.common.collect.ImmutableSet;

/**
 * Interface implemented by classes that exercise Refaster rules. These classes come in {@code
 * *Input.java} and {@code *Output.java} pairs, demonstrating the expected result of applying the
 * associated Refaster rules. These classes are <em>resources</em> on the test classpath.
 */
public interface RefasterRuleCollectionTestCase {
  /**
   * In some test classes there are types and statically imported methods that are fully replaced by
   * the Refaster rules under test. In those cases Refaster does not remove the associated imports,
   * while Google Java Formatter does. Subclasses can extend this method to enumerate such types and
   * statically imported methods, such that any imports present in the input file are also present
   * in the output file.
   *
   * @return Any values that are the result of expressions defined to ensure that all {@code
   *     *Input.java} import statements are also required to be present in the associated {@code
   *     *Output.java} file.
   */
  default ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of();
  }
}
