package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Source;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import tech.picnic.errorprone.utils.ThirdPartyLibrary;

/**
 * A {@link BugChecker} that flags {@link Collector Collectors} that don't clearly express
 * (im)mutability.
 *
 * <p>Replacing such collectors with alternatives that produce immutable collections is preferred.
 * Do note that Guava's immutable collections are null-hostile.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Avoid `Collectors.to{List,Map,Set}` in favor of collectors that emphasize (im)mutability",
    link = BUG_PATTERNS_BASE_URL + "CollectorMutability",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class CollectorMutability extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> COLLECTOR_METHOD =
      staticMethod().onClass(Collectors.class.getCanonicalName());
  private static final Matcher<ExpressionTree> LIST_COLLECTOR =
      staticMethod().anyClass().named("toList");
  private static final Matcher<ExpressionTree> MAP_COLLECTOR =
      staticMethod().anyClass().named("toMap");
  private static final Matcher<ExpressionTree> SET_COLLECTOR =
      staticMethod().anyClass().named("toSet");

  /** Instantiates a new {@link CollectorMutability} instance. */
  public CollectorMutability() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!COLLECTOR_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    if (LIST_COLLECTOR.matches(tree, state)) {
      return suggestToCollectionAlternatives(
          tree,
          ImmutableList.class.getCanonicalName() + ".toImmutableList",
          Collectors.class.getCanonicalName() + ".toUnmodifiableList",
          ArrayList.class.getCanonicalName(),
          state);
    }

    if (MAP_COLLECTOR.matches(tree, state)) {
      return suggestToMapAlternatives(tree, state);
    }

    if (SET_COLLECTOR.matches(tree, state)) {
      return suggestToCollectionAlternatives(
          tree,
          ImmutableSet.class.getCanonicalName() + ".toImmutableSet",
          Collectors.class.getCanonicalName() + ".toUnmodifiableSet",
          HashSet.class.getCanonicalName(),
          state);
    }

    return Description.NO_MATCH;
  }

  private Description suggestToCollectionAlternatives(
      MethodInvocationTree tree,
      String immutableReplacement,
      String unmodifiableReplacement,
      String mutableReplacement,
      VisitorState state) {
    Description.Builder description = buildDescription(tree);

    if (ThirdPartyLibrary.GUAVA.isIntroductionAllowed(state)) {
      description.addFix(replaceMethodInvocation(tree, immutableReplacement, state));
    }

    if (isJdk10Plus(state)) {
      description.addFix(replaceMethodInvocation(tree, unmodifiableReplacement, state));
    }

    SuggestedFix.Builder mutableFix = SuggestedFix.builder();
    String toCollectionSelect =
        SuggestedFixes.qualifyStaticImport(
            Collectors.class.getCanonicalName() + ".toCollection", mutableFix, state);
    String mutableCollection = SuggestedFixes.qualifyType(state, mutableFix, mutableReplacement);
    description.addFix(
        mutableFix
            .replace(tree, "%s(%s::new)".formatted(toCollectionSelect, mutableCollection))
            .build());

    return description.build();
  }

  private Description suggestToMapAlternatives(MethodInvocationTree tree, VisitorState state) {
    int argCount = tree.getArguments().size();
    if (argCount > 3) {
      return Description.NO_MATCH;
    }

    Description.Builder description = buildDescription(tree);

    if (ThirdPartyLibrary.GUAVA.isIntroductionAllowed(state)) {
      description.addFix(
          replaceMethodInvocation(
              tree, ImmutableMap.class.getCanonicalName() + ".toImmutableMap", state));
    }

    if (isJdk10Plus(state)) {
      description.addFix(
          replaceMethodInvocation(
              tree, Collectors.class.getCanonicalName() + ".toUnmodifiableMap", state));
    }

    SuggestedFix.Builder mutableFix = SuggestedFix.builder();
    String hashMap =
        SuggestedFixes.qualifyType(state, mutableFix, HashMap.class.getCanonicalName());
    description.addFix(
        mutableFix
            .postfixWith(
                tree.getArguments().get(argCount - 1),
                (argCount == 2 ? ", (a, b) -> { throw new IllegalStateException(); }" : "")
                    + ", %s::new".formatted(hashMap))
            .build());

    return description.build();
  }

  private static boolean isJdk10Plus(VisitorState state) {
    Source lowerBound = Source.lookup("10");
    return lowerBound != null && Source.instance(state.context).compareTo(lowerBound) >= 0;
  }

  private static SuggestedFix replaceMethodInvocation(
      MethodInvocationTree tree, String fullyQualifiedReplacement, VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder();
    String replacement = SuggestedFixes.qualifyStaticImport(fullyQualifiedReplacement, fix, state);
    fix.merge(SuggestedFix.replace(tree.getMethodSelect(), replacement));
    return fix.build();
  }
}
