package tech.picnic.errorprone.testngjunit;

import static com.google.auto.common.MoreStreams.toImmutableList;
import static com.sun.source.tree.Tree.Kind.MEMBER_SELECT;
import static com.sun.source.tree.Tree.Kind.NEW_ARRAY;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import java.util.Optional;
import tech.picnic.errorprone.testngjunit.TestNgMetadata.AnnotationMetadata;
import tech.picnic.errorprone.util.SourceCode;

/** A {@link AttributeMigrator} that migrates the {@code expectedExceptions} attribute. */
@Immutable
final class ExpectedExceptionsAttributeMigrator implements AttributeMigrator {
  @Override
  public Optional<SuggestedFix> migrate(
      TestNgMetadata metadata,
      AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state) {

    // XXX: New more conservative way:
    String methodName = methodTree.getName().toString();

    AnnotationTree testAnnotation =
        ASTHelpers.getAnnotationWithSimpleName(ASTHelpers.getAnnotations(methodTree), "Test");
    SuggestedFix.Builder fix = SuggestedFix.builder().delete(testAnnotation);

    Optional<String> exception =
        Optional.ofNullable(annotation.getAttributes().get("expectedExceptions"))
            .flatMap(expectedExceptions -> getExpectedException(expectedExceptions, state));
    if (exception.isEmpty()) {
      return Optional.empty();
    }

    String newMethod =
        """
            @Test
            void test%s() {
              assertThrows(%s, () -> %s());
            }
        """
            .formatted(
                Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1),
                exception.orElseThrow(),
                methodName);
    fix.prefixWith(methodTree, newMethod)
        .addImport("org.junit.jupiter.api.Assertions.assertThrows");
    return Optional.of(fix.build());

    /* return Optional.ofNullable(annotation.getAttributes().get("expectedExceptions"))
    .map(
        expectedExceptions ->
            getExpectedException(expectedExceptions, state)
                .map(
                    expectedException -> {
                      SuggestedFix.Builder fix =
                          SuggestedFix.builder()
                              .replace(
                                  methodTree.getBody(),
                                  buildWrappedBody(
                                      methodTree.getBody(), expectedException, state));
                      ImmutableList<String> removedExceptions =
                          getRemovedExceptions(expectedExceptions, state);
                      if (!removedExceptions.isEmpty()) {
                        fix.prefixWith(
                            methodTree,
                            String.format(
                                "// XXX: Removed handling of `%s` because this migration doesn't support%n// XXX: multiple expected exceptions.%n",
                                String.join(", ", removedExceptions)));
                      }

                      return fix.build();
                    })
                .orElseGet(SuggestedFix::emptyFix));*/
  }

  private static Optional<String> getExpectedException(
      ExpressionTree expectedExceptions, VisitorState state) {
    if (expectedExceptions.getKind() == NEW_ARRAY) {
      NewArrayTree arrayTree = (NewArrayTree) expectedExceptions;
      if (arrayTree.getInitializers().isEmpty()) {
        return Optional.empty();
      }

      return Optional.of(SourceCode.treeToString(arrayTree.getInitializers().get(0), state));
    } else if (expectedExceptions.getKind() == MEMBER_SELECT) {
      return Optional.of(SourceCode.treeToString(expectedExceptions, state));
    }

    return Optional.empty();
  }

  private static ImmutableList<String> getRemovedExceptions(
      ExpressionTree expectedExceptions, VisitorState state) {
    if (expectedExceptions.getKind() != NEW_ARRAY) {
      return ImmutableList.of();
    }

    NewArrayTree arrayTree = (NewArrayTree) expectedExceptions;
    return arrayTree.getInitializers().subList(1, arrayTree.getInitializers().size()).stream()
        .map(initializer -> SourceCode.treeToString(initializer, state))
        .collect(toImmutableList());
  }

  private static String buildWrappedBody(BlockTree tree, String exception, VisitorState state) {
    return String.format(
        "{%norg.junit.jupiter.api.Assertions.assertThrows(%s, () -> %s);%n}",
        exception, SourceCode.treeToString(tree, state));
  }
}
