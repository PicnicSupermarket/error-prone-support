package tech.picnic.errorprone.plugin;

public enum DocType {
  BUG_PATTERN("bug-pattern-data.jsonl", new BugPatternExtractor()),
  BUG_PATTERN_TEST("bug-pattern-test-data.jsonl", new BugPatternTestsExtractor()),
  REFASTER_TEMPLATE_TEST_INPUT("refaster-test-input-data.jsonl", new RefasterTestExtractor()),
  REFASTER_TEMPLATE_TEST_OUTPUT("refaster-test-output-data.jsonl", new RefasterTestExtractor());

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
