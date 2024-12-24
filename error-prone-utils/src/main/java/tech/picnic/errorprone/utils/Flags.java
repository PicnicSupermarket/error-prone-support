package tech.picnic.errorprone.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
    ImmutableList<String> list = errorProneFlags.getListOrEmpty(name);
    return list.equals(ImmutableList.of("")) ? ImmutableList.of() : list;
  }

  /**
   * Returns the set of (comma-separated) arguments passed using the given Error Prone flag.
   *
   * @param errorProneFlags The full set of flags provided.
   * @param name The name of the flag of interest.
   * @return A non-{@code null} set of provided arguments; this set is empty if the flag was not
   *     provided, or if the flag's value is the empty string.
   * @implNote This method does not delegate to {@link ErrorProneFlags#getSetOrEmpty(String)}, as
   *     that method wouldn't allow us to identify a non-singleton set of empty strings; such a set
   *     should not be treated as empty.
   */
  public static ImmutableSet<String> getSet(ErrorProneFlags errorProneFlags, String name) {
    return ImmutableSet.copyOf(getList(errorProneFlags, name));
  }
}
