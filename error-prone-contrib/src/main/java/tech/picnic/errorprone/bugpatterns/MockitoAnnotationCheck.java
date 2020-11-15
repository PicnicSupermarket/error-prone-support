package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.hasArgumentWithValue;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree.Kind;
import java.util.List;

/** A {@link BugChecker} which flags classes importing Mockito, but not enforcing strict mocks. */
@AutoService(BugChecker.class)
@BugPattern(
        name = "MockitoAnnotation",
        summary = "Prefer using strict stubs with Mockito",
        linkType = LinkType.NONE,
        severity = SeverityLevel.SUGGESTION,
        tags = StandardTags.STYLE,
        providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class MockitoAnnotationCheck extends BugChecker implements CompilationUnitTreeMatcher {
    private static final long serialVersionUID = 1L;
    private static final Matcher<AnnotationTree> MOCKITO_ANNOTATION_MATCHER =
            allOf(
                    isType("org.mockito.junit.jupiter.MockitoSettings"), hasArgumentWithValue(
                            "strictness",
                            isSameType("static org.mockito.quality.Strictness.STRICT_STUBS")));

    @Override
    public Description matchCompilationUnit(
            CompilationUnitTree compilationUnitTree, VisitorState state) {
        List<? extends ImportTree> imports = compilationUnitTree.getImports();

        if (imports.isEmpty()) {
            return NO_MATCH;
        }

        boolean importsMockito =
                imports.stream()
                        // Pattern matching using "packageStartsWith" doesn't work, because it only
                        // looks at the visitor and not the tree
                        .map(Object::toString)
                        .anyMatch(importLine -> importLine.startsWith("org.mockito"));
        if (!importsMockito) {
            return NO_MATCH;
        }

        ImmutableList<ClassTree> classDeclarations =
                compilationUnitTree.getTypeDecls().stream()
                        .filter(c -> c.getKind() == Kind.CLASS)
                        .map(ClassTree.class::cast)
                        .collect(toImmutableList());

        ImmutableList<ClassTree> violatingClasses =
                classDeclarations.stream()
                        .filter(clazz -> !hasMockitoAnnotation(clazz, state))
                        .collect(toImmutableList());

        if (violatingClasses.isEmpty()) {
            return NO_MATCH;
        }

        SuggestedFix.Builder fixBuilder = SuggestedFix.builder();
        for (ClassTree violatingClass : violatingClasses) {
            // And here I would like to say
            // "Please add @MockitoSettings(strictness = STRICT_STUBS)",
            // but don't know how =(
            fixBuilder.delete(violatingClass);
        }
        return describeMatch(violatingClasses.iterator().next(), fixBuilder.build());
    }

    private boolean hasMockitoAnnotation(ClassTree classTree, VisitorState state) {
        return classTree.getModifiers().getAnnotations().stream()
                .anyMatch(tree -> MOCKITO_ANNOTATION_MATCHER.matches(tree, state));
    }
}
