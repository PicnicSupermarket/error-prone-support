package tech.picnic.errorprone.bugpatterns.util;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.ErrorProneFlags;

/** Helper methods for working with {@link ErrorProneFlags}. */
public final class Flags {
  private Flags() {}

  /**
   * Returns the list of (comma-separated) arguments passed using the given Error Prone flag.
   *
   * @param errorProneFlags The full set of flags provided.
   * @param name The name of the flag of interest.
   * @return A non-{@code null} list of provided arguments; this list is empty if the flag was not
   *     provided, or if the flag's value is the empty string.
   */
  public static ImmutableList<String> getList(ErrorProneFlags errorProneFlags, String name) {
    return errorProneFlags
        .getList(name)
        .map(ImmutableList::copyOf)
        .filter(flags -> !flags.equals(ImmutableList.of("")))
        .orElseGet(ImmutableList::of);
  }
}
