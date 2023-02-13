package tech.picnic.errorprone.bugpatterns.util;

import static tech.picnic.errorprone.bugpatterns.util.JavaKeywords.isValidIdentifier;

import com.google.errorprone.VisitorState;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import java.util.Optional;

/** A set of helper methods for detecting conflicts that would be caused when applying fixes. */
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
   *       existing method declaration in its class or a supertype.
   *   <li>Whether the rename would in fact clash with a static import. (It could be that a static
   *       import of the same name is only referenced from lexical scopes in which the method under
   *       consideration cannot be referenced directly.)
   * </ul>
   *
   * @param method The method considered for renaming.
   * @param newName The newly proposed name for the method.
   * @param state The {@link VisitorState} to use when searching for blockers.
   * @return A human-readable argument against assigning the proposed name to the given method, or
   *     {@link Optional#empty()} if no blocker was found.
   */
  public static Optional<String> findMethodRenameBlocker(
      MethodSymbol method, String newName, VisitorState state) {
    if (isExistingMethodName(method.owner.type, newName, state)) {
      return Optional.of(
          String.format(
              "a method named `%s` is already defined in this class or a supertype", newName));
    }

    if (isSimpleNameStaticallyImported(newName, state)) {
      return Optional.of(String.format("`%s` is already statically imported", newName));
    }

    if (!isValidIdentifier(newName)) {
      return Optional.of(String.format("`%s` is not a valid identifier", newName));
    }

    return Optional.empty();
  }

  private static boolean isExistingMethodName(Type clazz, String name, VisitorState state) {
    return ASTHelpers.matchingMethods(state.getName(name), method -> true, clazz, state.getTypes())
        .findAny()
        .isPresent();
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
