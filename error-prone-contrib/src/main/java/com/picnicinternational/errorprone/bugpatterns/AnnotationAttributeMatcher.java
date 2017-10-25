package com.picnicinternational.errorprone.bugpatterns;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

// XXX: Redefine using auto-value
// XXX: Document design decision that the project stays as close as possible to Error Prone.
//      ^ ... and *therefore* uses Google Auto Value rather than Immutables.org.
/**
 * A matcher of (annotation, attribute) pairs.
 *
 * <p>This class allows one to define a whitelist or blacklist of annotations or their attributes.
 * Annotations are identified by their fully qualified name.
 */
final class AnnotationAttributeMatcher implements Serializable {
  private static final long serialVersionUID = 1L;

  private final boolean complement;
  private final ImmutableSet<String> wholeTypes;
  private final ImmutableSetMultimap<String, String> includedAttributes;
  private final ImmutableSetMultimap<String, String> excludedAttributes;

  private AnnotationAttributeMatcher(
      boolean complement,
      ImmutableSet<String> wholeTypes,
      ImmutableSetMultimap<String, String> includedAttributes,
      ImmutableSetMultimap<String, String> excludedAttributes) {
    this.complement = complement;
    this.wholeTypes = wholeTypes;
    this.includedAttributes = includedAttributes;
    this.excludedAttributes = excludedAttributes;
  }

  /**
   * Creates an {@link AnnotationAttributeMatcher}.
   *
   * <p>Each string provided to this method must be of the form {@code
   * "some.fully.qualified.AnnotationType"} or {@code
   * "some.fully.qualified.AnnotationType#attribute"}.
   *
   * @param inclusions If specified, only the listed annotations or annotation attributes are
   *     matched.
   * @param exclusions If specified, the listed annotations or annotation attributes are not
   *     matched.
   * @return A non-{@code null} {@link AnnotationAttributeMatcher}.
   */
  static AnnotationAttributeMatcher create(
      Optional<ImmutableList<String>> inclusions, Optional<ImmutableList<String>> exclusions) {
    Set<String> includedWholeTypes = new HashSet<>();
    Set<String> excludedWholeTypes = new HashSet<>();
    SetMultimap<String, String> includedAttributes = HashMultimap.create();
    SetMultimap<String, String> excludedAttributes = HashMultimap.create();

    inclusions.ifPresent(incl -> update(incl, includedWholeTypes, includedAttributes));
    exclusions.ifPresent(excl -> update(excl, excludedWholeTypes, excludedAttributes));
    includedWholeTypes.removeAll(excludedWholeTypes);
    includedAttributes.keySet().removeAll(includedWholeTypes);
    includedAttributes.keySet().removeAll(excludedWholeTypes);
    excludedAttributes.forEach(includedAttributes::remove);
    excludedAttributes.keySet().removeAll(excludedWholeTypes);

    return new AnnotationAttributeMatcher(
        !inclusions.isPresent(),
        ImmutableSet.copyOf(inclusions.isPresent() ? includedWholeTypes : excludedWholeTypes),
        ImmutableSetMultimap.copyOf(includedAttributes),
        ImmutableSetMultimap.copyOf(excludedAttributes));
  }

  private static void update(
      ImmutableList<String> enumeration,
      Set<String> wholeTypes,
      SetMultimap<String, String> attributeRestrictions) {
    for (String entry : enumeration) {
      int hash = entry.indexOf('#');
      if (hash < 0) {
        wholeTypes.add(entry);

      } else {
        String annotationType = entry.substring(0, hash);
        String attribute = entry.substring(hash + 1);
        attributeRestrictions.put(annotationType, attribute);
      }
    }
  }

  boolean matches(String annotationType, String attribute) {
    if (this.complement) {
      return !this.wholeTypes.contains(annotationType)
          && !this.excludedAttributes.containsEntry(annotationType, attribute);
    }

    return (this.wholeTypes.contains(annotationType)
            && !this.excludedAttributes.containsEntry(annotationType, attribute))
        || this.includedAttributes.containsEntry(annotationType, attribute);
  }
}
