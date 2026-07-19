package tech.picnic.errorprone.utils;

import java.util.Optional;

/**
 * A parsed {@code <type>} or {@code <type>#<member>} specification, as accepted by several flags
 * throughout this project.
 *
 * @param type The (canonical) type name.
 * @param member The type member, if the specification identifies one.
 */
public record TypeMemberSpec(String type, Optional<String> member) {
  /**
   * Parses the given specification.
   *
   * <p>This method does not validate {@code spec}; callers requiring e.g. well-formedness checks
   * must perform these themselves.
   *
   * @param spec A string of the form {@code <type>} or {@code <type>#<member>}.
   * @return The parsed specification.
   */
  public static TypeMemberSpec parse(String spec) {
    int hash = spec.indexOf('#');
    return hash < 0
        ? new TypeMemberSpec(spec, Optional.empty())
        : new TypeMemberSpec(spec.substring(0, hash), Optional.of(spec.substring(hash + 1)));
  }
}
