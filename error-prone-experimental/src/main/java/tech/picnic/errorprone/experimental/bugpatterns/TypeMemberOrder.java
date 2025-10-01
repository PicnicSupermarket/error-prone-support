package tech.picnic.errorprone.experimental.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.sun.tools.javac.code.Flags.ENUM;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.fixes.Replacement;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneToken;
import com.google.errorprone.util.ErrorProneTokens;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.parser.Tokens.TokenKind;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Position;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Modifier;

/**
 * A {@link BugChecker} that flags classes with a non-canonical member order.
 *
 * <p>Class members should be ordered as follows:
 *
 * <ol>
 *   <li>Static fields
 *   <li>Instance fields
 *   <li>Static initializer blocks
 *   <li>Instance initializer blocks
 *   <li>Constructors
 *   <li>Methods, Classes annotated with {@code org.junit.jupiter.api.Nested}
 *   <li>Nested classes, interfaces and enums
 * </ol>
 *
 * <p>Moving members as bug checker fixes introduces a relatively large number of line changes.
 * These changes should conflict with other fixes. To prevent the `TypeMemberOrder` checker from
 * conflicting with itself, it matches compilation units rather than types, and it merges its fixes
 * within the given compilation unit before reporting it as a single fix.
 *
 * <p>This should guarantee that the `TypeMemberOrder` can be run on any code-base as an individual
 * check. If it's conflicting with other bug checkers, one can run the `TypeMemberOrder` and all
 * other checks in two separate steps.
 *
 * @see <a
 *     href="https://checkstyle.sourceforge.io/apidocs/com/puppycrawl/tools/checkstyle/checks/coding/DeclarationOrderCheck.html">Checkstyle's
 *     {@code DeclarationOrderCheck}</a>
 */
// XXX: Consider introducing support for ordering members in records and annotation definitions.
// TODO: Reason `ErrorProneTestHelperSourceFormat` in tests.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Type members should be defined in a canonical order",
    link = BUG_PATTERNS_BASE_URL + "TypeMemberOrder",
    linkType = CUSTOM,
    severity = WARNING,
    tags = STYLE)
