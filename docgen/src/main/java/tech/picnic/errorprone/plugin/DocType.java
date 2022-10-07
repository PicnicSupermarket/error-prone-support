package tech.picnic.errorprone.plugin;


public enum DocType {
  BUG_PATTERN("bug-pattern", new BugPatternExtractor()),
  BUG_PATTERN_TEST("bug-pattern-test", new BugPatternTestsExtractor()),
  REFASTER_TEMPLATE_TEST_INPUT("refaster-test-input", new RefasterTestExtractor()),
  REFASTER_TEMPLATE_TEST_OUTPUT("refaster-test-output", new RefasterTestExtractor());

  private final String outputFileNamePrefix;
  private final DocExtractor<?> extractor;

  DocType(String outputFileNamePrefix, DocExtractor<?> extractor) {
    this.outputFileNamePrefix = outputFileNamePrefix;
    this.extractor = extractor;
  }

  public String getOutputFileNamePrefix() {
    return outputFileNamePrefix;
  }

  public DocExtractor<?> getExtractor() {
    return extractor;
  }
}
