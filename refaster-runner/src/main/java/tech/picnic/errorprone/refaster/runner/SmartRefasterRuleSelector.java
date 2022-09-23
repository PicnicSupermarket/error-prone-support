package tech.picnic.errorprone.refaster.runner;

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/** XXX: Write this */
@AutoService(RefasterRuleSelector.class)
public final class SmartRefasterRuleSelector implements RefasterRuleSelector {
  private final Node<RefasterRule<?, ?>> treeRules;

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

    for (Object template : RefasterIntrospection.getBeforeTemplates(refasterRule)) {
      if (template instanceof ExpressionTemplate) {
        UExpression expr = RefasterIntrospection.getExpression((ExpressionTemplate) template);
        results.addAll(extractTemplateIdentifiers(ImmutableList.of(expr)));
      } else if (template instanceof BlockTemplate) {
        ImmutableList<UStatement> statements =
            RefasterIntrospection.getTemplateStatements((BlockTemplate) template);
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
      @Nullable
      @Override
      public Void visitIdentifier(IdentifierTree node, List<Set<String>> identifierCombinations) {
        // XXX: Also include the package name if not `java.lang`; it must be present.
        if (RefasterIntrospection.isUClassIdent(node)) {
          for (Set<String> ids : identifierCombinations) {
            ids.add(getSimpleName(RefasterIntrospection.getTopLevelClass(node)));
            ids.add(getIdentifier(node));
          }
        } else if (node instanceof UStaticIdent) {
          IdentifierTree subNode = RefasterIntrospection.getClassIdent((UStaticIdent) node);
          for (Set<String> ids : identifierCombinations) {
            ids.add(getSimpleName(RefasterIntrospection.getTopLevelClass(subNode)));
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

      @Nullable
      @Override
      public Void visitMemberReference(
          MemberReferenceTree node, List<Set<String>> identifierCombinations) {
        super.visitMemberReference(node, identifierCombinations);
        String id = node.getName().toString();
        identifierCombinations.forEach(ids -> ids.add(id));
        return null;
      }

      @Nullable
      @Override
      public Void visitMemberSelect(
          MemberSelectTree node, List<Set<String>> identifierCombinations) {
        super.visitMemberSelect(node, identifierCombinations);
        String id = node.getIdentifier().toString();
        identifierCombinations.forEach(ids -> ids.add(id));
        return null;
      }

      @Nullable
      @Override
      public Void visitAssignment(AssignmentTree node, List<Set<String>> identifierCombinations) {
        registerOperator(node, identifierCombinations);
        return super.visitAssignment(node, identifierCombinations);
      }

      @Nullable
      @Override
      public Void visitCompoundAssignment(
          CompoundAssignmentTree node, List<Set<String>> identifierCombinations) {
        registerOperator(node, identifierCombinations);
        return super.visitCompoundAssignment(node, identifierCombinations);
      }

      @Nullable
      @Override
      public Void visitUnary(UnaryTree node, List<Set<String>> identifierCombinations) {
        registerOperator(node, identifierCombinations);
        return super.visitUnary(node, identifierCombinations);
      }

      @Nullable
      @Override
      public Void visitBinary(BinaryTree node, List<Set<String>> identifierCombinations) {
        registerOperator(node, identifierCombinations);
        return super.visitBinary(node, identifierCombinations);
      }

      // XXX: Rename!
      private void registerOperator(ExpressionTree node, List<Set<String>> identifierCombinations) {
        identifierCombinations.forEach(ids -> ids.add(treeKindToString(node.getKind())));
      }

      @Nullable
      @Override
      public Void visitOther(Tree node, List<Set<String>> identifierCombinations) {
        if (node instanceof UAnyOf) {
          List<Set<String>> base = copy(identifierCombinations);
          identifierCombinations.clear();

          for (UExpression expr : RefasterIntrospection.getExpressions((UAnyOf) node)) {
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
      @Nullable
      @Override
      public Void visitIdentifier(IdentifierTree node, Set<String> identifiers) {
        identifiers.add(node.getName().toString());
        return null;
      }

      @Nullable
      @Override
      public Void visitMemberReference(MemberReferenceTree node, Set<String> identifiers) {
        super.visitMemberReference(node, identifiers);
        identifiers.add(node.getName().toString());
        return null;
      }

      @Nullable
      @Override
      public Void visitMemberSelect(MemberSelectTree node, Set<String> identifiers) {
        super.visitMemberSelect(node, identifiers);
        identifiers.add(node.getIdentifier().toString());
        return null;
      }

      @Nullable
      @Override
      public Void visitAssignment(AssignmentTree node, Set<String> identifiers) {
        registerOperator(node, identifiers);
        return super.visitAssignment(node, identifiers);
      }

      @Nullable
      @Override
      public Void visitCompoundAssignment(CompoundAssignmentTree node, Set<String> identifiers) {
        registerOperator(node, identifiers);
        return super.visitCompoundAssignment(node, identifiers);
      }

      @Nullable
      @Override
      public Void visitUnary(UnaryTree node, Set<String> identifiers) {
        registerOperator(node, identifiers);
        return super.visitUnary(node, identifiers);
      }

      @Nullable
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

  private static final class RefasterIntrospection {
    private static final String UCLASS_IDENT_FQCN = "com.google.errorprone.refaster.UClassIdent";
    private static final String AUTO_VALUE_UCLASS_IDENT_FQCN = "com.google.errorprone.refaster.AutoValue_UClassIdent";
    private static final Class<?> UCLASS_IDENT = getUClassIdentClass();
    private static final Method METHOD_REFASTER_RULE_BEFORE_TEMPLATES =
        getMethod(RefasterRule.class, "beforeTemplates");
    private static final Method METHOD_EXPRESSION_TEMPLATE_EXPRESSION =
        getMethod(ExpressionTemplate.class, "expression");
    private static final Method METHOD_BLOCK_TEMPLATE_TEMPLATE_STATEMENTS =
        getMethod(BlockTemplate.class, "templateStatements");
    private static final Method METHOD_USTATIC_IDENT_CLASS_IDENT =
        getMethod(UStaticIdent.class, "classIdent");
    private static final Method METHOD_UCLASS_IDENT_GET_TOP_LEVEL_CLASS =
        getMethod(UCLASS_IDENT, "getTopLevelClass");
    private static final Method METHOD_UANY_OF_EXPRESSIONS = getMethod(UAnyOf.class, "expressions");

    static boolean isUClassIdent(IdentifierTree tree) {
      return UCLASS_IDENT.equals(tree.getClass());
    }

    static ImmutableList<?> getBeforeTemplates(RefasterRule<?, ?> refasterRule) {
      return invokeMethod(METHOD_REFASTER_RULE_BEFORE_TEMPLATES, refasterRule);
    }

    static UExpression getExpression(ExpressionTemplate template) {
      return invokeMethod(METHOD_EXPRESSION_TEMPLATE_EXPRESSION, template);
    }

    static ImmutableList<UStatement> getTemplateStatements(BlockTemplate template) {
      return invokeMethod(METHOD_BLOCK_TEMPLATE_TEMPLATE_STATEMENTS, template);
    }

    // Actually UClassIdent.
    static IdentifierTree getClassIdent(UStaticIdent tree) {
      return invokeMethod(METHOD_USTATIC_IDENT_CLASS_IDENT, tree);
    }

    // XXX: Make nicer. Or rename the other params.
    static String getTopLevelClass(IdentifierTree uClassIdent) {
      return invokeMethod(METHOD_UCLASS_IDENT_GET_TOP_LEVEL_CLASS, uClassIdent);
    }

    static ImmutableList<UExpression> getExpressions(UAnyOf tree) {
      return invokeMethod(METHOD_UANY_OF_EXPRESSIONS, tree);
    }

    @SuppressWarnings({"TypeParameterUnusedInFormals", "unchecked"})
    private static <T> T invokeMethod(Method method, Object instance) {
      try {
        return (T) method.invoke(instance);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new IllegalStateException(String.format("Failed to invoke method `%s`", method), e);
      }
    }

    private static Method getMethod(Class<?> clazz, String methodName) {
      try {
        Method method = clazz.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method;
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException(
            String.format("No method `%s` on class `%s`", methodName, clazz.getName()), e);
      }
    }

    private static Class<?> getUClassIdentClass() {
      try {
        return RefasterIntrospection.class.getClassLoader().loadClass(UCLASS_IDENT_FQCN);
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException(
            String.format("Failed to load class `%s`", UCLASS_IDENT_FQCN), e);
      }
    }
  }
}
