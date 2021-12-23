package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSet;

/**
 * Interface implemented by classes that exercise Refaster templates. These classes come in {@code
 * *Input.java} and {@code *Output.java} pairs, demonstrating the expected result of applying the
 * associated Refaster templates. These classes are <em>resources</em> in the {@code
 * tech.picnic.errorprone.bugpatterns} package.
 */
interface RefasterTemplateTestCase {
  /**
   * In some test classes there are types and statically imported methods that are fully replaced by
   * the Refaster templates under test. In those cases Refaster does not remove the associated
   * imports, while Google Java Formatter does. Subclasses can extend this method to enumerate such
   * types and statically imported methods, such that any imports present in the input file are also
   * present in the output file.
   */
  default ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of();
  }
}
