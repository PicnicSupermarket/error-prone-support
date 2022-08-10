package tech.picnic.errorprone.refaster.runner;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ResourceInfo;
import com.google.errorprone.CodeTransformer;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UncheckedIOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Scans the classpath for {@value #REFASTER_TEMPLATE_SUFFIX} files and loads them as {@link
 * CodeTransformer}s.
 */
public final class CodeTransformers {
  private static final String REFASTER_TEMPLATE_SUFFIX = ".refaster";
  private static final Supplier<ImmutableListMultimap<String, CodeTransformer>>
      ALL_CODE_TRANSFORMERS = Suppliers.memoize(CodeTransformers::loadAllCodeTransformers);

  private CodeTransformers() {}

  /**
   * Returns all Refaster {@link CodeTransformer}s found on the classpath.
   *
   * <p>This method returns a cached view; all invocations except the first are very cheap.
   *
   * @return A mapping from Refaster template names to associated {@link CodeTransformer}s.
   */
  public static ImmutableListMultimap<String, CodeTransformer> getAllCodeTransformers() {
    return ALL_CODE_TRANSFORMERS.get();
  }

  /**
   * Scans the classpath for compiled Refaster templates and returns the associated deserialized
   * {@link CodeTransformer}s, indexed by their name.
   *
   * @return A mapping from Refaster template names to associated {@link CodeTransformer}s.
   */
  private static ImmutableListMultimap<String, CodeTransformer> loadAllCodeTransformers() {
    ImmutableListMultimap.Builder<String, CodeTransformer> transformers =
        ImmutableListMultimap.builder();

    for (ResourceInfo resource : getClassPathResources()) {
      getRefasterTemplateName(resource)
          .ifPresent(
              templateName ->
                  loadCodeTransformer(resource)
                      .ifPresent(transformer -> transformers.put(templateName, transformer)));
    }

    return transformers.build();
  }

  private static ImmutableSet<ResourceInfo> getClassPathResources() {
    try {
      return ClassPath.from(CodeTransformers.class.getClassLoader()).getResources();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to scan classpath for resources", e);
    }
  }

  private static Optional<String> getRefasterTemplateName(ResourceInfo resource) {
    String resourceName = resource.getResourceName();
    if (!resourceName.endsWith(REFASTER_TEMPLATE_SUFFIX)) {
      return Optional.empty();
    }

    int lastPathSeparator = resourceName.lastIndexOf('/');
    int beginIndex = lastPathSeparator < 0 ? 0 : lastPathSeparator + 1;
    int endIndex = resourceName.length() - REFASTER_TEMPLATE_SUFFIX.length();
    return Optional.of(resourceName.substring(beginIndex, endIndex));
  }

  private static Optional<CodeTransformer> loadCodeTransformer(ResourceInfo resource) {
    try (InputStream in = resource.url().openStream();
        ObjectInputStream ois = new ObjectInputStream(in)) {
      @SuppressWarnings("BanSerializableRead" /* Part of the Refaster API. */)
      CodeTransformer codeTransformer = (CodeTransformer) ois.readObject();
      return Optional.of(codeTransformer);
    } catch (NoSuchElementException e) {
      /* For some reason we can't load the resource. Skip it. */
      // XXX: Should we log this?
      return Optional.empty();
    } catch (ClassCastException e) {
      /* This resource does not appear to be compatible with the current classpath. */
      // XXX: Should we log this?
      return Optional.empty();
    } catch (ClassNotFoundException | IOException e) {
      throw new IllegalStateException("Can't load `CodeTransformer` from " + resource, e);
    }
  }
}
