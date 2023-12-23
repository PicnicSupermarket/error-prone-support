package tech.picnic.errorprone.documentation;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tech.picnic.errorprone.documentation.models.RefasterTemplateCollectionTestData;
import tech.picnic.errorprone.documentation.models.RefasterTemplateTestData;

@Immutable
@AutoService(Extractor.class)
@SuppressWarnings("rawtypes" /* See https://github.com/google/auto/issues/870. */)
public final class RefasterTestOutputExtractor
    implements Extractor<RefasterTemplateCollectionTestData> {
  private static final Pattern TEST_CLASS_NAME_PATTERN = Pattern.compile("(.*)Test");

  @Override
  public String identifier() {
    return "refaster-test-output";
  }

  @Override
  public Optional<RefasterTemplateCollectionTestData> tryExtract(
      ClassTree tree, VisitorState state) {
    // XXX: The `String.valueOf` call is a hack to avoid an NPE in the absence of an explicit
    // package declaration.
    if (!String.valueOf(state.getPath().getCompilationUnit().getPackageName()).contains("output")) {
      return Optional.empty();
    }

    Optional<String> className = getClassUnderTest(tree);
    if (className.isEmpty()) {
      return Optional.empty();
    }

    ImmutableList<RefasterTemplateTestData> templateTests =
        tree.getMembers().stream()
            .filter(MethodTree.class::isInstance)
            .map(MethodTree.class::cast)
            .filter(m -> m.getName().toString().startsWith("test"))
            .map(
                m ->
                    RefasterTemplateTestData.create(
                        m.getName().toString().replace("test", ""), getSourceCode(m, state)))
            .collect(toImmutableList());

    return Optional.of(
        RefasterTemplateCollectionTestData.create(
            className.orElseThrow(), /* isInput= */ false, templateTests));
  }

  private static Optional<String> getClassUnderTest(ClassTree tree) {
    return Optional.of(TEST_CLASS_NAME_PATTERN.matcher(tree.getSimpleName().toString()))
        .filter(Matcher::matches)
        .map(m -> m.group(1));
  }

  // XXX: Duplicated from `SourceCode`. Can we do better?
  private static String getSourceCode(MethodTree tree, VisitorState state) {
    String src = state.getSourceForNode(tree);
    return src != null ? src : tree.toString();
  }
}
