package tech.picnic.errorprone.plugin;

enum DocumentationType {
  BUG_PATTERN("bugpattern", new BugPatternExtractor());

  private final String identifier;

  @SuppressWarnings("ImmutableEnumChecker" /* `DocumentationExtractor` is effectively immutable. */)
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
}
