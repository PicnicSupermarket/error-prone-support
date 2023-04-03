package tech.picnic.errorprone.documentation;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import java.util.regex.Pattern;
import tech.picnic.errorprone.documentation.models.RefasterTemplateCollectionTestData;
import tech.picnic.errorprone.documentation.models.RefasterTemplateTestData;

public final class RefasterTestInputExtractor
    implements Extractor<RefasterTemplateCollectionTestData> {
  private static final Pattern TEST_INPUT_CLASS_NAME_PATTERN = Pattern.compile("(.*)TestInput");

  @Override
  public String identifier() {
    return "refaster-test-input";
  }

  @Override
  public Optional<RefasterTemplateCollectionTestData> tryExtract(
      ClassTree tree, VisitorState state) {
    Optional<String> className = getClassUnderTest(tree);
    if (className.isEmpty()) {
      return Optional.empty();
    }
    String templateCollectionName = className.orElseThrow().replace("Test", "");

    ImmutableList<RefasterTemplateTestData> templateTests =
        tree.getMembers().stream()
            .filter(m -> m instanceof MethodTree)
            .map(MethodTree.class::cast)
            .filter(m -> m.getName().toString().startsWith("test"))
            .map(
                m ->
                    RefasterTemplateTestData.create(
                        m.getName().toString().replace("test", ""), getSourceCode(m, state)))
            .collect(toImmutableList());

    return Optional.of(
        RefasterTemplateCollectionTestData.create(templateCollectionName, true, templateTests));
  }

  private static Optional<String> getClassUnderTest(ClassTree tree) {
    return Optional.of(TEST_INPUT_CLASS_NAME_PATTERN.matcher(tree.getSimpleName().toString()))
        .filter(java.util.regex.Matcher::matches)
        .map(m -> m.group(1));
  }

  // XXX: Duplicated from `SourceCode`. Can we do better?
  private String getSourceCode(MethodTree tree, VisitorState state) {
    String src = state.getSourceForNode(tree);
    return src != null ? src : tree.toString();
  }
}
