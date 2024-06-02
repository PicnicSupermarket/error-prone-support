package tech.picnic.errorprone.experimental.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.sun.tools.javac.code.Flags.ENUM;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneToken;
import com.google.errorprone.util.ErrorProneTokens;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.parser.Tokens.TokenKind;
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
 *   <li>Methods
 *   <li>Nested classes, interfaces and enums
 * </ol>
 *
 * @see <a
 *     href="https://checkstyle.sourceforge.io/apidocs/com/puppycrawl/tools/checkstyle/checks/coding/DeclarationOrderCheck.html">Checkstyle's
 *     {@code DeclarationOrderCheck}</a>
 */
// XXX: Consider introducing support for ordering members in records or annotation definitions.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Type members should be defined in a canonical order",
    link = BUG_PATTERNS_BASE_URL + "TypeMemberOrder",
    linkType = CUSTOM,
    severity = WARNING,
    tags = STYLE)
public final class TypeMemberOrder extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link TypeMemberOrder} instance. */
  public TypeMemberOrder() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    Kind treeKind = tree.getKind();
    if (treeKind != Kind.CLASS && treeKind != Kind.INTERFACE && treeKind != Kind.ENUM) {
      return Description.NO_MATCH;
    }

    int bodyStartPos = getBodyStartPos(tree, state);
    if (bodyStartPos == Position.NOPOS) {
      /*
       * We can't determine the type body's start position in the source code. This generally means
       * that (part of) its code was generated. Even if the source code for a subset of its members
       * is available, dealing with this edge case is not worth the trouble.
       */
      return Description.NO_MATCH;
    }

    ImmutableList<TypeMember> members = getAllTypeMembers(tree, bodyStartPos, state);
    ImmutableList<TypeMember> sorted = ImmutableList.sortedCopyOf(members);

    if (members.equals(sorted)) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree, sortTypeMembers(members, state));
  }

  /** Returns all members that can be moved or may lay between movable ones. */
  private ImmutableList<TypeMember> getAllTypeMembers(
      ClassTree tree, int bodyStartPos, VisitorState state) {
    ImmutableList.Builder<TypeMember> builder = ImmutableList.builder();
    @Var int currentStartPos = bodyStartPos;
    for (Tree member : tree.getMembers()) {
      if (state.getEndPosition(member) == Position.NOPOS) {
        continue;
      }

      int treeStartPos = currentStartPos;
      getMemberTypeOrdinal(member, state)
          .ifPresent(
              e ->
                  builder.add(
                      new AutoValue_TypeMemberOrder_TypeMember(
                          member, treeStartPos, state.getEndPosition(member), e)));

      currentStartPos = state.getEndPosition(member);
    }
    return builder.build();
  }

  /**
   * Returns the preferred ordinal of the given member, or empty if it's unmovable for any reason,
   * including it lacking a preferred ordinal.
   */
  private Optional<Integer> getMemberTypeOrdinal(Tree tree, VisitorState state) {
    if (isSuppressed(tree, state) || isEnumDefinition(tree)) {
      return Optional.empty();
    }
    return switch (tree.getKind()) {
      case VARIABLE -> Optional.of(isStatic((VariableTree) tree) ? 1 : 2);
      case BLOCK -> Optional.of(isStatic((BlockTree) tree) ? 3 : 4);
      case METHOD -> Optional.of(isConstructor((MethodTree) tree) ? 5 : 6);
      case CLASS, INTERFACE, ENUM -> Optional.of(7);
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
   * @see com.sun.tools.javac.tree.Pretty#isEnumerator(JCTree)
   * @see com.sun.tools.javac.code.Flags#ENUM
   */
  private static boolean isEnumDefinition(Tree tree) {
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
      ImmutableList<TypeMember> members, VisitorState state) {
    CharSequence sourceCode = requireNonNull(state.getSourceCode(), "Source code");
    return Streams.zip(
            members.stream(),
            members.stream().sorted(),
            (original, replacement) -> original.replaceWith(replacement, sourceCode))
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

  //  /** Type members that have a sourcecode and are not generated, are considered to be movable.
  // */
  /** XXX: Write this. Every member that is in a ClassTree? */
  @AutoValue
  abstract static class TypeMember implements Comparable<TypeMember> {
    abstract Tree tree();

    abstract int startPosition();

    abstract int endPosition();

    abstract Integer preferredOrdinal();

    @Override
    public int compareTo(TypeMemberOrder.TypeMember o) {
      //      if (preferredOrdinal().isEmpty() || o.preferredOrdinal().isEmpty()) {
      //        return 0;
      //      }
      return preferredOrdinal().compareTo(o.preferredOrdinal());
    }

    SuggestedFix replaceWith(TypeMember other, CharSequence fullSourceCode) {
      return equals(other)
          ? SuggestedFix.emptyFix()
          : SuggestedFix.replace(
              startPosition(),
              endPosition(),
              fullSourceCode.subSequence(other.startPosition(), other.endPosition()).toString());
    }
  }
}
