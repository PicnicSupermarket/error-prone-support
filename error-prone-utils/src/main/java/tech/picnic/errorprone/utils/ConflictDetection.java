package tech.picnic.errorprone.utils;

import static java.util.Objects.requireNonNull;

import com.google.errorprone.VisitorState;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

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
   *   <li>Whether the rename would in fact change the target of an existing method invocation in
   *       the scope of its containing class. (It could e.g. be that said invocation targets an
   *       identically-named method with different parameter types in some non-static nested type
   *       declaration.)
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

    if (isLocalMethodInvocation(newName, state)) {
      return Optional.of(String.format("another method named `%s` is in scope", newName));
    }

    if (!SourceCode.isValidIdentifier(newName)) {
      return Optional.of(String.format("`%s` is not a valid identifier", newName));
    }

    return Optional.empty();
  }

  private static boolean isExistingMethodName(Type clazz, String name, VisitorState state) {
    return ASTHelpers.matchingMethods(state.getName(name), method -> true, clazz, state.getTypes())
        .findAny()
        .isPresent();
  }

  private static boolean isLocalMethodInvocation(String name, VisitorState state) {
    return Boolean.TRUE.equals(
        new TreeScanner<Boolean, @Nullable Void>() {
          @Override
          public Boolean visitClass(ClassTree tree, @Nullable Void unused) {
            if (ASTHelpers.getSymbol(tree).isStatic()) {
              /*
               * Don't descend into static type definitions: in those context, any unqualified
               * method invocation cannot refer to a method in the outer scope.
               */
              return false;
            }

            return super.visitClass(tree, null);
          }

          @Override
          public Boolean visitMethodInvocation(MethodInvocationTree tree, @Nullable Void unused) {
            return (tree.getMethodSelect() instanceof IdentifierTree identifier
                    && name.contentEquals(identifier.getName()))
                || super.visitMethodInvocation(tree, null);
          }

          @Override
          public Boolean reduce(Boolean r1, Boolean r2) {
            return Boolean.TRUE.equals(r1) || Boolean.TRUE.equals(r2);
          }
        }.scan(
            requireNonNull(state.findEnclosing(ClassTree.class), "No enclosing class").getMembers(),
            null));
  }
}
