package tech.picnic.errorprone.refaster.runner;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Collections.newSetFromMap;
import static java.util.stream.Collectors.toCollection;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.CompositeCodeTransformer;
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
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.refaster.AnnotatedCompositeCodeTransformer;

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
  private final Node<CodeTransformer> codeTransformers;

  private RefasterRuleSelector(Node<CodeTransformer> codeTransformers) {
    this.codeTransformers = codeTransformers;
  }

  /**
   * Instantiates a new {@link RefasterRuleSelector} backed by the given {@link CodeTransformer}s.
   */
  static RefasterRuleSelector create(ImmutableCollection<CodeTransformer> refasterRules) {
    Map<CodeTransformer, ImmutableSet<ImmutableSet<String>>> ruleIdentifiersByTransformer =
        indexRuleIdentifiers(refasterRules);
    return new RefasterRuleSelector(
        Node.create(ruleIdentifiersByTransformer.keySet(), ruleIdentifiersByTransformer::get));
  }

  /**
   * Retrieves a set of Refaster templates that can possibly match based on a {@link
   * CompilationUnitTree}.
   *
   * @param tree The {@link CompilationUnitTree} for which candidate Refaster templates are
   *     selected.
   * @return Set of Refaster templates that can possibly match in the provided {@link
   *     CompilationUnitTree}.
   */
  Set<CodeTransformer> selectCandidateRules(CompilationUnitTree tree) {
    Set<CodeTransformer> candidateRules = newSetFromMap(new IdentityHashMap<>());
    codeTransformers.collectReachableValues(extractSourceIdentifiers(tree), candidateRules::add);
    return candidateRules;
  }

  private static Map<CodeTransformer, ImmutableSet<ImmutableSet<String>>> indexRuleIdentifiers(
      ImmutableCollection<CodeTransformer> codeTransformers) {
    IdentityHashMap<CodeTransformer, ImmutableSet<ImmutableSet<String>>> identifiers =
        new IdentityHashMap<>();
    for (CodeTransformer transformer : codeTransformers) {
      collectRuleIdentifiers(transformer, identifiers);
    }
    return identifiers;
  }

  private static void collectRuleIdentifiers(
      CodeTransformer codeTransformer,
      Map<CodeTransformer, ImmutableSet<ImmutableSet<String>>> identifiers) {
    if (codeTransformer instanceof CompositeCodeTransformer compositeCodeTransformer) {
      for (CodeTransformer transformer : compositeCodeTransformer.transformers()) {
        collectRuleIdentifiers(transformer, identifiers);
      }
    } else if (codeTransformer instanceof AnnotatedCompositeCodeTransformer annotatedTransformer) {
      for (Map.Entry<CodeTransformer, ImmutableSet<ImmutableSet<String>>> e :
          indexRuleIdentifiers(annotatedTransformer.transformers()).entrySet()) {
        identifiers.put(annotatedTransformer.withTransformers(e.getKey()), e.getValue());
      }
    } else if (codeTransformer instanceof RefasterRule) {
      identifiers.put(
          codeTransformer, extractRuleIdentifiers((RefasterRule<?, ?>) codeTransformer));
    } else {
      /* Unrecognized `CodeTransformer` types are indexed such that they always apply. */
      identifiers.put(codeTransformer, ImmutableSet.of(ImmutableSet.of()));
    }
  }

  // XXX: Consider decomposing `RefasterRule`s such that each rule has exactly one
  // `@BeforeTemplate`.
  private static ImmutableSet<ImmutableSet<String>> extractRuleIdentifiers(
      RefasterRule<?, ?> refasterRule) {
    ImmutableSet.Builder<ImmutableSet<String>> results = ImmutableSet.builder();

    for (Object template : RefasterIntrospection.getBeforeTemplates(refasterRule)) {
      if (template instanceof ExpressionTemplate expressionTemplate) {
        UExpression expr = RefasterIntrospection.getExpression(expressionTemplate);
        results.addAll(extractRuleIdentifiers(ImmutableList.of(expr)));
      } else if (template instanceof BlockTemplate blockTemplate) {
        ImmutableList<UStatement> statements =
            RefasterIntrospection.getTemplateStatements(blockTemplate);
        results.addAll(extractRuleIdentifiers(statements));
      } else {
        throw new IllegalStateException(
            String.format("Unexpected template type '%s'", template.getClass()));
      }
    }

    return results.build();
  }

  // XXX: Consider interning the strings (once a benchmark is in place).
  private static ImmutableSet<ImmutableSet<String>> extractRuleIdentifiers(
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
    return switch (kind) {
      case ASSIGNMENT -> "=";
      case POSTFIX_INCREMENT -> "x++";
      case PREFIX_INCREMENT -> "++x";
      case POSTFIX_DECREMENT -> "x--";
      case PREFIX_DECREMENT -> "--x";
      case UNARY_PLUS -> "+x";
      case UNARY_MINUS -> "-x";
      case BITWISE_COMPLEMENT -> "~";
      case LOGICAL_COMPLEMENT -> "!";
      case MULTIPLY -> "*";
      case DIVIDE -> "/";
      case REMAINDER -> "%";
      case PLUS -> "+";
      case MINUS -> "-";
      case LEFT_SHIFT -> "<<";
      case RIGHT_SHIFT -> ">>";
      case UNSIGNED_RIGHT_SHIFT -> ">>>";
      case LESS_THAN -> "<";
      case GREATER_THAN -> ">";
      case LESS_THAN_EQUAL -> "<=";
      case GREATER_THAN_EQUAL -> ">=";
      case EQUAL_TO -> "==";
      case NOT_EQUAL_TO -> "!=";
      case AND -> "&";
      case XOR -> "^";
      case OR -> "|";
      case CONDITIONAL_AND -> "&&";
      case CONDITIONAL_OR -> "||";
      case MULTIPLY_ASSIGNMENT -> "*=";
      case DIVIDE_ASSIGNMENT -> "/=";
      case REMAINDER_ASSIGNMENT -> "%=";
      case PLUS_ASSIGNMENT -> "+=";
      case MINUS_ASSIGNMENT -> "-=";
      case LEFT_SHIFT_ASSIGNMENT -> "<<=";
      case RIGHT_SHIFT_ASSIGNMENT -> ">>=";
      case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT -> ">>>=";
      case AND_ASSIGNMENT -> "&=";
      case XOR_ASSIGNMENT -> "^=";
      case OR_ASSIGNMENT -> "|=";
      default -> throw new IllegalStateException("Cannot convert Tree.Kind to a String: " + kind);
    };
  }

  private static final class RefasterIntrospection {
    // XXX: Update `ErrorProneRuntimeClasspath` to not suggest inaccessible types.
    @SuppressWarnings("ErrorProneRuntimeClasspath")
    private static final String UCLASS_IDENT_FQCN = "com.google.errorprone.refaster.UClassIdent";

    private static final Class<?> UCLASS_IDENT = getClass(UCLASS_IDENT_FQCN);
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
      return UCLASS_IDENT.isInstance(tree);
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

    static IdentifierTree getClassIdent(UStaticIdent tree) {
      return invokeMethod(METHOD_USTATIC_IDENT_CLASS_IDENT, tree);
    }

    // Arguments to this method must actually be of the package-private type `UClassIdent`.
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

  private static final class TemplateIdentifierExtractor
      extends TreeScanner<@Nullable Void, List<Set<String>>> {
    private static final TemplateIdentifierExtractor INSTANCE = new TemplateIdentifierExtractor();

    @Override
    public @Nullable Void visitIdentifier(
        IdentifierTree node, List<Set<String>> identifierCombinations) {
      // XXX: Also include the package name if not `java.lang`; it must be present.
      if (RefasterIntrospection.isUClassIdent(node)) {
        for (Set<String> ids : identifierCombinations) {
          ids.add(getSimpleName(RefasterIntrospection.getTopLevelClass(node)));
          ids.add(getIdentifier(node));
        }
      } else if (node instanceof UStaticIdent uStaticIdent) {
        IdentifierTree subNode = RefasterIntrospection.getClassIdent(uStaticIdent);
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

    @Override
    public @Nullable Void visitMemberReference(
        MemberReferenceTree node, List<Set<String>> identifierCombinations) {
      super.visitMemberReference(node, identifierCombinations);
      String id = node.getName().toString();
      identifierCombinations.forEach(ids -> ids.add(id));
      return null;
    }

    @Override
    public @Nullable Void visitMemberSelect(
        MemberSelectTree node, List<Set<String>> identifierCombinations) {
      super.visitMemberSelect(node, identifierCombinations);
      String id = node.getIdentifier().toString();
      identifierCombinations.forEach(ids -> ids.add(id));
      return null;
    }

    @Override
    public @Nullable Void visitAssignment(
        AssignmentTree node, List<Set<String>> identifierCombinations) {
      registerOperator(node, identifierCombinations);
      return super.visitAssignment(node, identifierCombinations);
    }

    @Override
    public @Nullable Void visitCompoundAssignment(
        CompoundAssignmentTree node, List<Set<String>> identifierCombinations) {
      registerOperator(node, identifierCombinations);
      return super.visitCompoundAssignment(node, identifierCombinations);
    }

    @Override
    public @Nullable Void visitUnary(UnaryTree node, List<Set<String>> identifierCombinations) {
      registerOperator(node, identifierCombinations);
      return super.visitUnary(node, identifierCombinations);
    }

    @Override
    public @Nullable Void visitBinary(BinaryTree node, List<Set<String>> identifierCombinations) {
      registerOperator(node, identifierCombinations);
      return super.visitBinary(node, identifierCombinations);
    }

    private static void registerOperator(
        ExpressionTree node, List<Set<String>> identifierCombinations) {
      String id = treeKindToString(node.getKind());
      identifierCombinations.forEach(ids -> ids.add(id));
    }

    @Override
    public @Nullable Void visitOther(Tree node, List<Set<String>> identifierCombinations) {
      if (node instanceof UAnyOf uAnyOf) {
        List<Set<String>> base = copy(identifierCombinations);
        identifierCombinations.clear();

        for (UExpression expr : RefasterIntrospection.getExpressions(uAnyOf)) {
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

  private static final class SourceIdentifierExtractor
      extends TreeScanner<@Nullable Void, Set<String>> {
    private static final SourceIdentifierExtractor INSTANCE = new SourceIdentifierExtractor();

    @Override
    public @Nullable Void visitPackage(PackageTree node, Set<String> identifiers) {
      /* Refaster rules never match package declarations. */
      return null;
    }

    @Override
    public @Nullable Void visitClass(ClassTree node, Set<String> identifiers) {
      /*
       * Syntactic details of a class declaration other than the definition of its members do not
       * need to be reflected in a Refaster rule for it to apply to the class's code.
       */
      return scan(node.getMembers(), identifiers);
    }

    @Override
    public @Nullable Void visitMethod(MethodTree node, Set<String> identifiers) {
      /*
       * Syntactic details of a method declaration other than its body do not need to be reflected
       * in a Refaster rule for it to apply to the method's code.
       */
      return scan(node.getBody(), identifiers);
    }

    @Override
    public @Nullable Void visitVariable(VariableTree node, Set<String> identifiers) {
      /* A variable's modifiers and name do not influence where a Refaster rule matches. */
      return reduce(scan(node.getInitializer(), identifiers), scan(node.getType(), identifiers));
    }

    @Override
    public @Nullable Void visitIdentifier(IdentifierTree node, Set<String> identifiers) {
      identifiers.add(node.getName().toString());
      return null;
    }

    @Override
    public @Nullable Void visitMemberReference(MemberReferenceTree node, Set<String> identifiers) {
      super.visitMemberReference(node, identifiers);
      identifiers.add(node.getName().toString());
      return null;
    }

    @Override
    public @Nullable Void visitMemberSelect(MemberSelectTree node, Set<String> identifiers) {
      super.visitMemberSelect(node, identifiers);
      identifiers.add(node.getIdentifier().toString());
      return null;
    }

    @Override
    public @Nullable Void visitAssignment(AssignmentTree node, Set<String> identifiers) {
      registerOperator(node, identifiers);
      return super.visitAssignment(node, identifiers);
    }

    @Override
    public @Nullable Void visitCompoundAssignment(
        CompoundAssignmentTree node, Set<String> identifiers) {
      registerOperator(node, identifiers);
      return super.visitCompoundAssignment(node, identifiers);
    }

    @Override
    public @Nullable Void visitUnary(UnaryTree node, Set<String> identifiers) {
      registerOperator(node, identifiers);
      return super.visitUnary(node, identifiers);
    }

    @Override
    public @Nullable Void visitBinary(BinaryTree node, Set<String> identifiers) {
      registerOperator(node, identifiers);
      return super.visitBinary(node, identifiers);
    }

    private static void registerOperator(ExpressionTree node, Set<String> identifiers) {
      identifiers.add(treeKindToString(node.getKind()));
    }
  }
}
