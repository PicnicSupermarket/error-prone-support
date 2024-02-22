package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Verify.verify;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.sun.tools.javac.code.Flags.ENUM;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneToken;
import com.google.errorprone.util.ErrorProneTokens;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.parser.Tokens.TokenKind;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Position;
import java.util.ArrayList;
import java.util.List;
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
// XXX: Reference
// https://checkstyle.sourceforge.io/apidocs/com/puppycrawl/tools/checkstyle/checks/coding/DeclarationOrderCheck.html
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Type members should be ordered in a standard way",
    link = BUG_PATTERNS_BASE_URL + "TypeMemberOrder",
    linkType = CUSTOM,
    severity = WARNING,
    tags = STYLE)
public final class TypeMemberOrder extends BugChecker implements BugChecker.ClassTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link TypeMemberOrder} instance. */
  public TypeMemberOrder() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (tree.getKind() != Tree.Kind.CLASS
        && tree.getKind() != Tree.Kind.INTERFACE
        && tree.getKind() != Tree.Kind.ENUM) {
      return Description.NO_MATCH;
    }

    // Keep track of all members, including unmovable ones, to exclude them from the sources
    // present in-between members.
    ImmutableList<TypeMember> members =
        tree.getMembers().stream()
            .filter(TypeMemberOrder::hasSource)
            .map(m -> new AutoValue_TypeMemberOrder_TypeMember(m, getPreferredOrdinal(m, state)))
            .collect(toImmutableList());

    // List of the sortable members' preferred ordinals,
    // ordered by the member's position in original source.
    ImmutableList<Integer> preferredOrdinals =
        members.stream()
            .filter(m -> m.preferredOrdinal().isPresent())
            .map(m -> m.preferredOrdinal().orElseThrow(/* Unreachable due to preceding check. */ ))
            .collect(toImmutableList());

    if (Comparators.isInOrder(preferredOrdinals, naturalOrder())) {
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

    return describeMatch(tree, sortTypeMembers(bodyStartPos, members, state));
  }

  /**
   * Returns the preferred ordinal of the given member, or empty if it's unmovable for any reason,
   * including it lacking a preferred ordinal.
   */
  private Optional<Integer> getPreferredOrdinal(Tree tree, VisitorState state) {
    if (!canMove(tree, state)) {
      return Optional.empty();
    }
    return switch (tree.getKind()) {
      case VARIABLE -> Optional.of(isStatic((VariableTree) tree) ? 1 : 2);
      case BLOCK -> Optional.of(isStatic((BlockTree) tree) ? 3 : 4);
      case METHOD -> Optional.of(isConstructor((MethodTree) tree) ? 5 : 6);
      case CLASS, INTERFACE, ENUM -> Optional.of(7);
      default ->
      // TODO: Should we log unhandled kinds?
      Optional.empty();
    };
  }

  private boolean canMove(Tree tree, VisitorState state) {
    return hasSource(tree) && !isSuppressed(tree, state) && !isEnumerator(tree);
  }

  private static boolean hasSource(Tree tree) {
    if (tree.getKind() == Tree.Kind.METHOD) {
      return !ASTHelpers.isGeneratedConstructor(((MethodTree) tree));
    }
    return true;
  }

  /**
   * Returns true if Tree is an enumerator of an enumerated type, or false otherwise.
   *
   * @see com.sun.tools.javac.tree.Pretty#isEnumerator(JCTree)
   * @see com.sun.tools.javac.code.Flags#ENUM
   */
  private static boolean isEnumerator(Tree tree) {
    return tree instanceof JCTree.JCVariableDecl
        && (((JCTree.JCVariableDecl) tree).mods.flags & ENUM) != 0;
  }

  /**
   * Returns the start position of the body of the given type, in the case of enums, it returns the
   * position that follows the enumerated type's enumerations.
   */
  private static int getBodyStartPos(ClassTree tree, VisitorState state) {
    CharSequence sourceCode = state.getSourceCode();
    int typeStart = ASTHelpers.getStartPosition(tree);
    int typeEnd = state.getEndPosition(tree);
    if (sourceCode == null || typeStart == Position.NOPOS || typeEnd == Position.NOPOS) {
      return Position.NOPOS;
    }

    // We return the source code position of the first token that follows the first left brace.
    return ErrorProneTokens.getTokens(
            sourceCode.subSequence(typeStart, typeEnd).toString(), typeStart, state.context)
        .stream()
        .dropWhile(token -> token.kind() != TokenKind.LBRACE)
        // XXX: To accommodate enums, start processing the body, after the enumerated type's
        // enumerators.
        .dropWhile(token -> tree.getKind() == Tree.Kind.ENUM && token.kind() != TokenKind.SEMI)
        .findFirst()
        .map(ErrorProneToken::endPos)
        .orElse(Position.NOPOS);
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
      int bodyStartPos, ImmutableList<TypeMember> members, VisitorState state) {
    List<MovableTypeMember> membersWithSource = new ArrayList<>();

    @Var int start = bodyStartPos;
    for (TypeMember member : members) {
      int end = state.getEndPosition(member.tree());
      if (isEnumerator(member.tree())) {
        // XXX: To accommodate enums, skip enumerators of enumerated types.
        continue;
      }
      verify(
          end != Position.NOPOS && start < end,
          "Unexpected member end position, member: %s",
          member);
      if (member.preferredOrdinal().isPresent()) {
        membersWithSource.add(
            new AutoValue_TypeMemberOrder_MovableTypeMember(
                member.tree(),
                start,
                end,
                member.preferredOrdinal().orElseThrow(/* Unreachable due to preceding check. */)));
      }
      start = end;
    }

    CharSequence sourceCode = requireNonNull(state.getSourceCode(), "Source code");
    return Streams.zip(
            membersWithSource.stream(),
            membersWithSource.stream()
                .sorted(comparing(MovableTypeMember::preferredOrdinal, naturalOrder())),
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

  @AutoValue
  abstract static class TypeMember {
    abstract Tree tree();

    abstract Optional<Integer> preferredOrdinal();
  }

  @AutoValue
  abstract static class MovableTypeMember {
    abstract Tree tree();

    abstract int startPosition();

    abstract int endPosition();

    abstract int preferredOrdinal();

    SuggestedFix replaceWith(MovableTypeMember other, CharSequence fullSourceCode) {
      return equals(other)
          ? SuggestedFix.emptyFix()
          : SuggestedFix.replace(
              startPosition(),
              endPosition(),
              fullSourceCode.subSequence(other.startPosition(), other.endPosition()).toString());
    }
  }
}
