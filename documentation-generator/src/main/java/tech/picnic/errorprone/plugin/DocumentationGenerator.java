package tech.picnic.errorprone.plugin;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.tools.javac.api.BasicJavacTask;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * A compiler {@link Plugin plugin} that analyzes and extracts relevant information for
 * documentation purposes from processed files.
 */
@AutoService(Plugin.class)
public final class DocumentationGenerator implements Plugin {
  @VisibleForTesting static final String DOCS_DIRECTORY = "docs";
  @VisibleForTesting static final String OUTPUT_DIRECTORY_OPTION = "-XdocsOutputDirectory";

  /** Instantiates a new {@link DocumentationGenerator} instance. */
  public DocumentationGenerator() {}

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public void init(JavacTask javacTask, String... args) {
    checkArgument(args.length == 1, "Specify only one output path");
    javacTask.addTaskListener(
        new DocumentationGeneratorTaskListener(
            ((BasicJavacTask) javacTask).getContext(), getDocsPath(args[0])));
  }

  @VisibleForTesting
  static Path getDocsPath(String docsPathArg) {
    String[] option = docsPathArg.split("=", 2);
    validateOutputDirectoryOption(option.length == 2, docsPathArg);
    validateOutputDirectoryOption(option[0].equals(OUTPUT_DIRECTORY_OPTION), docsPathArg);

    String basePath = option[1];
    try {
      return Path.of(basePath, DOCS_DIRECTORY);
    } catch (InvalidPathException e) {
      throw new IllegalArgumentException(
          String.format("Error while creating directory with path '%s'", basePath), e);
    }
  }

  private static void validateOutputDirectoryOption(boolean expression, String docsPathArg) {
    checkArgument(
        expression, "%s must be of the form '%s=<value>'", docsPathArg, OUTPUT_DIRECTORY_OPTION);
  }
}
