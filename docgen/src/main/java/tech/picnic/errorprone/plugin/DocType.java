package tech.picnic.errorprone.plugin;

public enum DocType {
  BUG_PATTERN("bug-pattern", new BugPatternExtractor()),
  BUG_PATTERN_TEST("bug-pattern-test", new BugPatternTestsExtractor()),
  //  REFASTER("refaster", new RefasterExtractor()),
  REFASTER_TEMPLATE_TEST_INPUT("refaster-test-input", new RefasterTestExtractor()),
  REFASTER_TEMPLATE_TEST_OUTPUT("refaster-test-output", new RefasterTestExtractor());

  private final String outputFileNamePrefix;
  private final DocExtractor<?> docExtractor;

  DocType(String outputFileNamePrefix, DocExtractor<?> docExtractor) {
    this.outputFileNamePrefix = outputFileNamePrefix;
    this.docExtractor = docExtractor;
  }

  public String getOutputFileNamePrefix() {
    return outputFileNamePrefix;
  }

  public DocExtractor<?> getDocExtractor() {
    return docExtractor;
  }
}
