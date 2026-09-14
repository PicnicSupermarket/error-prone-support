package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MemberSelectTreeMatcher;
import com.google.errorprone.bugpatterns.StaticImports;
import com.google.errorprone.bugpatterns.StaticImports.StaticImportInfo;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import java.util.Optional;
import javax.inject.Inject;

/**
 * A {@link BugChecker} that flags type members that can and should be statically imported.
 *
 * <p>See {@link StaticImportConfig} for how additional candidates can be configured, and how this
 * check interacts with {@link NonStaticImport}.
 */
// XXX: This check is closely linked to `NonStaticImport`. Consider merging the two.
// XXX: Tricky cases:
// - `org.springframework.http.HttpStatus` (not always an improvement, and `valueOf` must
//    certainly be excluded)
// - `com.google.common.collect.Tables`
// - `ch.qos.logback.classic.Level.{DEBUG, ERROR, INFO, TRACE, WARN}`
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Identifier should be statically imported",
    link = BUG_PATTERNS_BASE_URL + "StaticImport",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
@SuppressWarnings({
  "java:S2160" /* Super class equality definition suffices. */,
  "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
})
public final class StaticImport extends BugChecker implements MemberSelectTreeMatcher {
  private static final long serialVersionUID = 1L;

  private final StaticImportConfig config;

  /** Instantiates a default {@link StaticImport} instance. */
  public StaticImport() {
    this(new StaticImportConfig(ErrorProneFlags.empty()));
  }

  /**
   * Instantiates a customized {@link StaticImport}.
   *
   * @param config The candidate-matching configuration to apply.
   */
  @Inject
  StaticImport(StaticImportConfig config) {
    this.config = config;
  }

  @Override
  public Description matchMemberSelect(MemberSelectTree tree, VisitorState state) {
    if (!isCandidateContext(state) || !config.isCandidate(tree)) {
      return Description.NO_MATCH;
    }

    StaticImportInfo importInfo = StaticImports.tryCreate(tree, state);
    if (importInfo == null) {
      return Description.NO_MATCH;
    }

    return config
        .getCandidateSimpleName(importInfo)
        .flatMap(n -> tryStaticImport(tree, importInfo.canonicalName() + '.' + n, n, state))
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static boolean isCandidateContext(VisitorState state) {
    Tree parentTree =
        requireNonNull(state.getPath().getParentPath(), "MemberSelectTree lacks enclosing node")
            .getLeaf();

    return parentTree instanceof MethodInvocationTree methodInvocation
        ? methodInvocation.getTypeArguments().isEmpty()
        : !(parentTree instanceof ImportTree || parentTree instanceof MemberSelectTree);
  }

  private static Optional<Fix> tryStaticImport(
      MemberSelectTree tree, String fullyQualifiedName, String simpleName, VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder().replace(tree, simpleName);

    if (!simpleName.equals(SuggestedFixes.qualifyStaticImport(fullyQualifiedName, fix, state))) {
      /* Statically importing this symbol would clash with an existing import. */
      return Optional.empty();
    }

    return Optional.of(fix.build());
  }
}
