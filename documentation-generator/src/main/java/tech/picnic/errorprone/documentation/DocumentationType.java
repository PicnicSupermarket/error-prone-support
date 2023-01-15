package tech.picnic.errorprone.documentation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sun.source.tree.ClassTree;
import java.util.EnumSet;
import java.util.Optional;

/** The types of documentation that can be identified and extracted from the source code. */
enum DocumentationType {
  BUG_PATTERN("bugpattern", new BugPatternExtractor());

  private static final ImmutableSet<DocumentationType> TYPES =
      Sets.immutableEnumSet(EnumSet.allOf(DocumentationType.class));

  private final String identifier;
  private final DocumentationExtractor<?> docExtractor;

  DocumentationType(String identifier, DocumentationExtractor<?> documentationExtractor) {
    this.identifier = identifier;
    this.docExtractor = documentationExtractor;
  }

  String getIdentifier() {
    return identifier;
  }

  DocumentationExtractor<?> getDocumentationExtractor() {
    return docExtractor;
  }

  static Optional<DocumentationType> findMatchingType(ClassTree tree) {
    return TYPES.stream()
        .filter(type -> type.getDocumentationExtractor().canExtract(tree))
        .findFirst();
  }
}