public final class TypeMemberOrder extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link TypeMemberOrder} instance. */
  public TypeMemberOrder() {}

  @Override
  public Description matchCompilationUnit(
      CompilationUnitTree compilationUnitTree, VisitorState state) {
    SuggestedFix.Builder suggestedFixes = SuggestedFix.builder();
    for (Tree tree : compilationUnitTree.getTypeDecls()) {
      // TODO: Are there other type declarations than class trees in a compilation unit? Imports
      // maybe?
      if (tree instanceof ClassTree classTree) {
        suggestedFixes.merge(
            matchClass(
                classTree,
                requireNonNull(state.getSourceCode(), "Source code"),
                ((JCTree.JCCompilationUnit) compilationUnitTree).endPositions,
                state));
      }
    }
    SuggestedFix suggestedFix = suggestedFixes.build();
    if (suggestedFix.isEmpty()) {
      return Description.NO_MATCH;
    }
    return describeMatch(compilationUnitTree, suggestedFix);
  }

  private SuggestedFix matchClass(
      ClassTree tree,
      CharSequence source,
      EndPosTable compilationUnitEndPosTable,
      VisitorState state) {
    boolean isTreeKindSupported =
        tree.getKind() == Kind.CLASS
            || tree.getKind() == Kind.INTERFACE
            || tree.getKind() == Kind.ENUM;
    int bodyStartPos = getBodyStartPos(tree, state);
    if (isSuppressed(tree, state) || !isTreeKindSupported || bodyStartPos == Position.NOPOS) {
      /*
       * In case the tree is suppressed or unsupported, we skip sorting it.
       *
       * Otherwise, its body's position is unknown in the source code. This generally means that
       * (part of) its code was generated. Even if the source code for a subset of its members is
       * available, dealing with this edge case is not worth the trouble.
       */
      return SuggestedFix.emptyFix();
    }

    ImmutableList<TypeMember> members = getAllTypeMembers(tree, bodyStartPos, state);
    boolean topLevelSorted = members.equals(ImmutableList.sortedCopyOf(members));

    if (topLevelSorted) {
      SuggestedFix.Builder nestedSuggestedFixes = SuggestedFix.builder();
      for (TypeMember member : members) {
        if (member.tree() instanceof ClassTree memberClassTree) {
          nestedSuggestedFixes.merge(
              matchClass(memberClassTree, source, compilationUnitEndPosTable, state));
        }
      }
      return nestedSuggestedFixes.build();
    }

    /* Find and pair each type member with its source code. For nested types, apply their nested
     * fixes first, and only propagate their fixed source code.
     */
    ImmutableMap.Builder<TypeMember, String> typeMemberSource = ImmutableMap.builder();
    for (TypeMember member : members) {
      if (member.tree() instanceof ClassTree memberClassTree) {
        SuggestedFix memberClassTreeFix =
            matchClass(memberClassTree, source, compilationUnitEndPosTable, state);
        @Var
        String memberSource =
            source.subSequence(member.startPosition(), member.endPosition()).toString();
        /*
         * Apply nested fixes "manually". The positions of the fixes' replacements are based on the
         * compilation unit's _original_ source code, while applying it we need to compensate for
         * (1) the member's own position, and (2) the change in length between the original and the
         * fixed source codes.
         */
        @Var int diff = -member.startPosition();
        for (Replacement replacement :
            memberClassTreeFix.getReplacements(compilationUnitEndPosTable)) {
          memberSource =
              memberSource.subSequence(0, replacement.startPosition() + diff)
                  + replacement.replaceWith()
                  + memberSource.subSequence(
                      replacement.endPosition() + diff, memberSource.length());
          diff +=
              replacement.replaceWith().length()
                  - (replacement.endPosition() - replacement.startPosition());
        }
        typeMemberSource.put(member, memberSource);
      } else {
        typeMemberSource.put(
            member, source.subSequence(member.startPosition(), member.endPosition()).toString());
      }
    }

    return sortTypeMembers(members, typeMemberSource.buildOrThrow());
  }

  /**
   * Returns all members that may be moved, i.e. members that have positions and not enumeration
   * constants.
   */
  private static ImmutableList<TypeMember> getAllTypeMembers(
      ClassTree tree, int bodyStartPos, VisitorState state) {
    ImmutableList.Builder<TypeMember> builder = ImmutableList.builder();
    @Var int currentStartPos = bodyStartPos;
    for (Tree member : tree.getMembers()) {
      if (state.getEndPosition(member) == Position.NOPOS || isEnumeratorConstant(member)) {
        continue;
      }

      int treeStartPos = currentStartPos;
      getMemberTypeOrdinal(member, state)
          .ifPresent(
              memberTypeOrdinal ->
                  builder.add(
                      new TypeMember(
                          member, treeStartPos, state.getEndPosition(member), memberTypeOrdinal)));
      currentStartPos = state.getEndPosition(member);
    }
    return builder.build();
  }

  /** Returns the preferred ordinal of the given member if any. */
  private static Optional<Integer> getMemberTypeOrdinal(Tree tree, VisitorState state) {
    if (isEnumeratorConstant(tree)) {
      return Optional.empty();
    }
    return switch (tree.getKind()) {
      case VARIABLE -> Optional.of(isStatic((VariableTree) tree) ? 1 : 2);
      case BLOCK -> Optional.of(isStatic((BlockTree) tree) ? 3 : 4);
      case METHOD -> Optional.of(isConstructor((MethodTree) tree) ? 5 : 6);
      case CLASS, INTERFACE, ENUM ->
          /*
           * XXX: To enable a Picnic specific preference, we order @Nested test classes as they were
           * methods. This could be replaced with a plugin system where downstream projects could
           * specify their own "canonical member type order".
           */
          ASTHelpers.hasAnnotation(tree, "org.junit.jupiter.api.Nested", state)
              ? Optional.of(6)
              : Optional.of(7);
      default -> Optional.empty();
    };
  }

  /**
   * Returns the start position of the body of the given type, in the case of enums, it returns the
   * position that follows the enumerated type's enumerations.
   */
  private static int getBodyStartPos(ClassTree tree, VisitorState state) {
    CharSequence sourceCode = state.getSourceCode();
    /*
     * To avoid including the type's preceding annotations, use `getPreferredPosition()` rather than
     * `ASTHelpers`.
     */
    int typeStart = ((JCTree.JCClassDecl) tree).getPreferredPosition();
    int typeEnd = state.getEndPosition(tree);
    if (sourceCode == null || typeStart == Position.NOPOS || typeEnd == Position.NOPOS) {
      return Position.NOPOS;
    }

    /*
     * Returns the source code position of the first token that comes after the first curly left
     * bracket.
     */
    return ErrorProneTokens.getTokens(
            sourceCode.subSequence(typeStart, typeEnd).toString(), typeStart, state.context)
        .stream()
        .dropWhile(token -> token.kind() != TokenKind.LBRACE)
        /*
         * To accommodate enums, skip processing their enumerators. This is needed as Error Prone
         * has access to the enumerations individually, but not to the whole expression that
         * declares them, leaving the semicolon trailing the declarations unaccounted for. The
         * current logic would move this trailing semicolon with the first member after the
         * enumerations instead of leaving it to close the enumerations' declaration, introducing a
         * syntax error.
         */
        .dropWhile(token -> tree.getKind() == Kind.ENUM && token.kind() != TokenKind.SEMI)
        .findFirst()
        .map(ErrorProneToken::endPos)
        .orElse(Position.NOPOS);
  }

  /**
   * Returns true if {@link Tree} is an enum or an enumerator definition, false otherwise.
   *
   * @see com.sun.tools.javac.code.Flags#ENUM
   */
  private static boolean isEnumeratorConstant(Tree tree) {
    return tree instanceof JCVariableDecl variableDecl && (variableDecl.mods.flags & ENUM) != 0;
  }

  /**
   * Suggests a different way of ordering the given type members.
   *
   * @implNote For each member, this method tracks the source code between the end of the definition
   *     of the member that precedes it (or the start of the type body if there is no such member)
   *     and the end of the definition of the member itself. This subsequently enables moving
   *     members around, including any preceding comments and Javadoc. This approach isn't perfect,
   *     and may at times move too much code or documentation around; users will have to manually
   *     resolve this.
   */
  private static SuggestedFix sortTypeMembers(
      ImmutableList<TypeMember> members, ImmutableMap<TypeMember, String> sourceCode) {
    return Streams.zip(
            members.stream(),
            members.stream().sorted(),
            (original, replacement) -> {
              String replacementSource = requireNonNull(sourceCode.get(replacement), "replacement");
              return original.equals(replacement)
                  ? SuggestedFix.emptyFix()
                  : SuggestedFix.replace(
                      original.startPosition(), original.endPosition(), replacementSource);
            })
        .reduce(SuggestedFix.builder(), SuggestedFix.Builder::merge, SuggestedFix.Builder::merge)
        .build();
  }

  private static boolean isStatic(VariableTree variableTree) {
    Set<Modifier> modifiers = variableTree.getModifiers().getFlags();
    return modifiers.contains(Modifier.STATIC);
  }

  private static boolean isStatic(BlockTree blockTree) {
    return blockTree.isStatic();
  }

  private static boolean isConstructor(MethodTree methodTree) {
    return ASTHelpers.getSymbol(methodTree).isConstructor();
  }

  record TypeMember(Tree tree, int startPosition, int endPosition, int preferredOrdinal)
      implements Comparable<TypeMember> {
    @Override
    public int compareTo(TypeMemberOrder.TypeMember o) {
      return Integer.compare(preferredOrdinal(), o.preferredOrdinal());
    }
  }
}
