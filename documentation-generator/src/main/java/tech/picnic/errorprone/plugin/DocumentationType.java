package tech.picnic.errorprone.plugin;

enum DocumentationType {
  BUG_PATTERN("bugpattern", new BugPatternExtractor());

  private final String outputFileNamePrefix;

  @SuppressWarnings("ImmutableEnumChecker" /* `DocumentationExtractor` is effectively immutable. */)
  private final DocumentationExtractor<?> docExtractor;

  DocumentationType(String outputFileNamePrefix, DocumentationExtractor<?> documentationExtractor) {
    this.outputFileNamePrefix = outputFileNamePrefix;
    this.docExtractor = documentationExtractor;
  }

  String getOutputFileNamePrefix() {
    return outputFileNamePrefix;
  }

  DocumentationExtractor<?> getDocumentationExtractor() {
    return docExtractor;
  }
}
