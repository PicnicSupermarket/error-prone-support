package tech.picnic.errorprone.refaster.runner;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Collections.newSetFromMap;
import static java.util.stream.Collectors.toCollection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.refaster.BlockTemplate;
import com.google.errorprone.refaster.ExpressionTemplate;
import com.google.errorprone.refaster.RefasterRule;
import com.google.errorprone.refaster.UAnyOf;
import com.google.errorprone.refaster.UExpression;
import com.google.errorprone.refaster.UStatement;
import com.google.errorprone.refaster.UStaticIdent;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

// XXX: Add some examples of which source files would match what templates in the tree.
// XXX: Consider this text in general.
/**
 * A {@link RefasterRuleSelector} algorithm that selects Refaster templates based on the content of
 * a {@link CompilationUnitTree}.
 *
 * <p>The algorithm consists of the following steps:
 *
 * <ol>
 *   <li>Create a {@link Node tree} structure based on the provided Refaster templates.
 *       <ol>
 *         <li>Extract all identifiers from the {@link BeforeTemplate}s.
 *         <li>Sort identifiers lexicographically and collect into a set.
 *         <li>Add a path to the tree based on the sorted identifiers.
 *       </ol>
 *   <li>Extract all identifiers from the {@link CompilationUnitTree} and sort them
 *       lexicographically.
 *   <li>Traverse the tree based on the identifiers from the {@link CompilationUnitTree}. Every node
 *       can contain Refaster templates. Once a node is we found a candidate Refaster template that
 *       might match some code and will therefore be added to the list of candidates.
 * </ol>
 *
 * <p>This is an example to explain the algorithm. Consider the templates with identifiers; {@code
 * T1 = [A, B, C]}, {@code T2 = [B]}, and {@code T3 = [B, D]}. This will result in the following
 * tree structure:
 *
 * <pre>{@code
 * <root>
 *    ├── A
 *    │   └── B
 *    │       └── C -- T1
 *    └── B         -- T2
 *        └── D     -- T3
 * }</pre>
 *
 * <p>The tree is traversed based on the identifiers in the {@link CompilationUnitTree}. When a node
 * containing a template is reached, we can be certain that the identifiers from the {@link
 * BeforeTemplate} are at least present in the {@link CompilationUnitTree}.
 *
 * <p>Since the identifiers are sorted, we can skip parts of the {@link Node tree} while we are
 * traversing it. Instead of trying to match all Refaster templates against every expression in a
 * {@link CompilationUnitTree} we now only matching a subset of the templates that at least have a
 * chance of matching. As a result, the performance of Refaster increases significantly.
 */
final class RefasterRuleSelector {
  private final Node<RefasterRule<?, ?>> treeRules;

  private RefasterRuleSelector(Node<RefasterRule<?, ?>> treeRules) {
    this.treeRules = treeRules;
  }

  /** Instantiates a new {@link RefasterRuleSelector} backed by the given {@link RefasterRule}s. */
  static RefasterRuleSelector create(ImmutableList<RefasterRule<?, ?>> refasterRules) {
    return new RefasterRuleSelector(
        Node.create(refasterRules, RefasterRuleSelector::extractTemplateIdentifiers));
  }

  /**
   * Retrieve a set of Refaster templates that can possibly match based on a {@link
   * CompilationUnitTree}.
   *
   * @param tree The {@link CompilationUnitTree} for which candidate Refaster templates are
   *     selected.
   * @return Set of Refaster templates that can possibly match in the provided {@link
   *     CompilationUnitTree}.
   */
  Set<RefasterRule<?, ?>> selectCandidateRules(CompilationUnitTree tree) {
    Set<RefasterRule<?, ?>> candidateRules = newSetFromMap(new IdentityHashMap<>());
    treeRules.collectReachableValues(extractSourceIdentifiers(tree), candidateRules::add);
    return candidateRules;
  }

