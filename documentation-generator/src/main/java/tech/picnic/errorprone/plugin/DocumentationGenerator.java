package tech.picnic.errorprone.plugin;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.tools.javac.api.BasicJavacTask;

/**
 * A compiler {@link Plugin plugin} that analyzes and extracts relevant information for
 * documentation purposes from processed files.
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
    checkArgument(args.length == 1, "Specify one output path");
    javacTask.addTaskListener(
        new DocumentationGeneratorTaskListener(((BasicJavacTask) javacTask).getContext(), args[0]));
  }
}
