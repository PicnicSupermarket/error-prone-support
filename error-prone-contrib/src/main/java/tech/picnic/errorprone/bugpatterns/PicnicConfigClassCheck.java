package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.util.ASTHelpers.getDeclaredSymbol;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import java.util.Optional;
import javax.lang.model.element.Modifier;

/** A {@link BugChecker} which flags `Configuration` classes that can be simplified. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "PicnicConfigClass",
    summary = "Enforces rules pertaining to Picnic `Configuration` classes.",
    linkType = LinkType.NONE,
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.SIMPLIFICATION,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class PicnicConfigClassCheck extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String CONFIG_CLASS_SUFFIX = "Config";
  private static final String CONFIG_ANNOTATION =
      "org.springframework.context.annotation.Configuration";

  @Override
  public Description matchClass(ClassTree clazz, VisitorState state) {
    if (!isConfigClass(clazz)) {
      return NO_MATCH;
    }

    SuggestedFix.Builder fixBuilder = SuggestedFix.builder();
    SuggestedFixes.addModifiers(clazz, state, Modifier.FINAL).ifPresent(fixBuilder::merge);
    tryRemoveConfigurationAnnotation(clazz).ifPresent(fixBuilder::merge);
    SuggestedFix fix = fixBuilder.build();

    if (!fix.isEmpty()) {
      String message =
          String.format(
              "Configuration class %s should be final and not be annotated with `@Configuration`",
              clazz.getSimpleName());
      return buildDescription(clazz).setMessage(message).addFix(fix).build();
    }

    return NO_MATCH;
  }

  /** If present removes the `@Configuration` annotation from the supplied class element. */
  private static Optional<SuggestedFix> tryRemoveConfigurationAnnotation(ClassTree clazz) {
    for (AnnotationTree annotation : clazz.getModifiers().getAnnotations()) {
      Symbol annotationSymbol = getDeclaredSymbol(annotation);
      if (annotationSymbol != null
          && annotationSymbol.getQualifiedName().contentEquals(CONFIG_ANNOTATION)) {
        return Optional.of(SuggestedFix.delete(annotation));
        /* Defer cleanup of possibly unused import to another rule. */
      }
    }
    return Optional.empty();
  }

  /**
   * Determines whether a class is a `Configuration` class according to Picnic naming conventions.
   */
  private static boolean isConfigClass(ClassTree clazz) {
    return clazz.getKind() == Tree.Kind.CLASS
        && clazz.getSimpleName().toString().endsWith(CONFIG_CLASS_SUFFIX);
  }
}
