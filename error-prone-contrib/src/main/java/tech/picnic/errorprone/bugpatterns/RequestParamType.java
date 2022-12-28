package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.isType;
import static com.google.errorprone.matchers.Matchers.not;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

/** A {@link BugChecker} that flags {@code @RequestParam} parameters with an unsupported type. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "`@RequestParam` does not support `ImmutableCollection` and `ImmutableMap` subtypes",
    link = BUG_PATTERNS_BASE_URL + "RequestParamType",
    linkType = CUSTOM,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class RequestParamType extends BugChecker implements VariableTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String FLAG_PREFIX = "RequestParamType:";
  private static final String INCLUDED_CLASS_FLAG = FLAG_PREFIX + "Includes";

  private final Matcher<VariableTree> hasUnsupportedRequestParams;

  /** Instantiates a default {@link RequestParamType} instance. */
  public RequestParamType() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link RequestParamType} instance.
   *
   * @param flags Any provided command line flags.
   */
  public RequestParamType(ErrorProneFlags flags) {
    hasUnsupportedRequestParams = createVariableTreeMatcher(flags);
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    return hasUnsupportedRequestParams.matches(tree, state)
        ? describeMatch(tree)
        : Description.NO_MATCH;
  }

  private static Matcher<VariableTree> createVariableTreeMatcher(ErrorProneFlags flags) {
    return allOf(
        annotations(AT_LEAST_ONE, isType("org.springframework.web.bind.annotation.RequestParam")),
        anyOf(isSubtypeOf(ImmutableCollection.class), isSubtypeOf(ImmutableMap.class)),
        not(anyOf(getSupportedClasses(includedClassNames(flags)))));
  }

  private static ImmutableList<String> includedClassNames(ErrorProneFlags flags) {
    return flags.getList(INCLUDED_CLASS_FLAG).map(ImmutableList::copyOf).orElse(ImmutableList.of());
  }

  private static ImmutableList<Matcher<Tree>> getSupportedClasses(
      ImmutableList<String> inclusions) {
    return inclusions.stream()
        .filter(inclusion -> !inclusion.isEmpty())
        .map(String::trim)
        .map(inclusion -> isSubtypeOf(createClass(inclusion)))
        .collect(toImmutableList());
  }

  private static Class<?> createClass(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(
          String.format("Invalid class name '%s' in `%s`", className, INCLUDED_CLASS_FLAG), e);
    }
  }
}