  // XXX: Decompose `RefasterRule`s such that each rule has exactly one `@BeforeTemplate`.
  private static ImmutableSet<ImmutableSet<String>> extractTemplateIdentifiers(
      RefasterRule<?, ?> refasterRule) {
    ImmutableSet.Builder<ImmutableSet<String>> results = ImmutableSet.builder();

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
  private static ImmutableSet<ImmutableSet<String>> extractTemplateIdentifiers(
      ImmutableList<? extends Tree> trees) {
    List<Set<String>> identifierCombinations = new ArrayList<>();
    identifierCombinations.add(new HashSet<>());
    TemplateIdentifierExtractor.INSTANCE.scan(trees, identifierCombinations);
    return identifierCombinations.stream().map(ImmutableSet::copyOf).collect(toImmutableSet());
  }

  private static Set<String> extractSourceIdentifiers(Tree tree) {
    Set<String> identifiers = new HashSet<>();
    SourceIdentifierExtractor.INSTANCE.scan(tree, identifiers);
    return identifiers;
  }

  /**
   * Returns a unique string representation of the given {@link Tree.Kind}.
   *
   * @return A string representation of the operator, if known
   * @throws IllegalArgumentException If the given input is not supported.
   */
  // XXX: Extend list to cover remaining cases; at least for any `Kind` that may appear in a
  // Refaster template. (E.g. keywords such as `if`, `instanceof`, `new`, ...)
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
    // XXX: Probably there is a better way to fix this... For a few BeforeTemplates like
    // `ImmutableMapBuilder` the algorithm wouldn't match so created this fix for now. About 10
    // templates would always match.
    private static final String AUTO_VALUE_UCLASS_IDENT_FQCN =
        "com.google.errorprone.refaster.AutoValue_UClassIdent";
    private static final Class<?> UCLASS_IDENT = getClass(UCLASS_IDENT_FQCN);
    private static final Class<?> UCLASS_AUTOVALUE_IDENT = getClass(AUTO_VALUE_UCLASS_IDENT_FQCN);
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
      return UCLASS_IDENT.equals(tree.getClass()) || UCLASS_AUTOVALUE_IDENT.equals(tree.getClass());
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

    private static Class<?> getClass(String fqcn) {
      try {
        return RefasterIntrospection.class.getClassLoader().loadClass(fqcn);
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException(String.format("Failed to load class `%s`", fqcn), e);
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

    @SuppressWarnings({"TypeParameterUnusedInFormals", "unchecked"})
    private static <T> T invokeMethod(Method method, Object instance) {
      try {
        return (T) method.invoke(instance);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new IllegalStateException(String.format("Failed to invoke method `%s`", method), e);
      }
    }
  }

  private static class TemplateIdentifierExtractor extends TreeScanner<Void, List<Set<String>>> {
    private static final TemplateIdentifierExtractor INSTANCE = new TemplateIdentifierExtractor();

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

    private static String getIdentifier(IdentifierTree tree) {
      return getSimpleName(tree.getName().toString());
    }

    private static String getSimpleName(String fqcn) {
      int index = fqcn.lastIndexOf('.');
      return index < 0 ? fqcn : fqcn.substring(index + 1);
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
    public Void visitMemberSelect(MemberSelectTree node, List<Set<String>> identifierCombinations) {
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

    private static void registerOperator(
        ExpressionTree node, List<Set<String>> identifierCombinations) {
      String id = treeKindToString(node.getKind());
      identifierCombinations.forEach(ids -> ids.add(id));
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

    private static List<Set<String>> copy(List<Set<String>> identifierCombinations) {
      return identifierCombinations.stream()
          .map(HashSet::new)
          .collect(toCollection(ArrayList::new));
    }
  }

  private static class SourceIdentifierExtractor extends TreeScanner<Void, Set<String>> {
    private static final SourceIdentifierExtractor INSTANCE = new SourceIdentifierExtractor();

    @Nullable
    @Override
    public Void visitPackage(PackageTree node, Set<String> identifiers) {
      /* Refaster rules never match package declarations. */
      return null;
    }

    @Nullable
    @Override
    public Void visitClass(ClassTree node, Set<String> identifiers) {
      /*
       * Syntactic details of a class declaration other than the definition of its members do not
       * need to be reflected in a Refaster rule for it to apply to the class's code.
       */
      return scan(node.getMembers(), identifiers);
    }

    @Nullable
    @Override
    public Void visitMethod(MethodTree node, Set<String> identifiers) {
      /*
       * Syntactic details of a method declaration other than its body do not need to be reflected
       * in a Refaster rule for it to apply to the method's code.
       */
      return scan(node.getBody(), identifiers);
    }

    @Nullable
    @Override
    public Void visitVariable(VariableTree node, Set<String> identifiers) {
      /* A variable's modifiers and name do not influence where a Refaster rule matches. */
      return reduce(scan(node.getInitializer(), identifiers), scan(node.getType(), identifiers));
    }

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

    private static void registerOperator(ExpressionTree node, Set<String> identifiers) {
      identifiers.add(treeKindToString(node.getKind()));
    }
  }
}
