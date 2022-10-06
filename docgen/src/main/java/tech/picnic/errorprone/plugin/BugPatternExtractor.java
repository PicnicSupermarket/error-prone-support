package tech.picnic.errorprone.plugin;

//public final class BugPatternExtractor implements DocExtractor<BugPatternData> {
//  @Override
//  public BugPatternTestData extractData(ClassTree tree, VisitorState state) {
//    String name = tree.getSimpleName().toString().replace("Test", "");
//    ScanBugCheckerTestData scanner = new ScanBugCheckerTestData(state);
//
//    tree.getMembers().stream()
//        .filter(MethodTree.class::isInstance)
//        .map(MethodTree.class::cast)
//        .filter(m -> BUG_PATTERN_TEST.matches(m, state))
//        .forEach(m -> scanner.scan(m, null));
//
//    return BugPatternTestData.create(
//        name, scanner.getIdentification(), scanner.getInput(), scanner.getOutput());
//  }
//}
