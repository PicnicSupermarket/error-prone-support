package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ResourceInfo;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.CompositeCodeTransformer;
import com.google.errorprone.SubContext;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.CompilationUnitTree;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * A {@link BugChecker} which flags code which can be simplified using a Refaster template located
 * on the classpath.
 *
 * <p>This checker locates all {@code *.refaster} classpath resources and assumes they contain a
 * {@link CodeTransformer}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "Refaster",
    summary = "Write idiomatic code when possible",
    linkType = LinkType.NONE,
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.SIMPLIFICATION,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class RefasterCheck extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;

  private final CodeTransformer codeTransformer = loadCompositeCodeTransformer();

  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    codeTransformer.apply(state.getPath(), new SubContext(state.context), state::reportMatch);

    /*
     * Any matches (there may be multiple) were already reported above by the `CodeTransformer`,
     * directly to the `VisitorState`.
     */
    return Description.NO_MATCH;
  }

  private static CodeTransformer loadCompositeCodeTransformer() {
    return CompositeCodeTransformer.compose(
        getClassPathResources().stream()
            .filter(ri -> ri.getResourceName().endsWith(".refaster"))
            .map(RefasterCheck::loadCodeTransformer)
            .flatMap(Streams::stream)
            .collect(toImmutableList()));
  }

  private static ImmutableSet<ResourceInfo> getClassPathResources() {
    try {
      return ClassPath.from(RefasterCheck.class.getClassLoader()).getResources();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to scan classpath for resources", e);
    }
  }

  private static Optional<CodeTransformer> loadCodeTransformer(ResourceInfo resource) {
    try (InputStream in = resource.url().openStream();
        ObjectInputStream ois = new ObjectInputStream(in)) {
      return Optional.of((CodeTransformer) ois.readObject());
    } catch (NoSuchElementException e) {
      /* For some reason we can't load the resource. Skip it. */
      // XXX: Should we log this?
      return Optional.empty();
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalStateException("Can't load `CodeTransformer` from " + resource, e);
    }
  }
}
