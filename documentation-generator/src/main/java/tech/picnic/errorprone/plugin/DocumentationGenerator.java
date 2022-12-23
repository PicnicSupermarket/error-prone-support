package tech.picnic.errorprone.plugin;

import com.google.auto.service.AutoService;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.tools.javac.api.BasicJavacTask;

/**
 * A compiler {@link Plugin plugin} that analyzes and extracts data from files containing relevant
 * information for documentation purposes.
 */
@AutoService(Plugin.class)
public final class DocumentationGenerator implements Plugin {
  /** Instantiates a new {@link DocumentationGenerator} instance. */
  public DocumentationGenerator() {}

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public void init(JavacTask javacTask, String... args) {
    javacTask.addTaskListener(
        new DocumentationGeneratorTaskListener(((BasicJavacTask) javacTask).getContext(), args[0]));
  }
}
