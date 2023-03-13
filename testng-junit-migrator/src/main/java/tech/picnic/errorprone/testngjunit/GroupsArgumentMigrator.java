package tech.picnic.errorprone.testngjunit;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import java.util.Optional;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link tech.picnic.errorprone.testngjunit.Migrator} that migrates the {@code group} argument.
 */
@Immutable
final class GroupsArgumentMigrator implements Migrator {

  @Override
  public Optional<SuggestedFix> createFix(
      ClassTree classTree, MethodTree methodTree, ExpressionTree dataValue, VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder().addImport("org.junit.jupiter.api.Tag");
    extractGroups(dataValue, state)
        .forEach(group -> fix.prefixWith(methodTree, String.format("@Tag(%s)", group)));
    return Optional.of(fix.build());
  }

  @Override
  public boolean canFix(
      TestNGMetadata metadata,
      TestNGMetadata.AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state) {
    return annotation.getArguments().containsKey("groups");
  }

  private static ImmutableList<String> extractGroups(ExpressionTree dataValue, VisitorState state) {
    if (dataValue.getKind() == Tree.Kind.STRING_LITERAL) {
      return ImmutableList.of(SourceCode.treeToString(dataValue, state));
    }

    if (dataValue.getKind() != Tree.Kind.NEW_ARRAY) {
      return ImmutableList.of();
    }

    NewArrayTree groupsTree = (NewArrayTree) dataValue;
    return groupsTree.getInitializers().stream()
        .map(initializer -> SourceCode.treeToString(initializer, state))
        .collect(toImmutableList());
  }
}
