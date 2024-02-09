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
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

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
import com.google.errorprone.suppliers.Suppliers;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import javax.inject.Inject;
import tech.picnic.errorprone.utils.Flags;

/** A {@link BugChecker} that flags {@code @RequestParam} parameters with an unsupported type. */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "By default, `@RequestParam` does not support `ImmutableCollection` and `ImmutableMap` subtypes",
    link = BUG_PATTERNS_BASE_URL + "RequestParamType",
    linkType = CUSTOM,
    severity = ERROR,
    tags = LIKELY_ERROR)
@SuppressWarnings("java:S2160" /* Super class equality definition suffices. */)
public final class RequestParamType extends BugChecker implements VariableTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String SUPPORTED_CUSTOM_TYPES_FLAG = "RequestParamType:SupportedCustomTypes";

  private final Matcher<VariableTree> hasUnsupportedRequestParamType;

  /** Instantiates a default {@link RequestParamType} instance. */
  public RequestParamType() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link RequestParamType} instance.
   *
   * @param flags Any provided command line flags.
   */
  @Inject
  RequestParamType(ErrorProneFlags flags) {
    hasUnsupportedRequestParamType = hasUnsupportedRequestParamType(flags);
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    return hasUnsupportedRequestParamType.matches(tree, state)
        ? describeMatch(tree)
        : Description.NO_MATCH;
  }

  private static Matcher<VariableTree> hasUnsupportedRequestParamType(ErrorProneFlags flags) {
    return allOf(
        annotations(AT_LEAST_ONE, isType("org.springframework.web.bind.annotation.RequestParam")),
        anyOf(isSubtypeOf(ImmutableCollection.class), isSubtypeOf(ImmutableMap.class)),
        not(isSubtypeOfAny(Flags.getList(flags, SUPPORTED_CUSTOM_TYPES_FLAG))));
  }

  private static Matcher<Tree> isSubtypeOfAny(ImmutableList<String> inclusions) {
    return anyOf(
        inclusions.stream()
            .map(inclusion -> isSubtypeOf(Suppliers.typeFromString(inclusion)))
            .collect(toImmutableList()));
  }
}
