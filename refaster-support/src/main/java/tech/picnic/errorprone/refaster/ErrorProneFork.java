package tech.picnic.errorprone.refaster;

import com.google.errorprone.ErrorProneOptions;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

/**
 * Utility class that enables the runtime to determine whether Picnic's fork of Error Prone is on
 * the classpath.
 *
 * @see <a href="https://github.com/PicnicSupermarket/error-prone">Picnic's Error Prone fork</a>
 */
public final class ErrorProneFork {
  private static final Optional<Method> ERROR_PRONE_OPTIONS_IS_SUGGESTIONS_AS_WARNINGS_METHOD =
      Arrays.stream(ErrorProneOptions.class.getDeclaredMethods())
          .filter(m -> Modifier.isPublic(m.getModifiers()))
          .filter(m -> "isSuggestionsAsWarnings".equals(m.getName()))
          .findFirst();

  private ErrorProneFork() {}

  /**
   * Tells whether the custom {@code -XepAllSuggestionsAsWarnings} flag is set.
   *
   * @param options The currently active Error Prone options.
   * @return {@code true} iff the Error Prone fork is available and the aforementioned flag is set.
   * @see <a href="https://github.com/google/error-prone/pull/3301">google/error-prone#3301</a>
   */
  public static boolean isSuggestionsAsWarningsEnabled(ErrorProneOptions options) {
    return ERROR_PRONE_OPTIONS_IS_SUGGESTIONS_AS_WARNINGS_METHOD
        .filter(m -> Boolean.TRUE.equals(invoke(m, options)))
        .isPresent();
  }

  private static Object invoke(Method method, Object obj, Object... args) {
    try {
      return method.invoke(obj, args);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(String.format("Failed to invoke method '%s'", method), e);
    }
  }
}
