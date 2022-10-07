package tech.picnic.errorprone.plugin;

public enum DocgenPart {
  BUGPATTERN("bug-pattern-test-data.jsonl", new BugPatternExtractor()),
  BUGPATTERN_TEST("bug-pattern-data.jsonl", new BugPatternTestsExtractor()),
  REFASTER_TEMPLATE_TEST_INPUT("refaster-test-input-data.jsonl", new RefasterTestExtractor()),
  REFASTER_TEMPLATE_TEST_OUTPUT("refaster-test-output-data.jsonl", new RefasterTestExtractor());

  private final String dataFileName;
  private final DocExtractor<?> extractor;

  DocgenPart(String dataFileName, DocExtractor<?> extractor) {
    this.dataFileName = dataFileName;
    this.extractor = extractor;
  }

  public DocExtractor<?> getExtractor() {
    return extractor;
  }

  public String getDataFileName() {
    return dataFileName;
  }
}
