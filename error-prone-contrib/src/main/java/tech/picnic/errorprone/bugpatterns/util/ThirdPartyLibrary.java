package tech.picnic.errorprone.bugpatterns.util;

import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.suppliers.Supplier;
import com.sun.tools.javac.code.ClassFinder;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Symbol.CompletionFailure;
import com.sun.tools.javac.code.Symbol.ModuleSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.util.Name;

/**
 * Utility class that helps decide whether it is appropriate to introduce references to (well-known)
 * third-party libraries.
 *
 * <p>This class should be used by {@link BugChecker}s that may otherwise suggest the introduction
 * of code that depends on possibly-not-present third-party libraries.
 */
// XXX: Consider giving users more fine-grained control. This would be beneficial in cases where a
// dependency is on the classpath, but new usages are undesirable.
public enum ThirdPartyLibrary {
  /**
   * AssertJ.
   *
   * @see <a href="https://assertj.github.io/doc">AssertJ documentation</a>
   */
  ASSERTJ("org.assertj.core.api.Assertions"),
  /**
   * Google's Guava.
   *
   * @see <a href="https://github.com/google/guava">Guava on GitHub</a>
   */
  GUAVA("com.google.common.collect.ImmutableList"),
  /**
   * VMWare's Project Reactor.
   *
   * @see <a href="https://projectreactor.io">Home page</a>
   */
  REACTOR("reactor.core.publisher.Flux");

  private static final String IGNORE_CLASSPATH_COMPAT_FLAG =
      "ErrorProneSupport:IgnoreClasspathCompat";

  @SuppressWarnings("ImmutableEnumChecker" /* Supplier is deterministic. */)
  private final Supplier<Boolean> canUse;

  /**
   * Instantiates a {@link ThirdPartyLibrary} enum value.
   *
   * @param witnessFqcn The fully-qualified class name of a type that is expected to be on the
   *     classpath iff the associated third-party library is on the classpath.
   */
  ThirdPartyLibrary(String witnessFqcn) {
    this.canUse = VisitorState.memoize(state -> canIntroduceUsage(witnessFqcn, state));
  }

  /**
   * Tells whether it is okay to introduce a dependency on this well-known third party library in
   * the given context.
   *
   * @param state The context under consideration.
   * @return {@code true} iff it is okay to assume or create a dependency on this library.
   */
  public boolean isIntroductionAllowed(VisitorState state) {
    return canUse.get(state);
  }

  private static boolean canIntroduceUsage(String className, VisitorState state) {
    return shouldIgnoreClasspath(state) || isKnownClass(className, state);
  }

  /**
   * Attempts to determine whether a class with the given FQCN is on the classpath.
   *
   * <p>The {@link VisitorState}'s symbol table is consulted first. If the type has not yet been
   * loaded, then an attempt is made to do so.
   *
   * @param className The type of interest.
   * @param state The context under consideration.
   * @return {@code true} iff the indicated type is on the classpath.
   */
  public static boolean isKnownClass(String className, VisitorState state) {
    return state.getTypeFromString(className) != null || canLoadClass(className, state);
  }

  private static boolean canLoadClass(String className, VisitorState state) {
    ClassFinder classFinder = ClassFinder.instance(state.context);
    Symtab symtab = state.getSymtab();
    // XXX: Drop support for targeting Java 8 once the oldest supported JDK drops such support.
    ModuleSymbol module =
        Source.instance(state.context).compareTo(Source.JDK9) < 0
            ? symtab.noModule
            : symtab.unnamedModule;
    Name binaryName = state.binaryNameFromClassname(className);
    try {
      classFinder.loadClass(module, binaryName);
      return true;
    } catch (
        @SuppressWarnings("java:S1166" /* Not exceptional. */)
        CompletionFailure e) {
      return false;
    }
  }

  private static boolean shouldIgnoreClasspath(VisitorState state) {
    return state
        .errorProneOptions()
        .getFlags()
        .getBoolean(IGNORE_CLASSPATH_COMPAT_FLAG)
        .orElse(Boolean.FALSE);
  }
}
