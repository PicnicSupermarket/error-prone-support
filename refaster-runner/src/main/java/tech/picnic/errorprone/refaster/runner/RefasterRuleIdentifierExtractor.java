package tech.picnic.errorprone.refaster.runner;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
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
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
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
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/** Extracts identifiers from RefasterRule templates. */
final class RefasterRuleIdentifierExtractor {
  private RefasterRuleIdentifierExtractor() {}

  /**
   * Extracts identifiers from a RefasterRule's before templates.
   *
   * @param refasterRule The RefasterRule to extract identifiers from
   * @return A set of identifier sets, one for each possible identifier combination in the rule
   */
  static ImmutableSet<ImmutableSet<String>> extractIdentifiers(RefasterRule<?, ?> refasterRule) {
    ImmutableSet.Builder<ImmutableSet<String>> results = ImmutableSet.builder();

    for (Object template : RefasterIntrospection.getBeforeTemplates(refasterRule)) {
      switch (template) {
        case ExpressionTemplate expressionTemplate -> {
          UExpression expr = RefasterIntrospection.getExpression(expressionTemplate);
          results.addAll(extractIdentifiers(ImmutableList.of(expr)));
        }
        case BlockTemplate blockTemplate -> {
          ImmutableList<UStatement> statements =
              RefasterIntrospection.getTemplateStatements(blockTemplate);
          results.addAll(extractIdentifiers(statements));
        }
        default ->
            throw new IllegalStateException(
                "Unexpected template type '%s'".formatted(template.getClass()));
      }
    }

    return results.build();
  }

  /**
   * Extracts identifiers from a list of trees.
   *
   * <p>This method is package-private to allow direct testing with simple Tree instances.
   *
   * @param trees The trees to extract identifiers from
   * @return A set of identifier sets
   */
  // XXX: Consider interning the strings (once a benchmark is in place).
  static ImmutableSet<ImmutableSet<String>> extractIdentifiers(
      ImmutableList<? extends Tree> trees) {
    List<Set<String>> identifierCombinations = new ArrayList<>();
    identifierCombinations.add(new HashSet<>());
    TemplateIdentifierExtractor.INSTANCE.scan(trees, identifierCombinations);
    return identifierCombinations.stream().map(ImmutableSet::copyOf).collect(toImmutableSet());
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

    static boolean isUClassIdentifier(IdentifierTree tree) {
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

    static IdentifierTree getClassIdentifier(UStaticIdent tree) {
      return invokeMethod(METHOD_USTATIC_IDENT_CLASS_IDENT, tree);
    }

    // XXX: Arguments to this method must actually be of the package-private type `UClassIdent`.
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
        throw new IllegalStateException("Failed to load class `%s`".formatted(fqcn), e);
      }
    }

    @SuppressWarnings(
        "java:S3011" /* The `setAccessible` is required to access private methods from Refaster. */)
    private static Method getMethod(Class<?> clazz, String methodName) {
      try {
        Method method = clazz.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method;
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException(
            "No method `%s` on class `%s`".formatted(methodName, clazz.getName()), e);
      }
    }

    @SuppressWarnings({
      "TypeParameterUnusedInFormals",
      "unchecked"
    } /* Special use of reflection here. */)
    private static <T> T invokeMethod(Method method, Object instance) {
      try {
        return (T) method.invoke(instance);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new IllegalStateException("Failed to invoke method `%s`".formatted(method), e);
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
      if (RefasterIntrospection.isUClassIdentifier(node)) {
        for (Set<String> ids : identifierCombinations) {
          ids.add(getSimpleName(RefasterIntrospection.getTopLevelClass(node)));
          ids.add(getIdentifier(node));
        }
      } else if (node instanceof UStaticIdent uStaticIdent) {
        IdentifierTree subNode = RefasterIntrospection.getClassIdentifier(uStaticIdent);
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
      String id = SourceIdentifierExtractor.treeKindToString(node.getKind());
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
}
