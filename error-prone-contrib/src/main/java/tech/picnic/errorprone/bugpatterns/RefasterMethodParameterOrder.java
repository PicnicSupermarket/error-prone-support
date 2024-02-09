package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.lang.model.element.Name;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags Refaster methods with a non-canonical parameter order.
 *
 * <p>To a first approximation, parameters should be ordered by their first usage in an
 * {@code @AfterTemplate} method. Ties are broken by preferring the order dictated by methods with a
 * larger number of parameters.
 */
// XXX: This check can introduce suggestions that are incompatible with Error Prone's
// `InconsistentOverloads` check. Review whether/how to improve this.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Refaster template parameters should be listed in a canonical order",
    link = BUG_PATTERNS_BASE_URL + "RefasterMethodParameterOrder",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
public final class RefasterMethodParameterOrder extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<Tree> BEFORE_TEMPLATE_METHOD = hasAnnotation(BeforeTemplate.class);
  private static final Matcher<Tree> BEFORE_OR_AFTER_TEMPLATE_METHOD =
      anyOf(BEFORE_TEMPLATE_METHOD, hasAnnotation(AfterTemplate.class));

  /** Instantiates a new {@link RefasterMethodParameterOrder} instance. */
  public RefasterMethodParameterOrder() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    ImmutableList<MethodTree> methods = getMethodsByPriority(tree, state);
    if (methods.isEmpty()) {
      return Description.NO_MATCH;
    }

    Comparator<VariableTree> canonicalOrder = determineCanonicalParameterOrder(methods);

    return methods.stream()
        .flatMap(m -> tryReorderParameters(m, canonicalOrder, state))
        .reduce(SuggestedFix.Builder::merge)
        .map(SuggestedFix.Builder::build)
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static ImmutableList<MethodTree> getMethodsByPriority(
      ClassTree tree, VisitorState state) {
    return tree.getMembers().stream()
        .filter(m -> BEFORE_OR_AFTER_TEMPLATE_METHOD.matches(m, state))
        .map(MethodTree.class::cast)
        .sorted(
            comparing((MethodTree m) -> BEFORE_TEMPLATE_METHOD.matches(m, state))
                .thenComparingInt(m -> -m.getParameters().size()))
        .collect(toImmutableList());
  }

  private static Comparator<VariableTree> determineCanonicalParameterOrder(
      ImmutableList<MethodTree> methods) {
    Set<Name> canonicalOrder = new LinkedHashSet<>();
    methods.forEach(m -> processParameters(m, canonicalOrder));

    ImmutableList<Name> reversedCanonicalOrder = ImmutableList.copyOf(canonicalOrder).reverse();
    return comparing(
        VariableTree::getName,
        Comparator.<Name>comparingInt(reversedCanonicalOrder::indexOf)
            .reversed()
            .thenComparing(Name::toString));
  }

  private static void processParameters(MethodTree method, Set<Name> orderedParams) {
    Set<Symbol> toBeOrdered =
        method.getParameters().stream()
            .map(ASTHelpers::getSymbol)
            .collect(toCollection(HashSet::new));

    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitIdentifier(IdentifierTree node, @Nullable Void unused) {
        if (toBeOrdered.remove(ASTHelpers.getSymbol(node))) {
          orderedParams.add(node.getName());
        }
        return super.visitIdentifier(node, null);
      }
    }.scan(method, null);
  }

  private static Stream<SuggestedFix.Builder> tryReorderParameters(
      MethodTree method, Comparator<VariableTree> canonicalOrder, VisitorState state) {
    List<? extends VariableTree> originalOrder = method.getParameters();
    ImmutableList<? extends VariableTree> orderedParams =
        ImmutableList.sortedCopyOf(canonicalOrder, originalOrder);

    return originalOrder.equals(orderedParams)
        ? Stream.empty()
        : Streams.zip(
            originalOrder.stream(),
            orderedParams.stream().map(p -> SourceCode.treeToString(p, state)),
            SuggestedFix.builder()::replace);
  }
}
