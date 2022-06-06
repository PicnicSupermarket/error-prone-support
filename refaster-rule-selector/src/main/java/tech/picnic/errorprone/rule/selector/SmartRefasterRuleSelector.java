package tech.picnic.errorprone.rule.selector;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Collections.newSetFromMap;
import static java.util.stream.Collectors.toCollection;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.errorprone.refaster.BlockTemplate;
import com.google.errorprone.refaster.ExpressionTemplate;
import com.google.errorprone.refaster.RefasterRule;
import com.google.errorprone.refaster.UAnyOf;
import com.google.errorprone.refaster.UClassIdent;
import com.google.errorprone.refaster.UExpression;
import com.google.errorprone.refaster.UStatement;
import com.google.errorprone.refaster.UStaticIdent;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.TreeScanner;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import tech.picnic.errorprone.rule.selector.RefasterRuleSelectorFactory.RefasterRuleSelector;

/** XXX: Write this */
@AutoService(RefasterRuleSelector.class)
public final class SmartRefasterRuleSelector implements RefasterRuleSelector {
  private final Node<RefasterRule<?, ?>> treeRules;

  // XXX: Here pass in the Node? Instead of the list?

  /**
   * XXX: Write this
   *
   * @param refasterRules XXX: Write this
   */
  public SmartRefasterRuleSelector(List<RefasterRule<?, ?>> refasterRules) {
    this.treeRules =
        Node.create(refasterRules, SmartRefasterRuleSelector::extractTemplateIdentifiers);
  }

  @Override
  public Set<RefasterRule<?, ?>> selectCandidateRules(CompilationUnitTree tree) {
    Set<RefasterRule<?, ?>> candidateRules = newSetFromMap(new IdentityHashMap<>());
    treeRules.collectCandidateTemplates(
        extractSourceIdentifiers(tree).asList(), candidateRules::add);

    return candidateRules;
  }

  // XXX: Decompose `RefasterRule`s such that each has exactly one `@BeforeTemplate`.
  private static ImmutableSet<ImmutableSortedSet<String>> extractTemplateIdentifiers(
      RefasterRule<?, ?> refasterRule) {
    ImmutableSet.Builder<ImmutableSortedSet<String>> results = ImmutableSet.builder();

    for (Object template : refasterRule.beforeTemplates()) {
      if (template instanceof ExpressionTemplate) {
        UExpression expr = ((ExpressionTemplate) template).expression();
        results.addAll(extractTemplateIdentifiers(ImmutableList.of(expr)));
      } else if (template instanceof BlockTemplate) {
        ImmutableList<UStatement> statements = ((BlockTemplate) template).templateStatements();
        results.addAll(extractTemplateIdentifiers(statements));
      } else {
        throw new IllegalStateException(
            String.format("Unexpected template type '%s'", template.getClass()));
      }
    }

    return results.build();
  }

  // XXX: Consider interning the strings (once a benchmark is in place).
  private static ImmutableSet<ImmutableSortedSet<String>> extractTemplateIdentifiers(
      ImmutableList<? extends Tree> trees) {
    List<Set<String>> identifierCombinations = new ArrayList<>();
    identifierCombinations.add(new HashSet<>());

    // XXX: Make the scanner static, then make also its helper methods static.
    new TreeScanner<Void, List<Set<String>>>() {
      @Override
      public Void visitIdentifier(IdentifierTree node, List<Set<String>> identifierCombinations) {
        // XXX: Also include the package name if not `java.lang`; it must be present.
        if (node instanceof UClassIdent) {
          for (Set<String> ids : identifierCombinations) {
            ids.add(getSimpleName(((UClassIdent) node).getTopLevelClass()));
            ids.add(getIdentifier(node));
          }
        } else if (node instanceof UStaticIdent) {
          UClassIdent subNode = ((UStaticIdent) node).classIdent();
          for (Set<String> ids : identifierCombinations) {
            ids.add(getSimpleName(subNode.getTopLevelClass()));
            ids.add(getIdentifier(subNode));
            ids.add(node.getName().toString());
          }
        }

        return null;
      }

      private String getIdentifier(IdentifierTree tree) {
        return getSimpleName(tree.getName().toString());
      }

      private String getSimpleName(String fcqn) {
        int index = fcqn.lastIndexOf('.');
        return index < 0 ? fcqn : fcqn.substring(index + 1);
      }

      @Override
      public Void visitMemberReference(
          MemberReferenceTree node, List<Set<String>> identifierCombinations) {
        super.visitMemberReference(node, identifierCombinations);
        String id = node.getName().toString();
        identifierCombinations.forEach(ids -> ids.add(id));
        return null;
      }

      @Override
      public Void visitMemberSelect(
          MemberSelectTree node, List<Set<String>> identifierCombinations) {
        super.visitMemberSelect(node, identifierCombinations);
        String id = node.getIdentifier().toString();
        identifierCombinations.forEach(ids -> ids.add(id));
        return null;
      }

      @Override
      public Void visitAssignment(AssignmentTree node, List<Set<String>> identifierCombinations) {
        registerOperator(node, identifierCombinations);
        return super.visitAssignment(node, identifierCombinations);
      }

      @Override
      public Void visitCompoundAssignment(
          CompoundAssignmentTree node, List<Set<String>> identifierCombinations) {
        registerOperator(node, identifierCombinations);
        return super.visitCompoundAssignment(node, identifierCombinations);
      }

      @Override
      public Void visitUnary(UnaryTree node, List<Set<String>> identifierCombinations) {
        registerOperator(node, identifierCombinations);
        return super.visitUnary(node, identifierCombinations);
      }

      @Override
      public Void visitBinary(BinaryTree node, List<Set<String>> identifierCombinations) {
        registerOperator(node, identifierCombinations);
        return super.visitBinary(node, identifierCombinations);
      }

      // XXX: Rename!
      private void registerOperator(ExpressionTree node, List<Set<String>> identifierCombinations) {
        identifierCombinations.forEach(ids -> ids.add(treeKindToString(node.getKind())));
      }

      @Override
      public Void visitOther(Tree node, List<Set<String>> identifierCombinations) {
        if (node instanceof UAnyOf) {
          List<Set<String>> base = copy(identifierCombinations);
          identifierCombinations.clear();

          for (UExpression expr : ((UAnyOf) node).expressions()) {
            List<Set<String>> branch = copy(base);
            scan(expr, branch);
            identifierCombinations.addAll(branch);
          }
        }

        return null;
      }

      private List<Set<String>> copy(List<Set<String>> identifierCombinations) {
        return identifierCombinations.stream()
            .map(HashSet::new)
            .collect(toCollection(ArrayList::new));
      }
    }.scan(trees, identifierCombinations);

    return identifierCombinations.stream()
        .map(ImmutableSortedSet::copyOf)
        .collect(toImmutableSet());
  }

