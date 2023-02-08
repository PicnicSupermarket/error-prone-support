package tech.picnic.errorprone.documentation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;
import java.util.EnumSet;
import java.util.Optional;

/** An enumeration of {@link Extractor} types. */
enum ExtractorType {
  BUG_PATTERN("bugpattern", new BugPatternExtractor()),
  BUG_PATTERN_TEST("bugpattern-test", new BugPatternTestExtractor());

  private static final ImmutableSet<ExtractorType> TYPES =
      Sets.immutableEnumSet(EnumSet.allOf(ExtractorType.class));

  private final String identifier;
  private final Extractor<?> extractor;

  ExtractorType(String identifier, Extractor<?> extractor) {
    this.identifier = identifier;
    this.extractor = extractor;
  }

  String getIdentifier() {
    return identifier;
  }

  @SuppressWarnings("java:S1452" /* The extractor returns data of an unspecified type. */)
  Extractor<?> getExtractor() {
    return extractor;
  }

  static Optional<ExtractorType> findMatchingType(ClassTree tree, VisitorState state) {
    return TYPES.stream().filter(type -> type.getExtractor().canExtract(tree, state)).findFirst();
  }
}
