package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.sun.source.tree.Tree.Kind.ASSIGNMENT;
import static java.lang.String.CASE_INSENSITIVE_ORDER;

import com.google.auto.service.AutoService;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.code.Symbol;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.lang.model.element.ElementKind;

@AutoService(BugChecker.class)
@BugPattern(
        name = "MockBeanAnnotation",
        summary = "Prefer class-level shiz",
        linkType = BugPattern.LinkType.NONE,
        severity = BugPattern.SeverityLevel.SUGGESTION,
        tags = BugPattern.StandardTags.SIMPLIFICATION,
        providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public class MockBeanAnnotationCheck extends BugChecker implements BugChecker.ClassTreeMatcher {
    private static final String MOCKBEAN_FQN = "org.springframework.boot.test.mock.mockito.MockBean";
    private static final String AUTOWIRED_FQN =
            "org.springframework.beans.factory.annotation.Autowired";
    private static final ImmutableSet<String> VALID_MOCKBEAN_ATTRIBUTE_NAMES =
            ImmutableSet.of("classes", "value");
    private static final Splitter ATTRIBUTE_SPLITTER = Splitter.on(',').trimResults();

    @Override
    public Description matchClass(ClassTree tree, VisitorState state) {
        Symbol.ClassSymbol symbol = ASTHelpers.getSymbol(tree);
        if (symbol == null) {
            return NO_MATCH;
        }
        ImmutableList<VariableTree> mockBeans =
                tree.getMembers().stream()
                        .flatMap(t -> tryGetMockBeanField(t, state).stream())
                        .collect(toImmutableList());
        ImmutableList<VariableTree> unused = unused(state, mockBeans);
        if (unused.isEmpty()) {
            return NO_MATCH;
        }
        return describeMatch(tree, suggestedFix(tree, state, unused));
    }

    private static ImmutableList<VariableTree> unused(
            VisitorState state, ImmutableList<VariableTree> trees) {
        ImmutableMap<Symbol, VariableTree> symbolToTree =
                Maps.uniqueIndex(trees, ASTHelpers::getSymbol);
        UsedVariableScanner scanner = new UsedVariableScanner(symbolToTree.keySet());
        scanner.scan(state.getPath(), null);
        return scanner.getUnusedVariables().stream().map(symbolToTree::get).collect(toImmutableList());
    }

    private Fix suggestedFix(
            ClassTree tree, VisitorState state, ImmutableList<VariableTree> mockBeans) {
        Optional<? extends AnnotationTree> classLevelMockBean =
                getMockBeanAnnotation(tree.getModifiers(), state);

        Stream<String> preExistingImports =
                classLevelMockBean.stream()
                        .flatMap(annotationTree -> annotationTree.getArguments().stream())
                        .filter(MockBeanAnnotationCheck::isBeanDefinitionArgument)
                        .flatMap(assignment -> getArguments(assignment, state));

        Stream<String> importsToAdd =
                mockBeans.stream()
                        .map(t -> extractSimplifiedTypeName(t.getType()))
                        .distinct()
                        .map(s -> s + ".class");

        ImmutableList<String> imports =
                Stream.concat(importsToAdd, preExistingImports)
                        .distinct()
                        .sorted(CASE_INSENSITIVE_ORDER)
                        .collect(toImmutableList());

        SuggestedFix.Builder deleteFields =
                mockBeans.stream()
                        .reduce(
                                SuggestedFix.builder(), SuggestedFix.Builder::delete, SuggestedFix.Builder::merge);

        String templateString = imports.size() > 1 ? "@MockBean({%s})" : "@MockBean(%s)";
        return classLevelMockBean
                // XXX: What to do if "classes" was used?
                .map(
                        annotationTree ->
                                SuggestedFixes.updateAnnotationArgumentValues(annotationTree, "value", imports))
                .orElseGet(
                        () ->
                                SuggestedFix.builder()
                                        .prefixWith(
                                                tree, String.format(templateString, String.join(", ", imports)) + "\n"))
                .merge(deleteFields)
                .build();
    }

    private static boolean isBeanDefinitionArgument(ExpressionTree tree) {
        return tree.getKind() == ASSIGNMENT
                && VALID_MOCKBEAN_ATTRIBUTE_NAMES.contains(
                ASTHelpers.getSymbol(((AssignmentTree) tree).getVariable()).getSimpleName().toString());
    }

    private static Stream<String> getArguments(ExpressionTree tree, VisitorState state) {
        if (tree.getKind() != ASSIGNMENT) {
            return Stream.of();
        }

        return ATTRIBUTE_SPLITTER.splitToStream(
                Util.treeToString(((AssignmentTree) tree).getExpression(), state)
                        .replace("{", "")
                        .replace("}", ""));
    }

    // Can we get other types that we need to handle or is there a smarter/helper way?
    private static String extractSimplifiedTypeName(Tree type) {
        if (type instanceof ParameterizedTypeTree) {
            return extractSimplifiedTypeName(((ParameterizedTypeTree) type).getType());
        }
        return type.toString();
    }

    private static Optional<VariableTree> tryGetMockBeanField(Tree tree, VisitorState state) {
        if (!(tree instanceof VariableTree)) {
            return Optional.empty();
        }

        VariableTree variableTree = (VariableTree) tree;
        Symbol symbol = ASTHelpers.getSymbol(tree);
        if (symbol == null || symbol.getKind() != ElementKind.FIELD) {
            return Optional.empty();
        }
        if (!ASTHelpers.hasAnnotation(symbol, MOCKBEAN_FQN, state)) {
            return Optional.empty();
        }

        if (variableTree.getModifiers().getAnnotations().size() == 2
                && !ASTHelpers.hasAnnotation(symbol, AUTOWIRED_FQN, state)) {
            return Optional.empty();
        }
        AnnotationTree mockBean =
                getMockBeanAnnotation(variableTree.getModifiers(), state).orElseThrow();
        if (!mockBean.getArguments().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(variableTree);
    }

    private static Optional<? extends AnnotationTree> getMockBeanAnnotation(
            ModifiersTree tree, VisitorState state) {
        return tree.getAnnotations().stream().filter(t -> isMockBean(t, state)).findFirst();
    }

    private static boolean isMockBean(AnnotationTree tree, VisitorState state) {
        return state
                .getTypes()
                .isSameType(ASTHelpers.getType(tree), state.getTypeFromString(MOCKBEAN_FQN));
    }

    // XXX: Should cover more cases. This only covers `unused#member` and `unused = ...`,
    // but not instanceof, if etc.
    private static final class UsedVariableScanner extends TreePathScanner<Void, Void> {
        private final Set<Symbol> unusedVariables;

        public UsedVariableScanner(ImmutableSet<Symbol> toScanFor) {
            this.unusedVariables = new HashSet<>(toScanFor);
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree node, Void unused) {
            removeFromUnused(node.getExpression());
            return super.visitMemberSelect(node, unused);
        }

        @Override
        public Void visitAssignment(AssignmentTree node, Void unused) {
            removeFromUnused(node.getVariable());
            return super.visitAssignment(node, unused);
        }

        public Set<Symbol> getUnusedVariables() {
            return unusedVariables;
        }

        private void removeFromUnused(Tree tree) {
            Symbol symbol = ASTHelpers.getSymbol(tree);
            if (symbol != null) {
                unusedVariables.remove(symbol);
            }
        }
    }
}