  // XXX: Consider interning!
  private ImmutableSortedSet<String> extractSourceIdentifiers(Tree tree) {
    Set<String> identifiers = new HashSet<>();

    // XXX: Make the scanner static.
    new TreeScanner<Void, Set<String>>() {
      @Override
      public Void visitIdentifier(IdentifierTree node, Set<String> identifiers) {
        identifiers.add(node.getName().toString());
        return null;
      }

      @Override
      public Void visitMemberReference(MemberReferenceTree node, Set<String> identifiers) {
        super.visitMemberReference(node, identifiers);
        identifiers.add(node.getName().toString());
        return null;
      }

      @Override
      public Void visitMemberSelect(MemberSelectTree node, Set<String> identifiers) {
        super.visitMemberSelect(node, identifiers);
        identifiers.add(node.getIdentifier().toString());
        return null;
      }

      @Override
      public Void visitAssignment(AssignmentTree node, Set<String> identifiers) {
        registerOperator(node, identifiers);
        return super.visitAssignment(node, identifiers);
      }

      @Override
      public Void visitCompoundAssignment(CompoundAssignmentTree node, Set<String> identifiers) {
        registerOperator(node, identifiers);
        return super.visitCompoundAssignment(node, identifiers);
      }

      @Override
      public Void visitUnary(UnaryTree node, Set<String> identifiers) {
        registerOperator(node, identifiers);
        return super.visitUnary(node, identifiers);
      }

      @Override
      public Void visitBinary(BinaryTree node, Set<String> identifiers) {
        registerOperator(node, identifiers);
        return super.visitBinary(node, identifiers);
      }

      // XXX: Rename!
      private void registerOperator(ExpressionTree node, Set<String> identifiers) {
        identifiers.add(treeKindToString(node.getKind()));
      }
    }.scan(tree, identifiers);

    return ImmutableSortedSet.copyOf(identifiers);
  }

  /**
   * Returns a unique string representation of the given {@link Tree.Kind}.
   *
   * @return A string representation of the operator, if known
   * @throws IllegalArgumentException If the given input is not supported.
   */
  // XXX: Extend list to cover remaining cases; at least for any `Kind` that may appear in a
  // Refaster template.
  private static String treeKindToString(Tree.Kind kind) {
    switch (kind) {
      case ASSIGNMENT:
        return "=";
      case POSTFIX_INCREMENT:
        return "x++";
      case PREFIX_INCREMENT:
        return "++x";
      case POSTFIX_DECREMENT:
        return "x--";
      case PREFIX_DECREMENT:
        return "--x";
      case UNARY_PLUS:
        return "+x";
      case UNARY_MINUS:
        return "-x";
      case BITWISE_COMPLEMENT:
        return "~";
      case LOGICAL_COMPLEMENT:
        return "!";
      case MULTIPLY:
        return "*";
      case DIVIDE:
        return "/";
      case REMAINDER:
        return "%";
      case PLUS:
        return "+";
      case MINUS:
        return "-";
      case LEFT_SHIFT:
        return "<<";
      case RIGHT_SHIFT:
        return ">>";
      case UNSIGNED_RIGHT_SHIFT:
        return ">>>";
      case LESS_THAN:
        return "<";
      case GREATER_THAN:
        return ">";
      case LESS_THAN_EQUAL:
        return "<=";
      case GREATER_THAN_EQUAL:
        return ">=";
      case EQUAL_TO:
        return "==";
      case NOT_EQUAL_TO:
        return "!=";
      case AND:
        return "&";
      case XOR:
        return "^";
      case OR:
        return "|";
      case CONDITIONAL_AND:
        return "&&";
      case CONDITIONAL_OR:
        return "||";
      case MULTIPLY_ASSIGNMENT:
        return "*=";
      case DIVIDE_ASSIGNMENT:
        return "/=";
      case REMAINDER_ASSIGNMENT:
        return "%=";
      case PLUS_ASSIGNMENT:
        return "+=";
      case MINUS_ASSIGNMENT:
        return "-=";
      case LEFT_SHIFT_ASSIGNMENT:
        return "<<=";
      case RIGHT_SHIFT_ASSIGNMENT:
        return ">>=";
      case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
        return ">>>=";
      case AND_ASSIGNMENT:
        return "&=";
      case XOR_ASSIGNMENT:
        return "^=";
      case OR_ASSIGNMENT:
        return "|=";
      default:
        throw new IllegalStateException("Cannot convert Tree.Kind to a String: " + kind);
    }
  }
}
