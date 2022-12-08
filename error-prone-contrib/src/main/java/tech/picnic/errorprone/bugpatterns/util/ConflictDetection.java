package tech.picnic.errorprone.bugpatterns.util;

import static tech.picnic.errorprone.bugpatterns.util.JavaKeywords.isReservedKeyword;
import static tech.picnic.errorprone.bugpatterns.util.MoreASTHelpers.methodExistsInEnclosingClass;

import com.google.errorprone.VisitorState;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import java.util.Optional;

/**
 * A set of helper methods for finding conflicts which would be caused by applying certain fixes.
 */
public final class ConflictDetection {
  private ConflictDetection() {}

  /**
   * If applicable, returns a human-readable argument against assigning the given name to an
   * existing method.
   *
   * <p>This method implements imperfect heuristics. Things it currently does not consider include
   * the following:
   *
   * <ul>
   *   <li>Whether the rename would merely introduce a method overload, rather than clashing with an
   *       existing method declaration.
   *   <li>Whether the rename would cause a method in a superclass to be overridden.
   *   <li>Whether the rename would in fact clash with a static import. (It could be that a static
   *       import of the same name is only referenced from lexical scopes in which the method under
   *       consideration cannot be referenced directly.)
   * </ul>
   *
   * @param methodName The proposed name to assign.
   * @param state The {@link VisitorState} to use for searching for blockers.
   * @return A human-readable argument against assigning the proposed name to an existing method, or
   *     {@link Optional#empty()} if no blocker was found.
   */
  public static Optional<String> findMethodRenameBlocker(String methodName, VisitorState state) {
    if (methodExistsInEnclosingClass(methodName, state)) {
      return Optional.of(
          String.format("a method named `%s` already exists in this class", methodName));
    }

    if (isSimpleNameStaticallyImported(methodName, state)) {
      return Optional.of(String.format("`%s` is already statically imported", methodName));
    }

    if (isReservedKeyword(methodName)) {
      return Optional.of(String.format("`%s` is a reserved keyword", methodName));
    }

    return Optional.empty();
  }

  private static boolean isSimpleNameStaticallyImported(String simpleName, VisitorState state) {
    return state.getPath().getCompilationUnit().getImports().stream()
        .filter(ImportTree::isStatic)
        .map(ImportTree::getQualifiedIdentifier)
        .map(tree -> getStaticImportSimpleName(tree, state))
        .anyMatch(simpleName::contentEquals);
  }

  private static CharSequence getStaticImportSimpleName(Tree tree, VisitorState state) {
    String source = SourceCode.treeToString(tree, state);
    return source.subSequence(source.lastIndexOf('.') + 1, source.length());
  }
}
