package tech.picnic.errorprone.documentation;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.tools.javac.api.BasicJavacTask;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

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
    checkArgument(args.length == 1, "Only a single path can be provided");
    javacTask.addTaskListener(
        new DocumentationGeneratorTaskListener(
            ((BasicJavacTask) javacTask).getContext(), getDocsPath(args[0])));
  }

  @VisibleForTesting
  static Path getDocsPath(String docsPathArg) {
    List<String> option = Splitter.on("=").splitToList(docsPathArg);
    validateOutputDirectoryOption(option.size() == 2, docsPathArg);
    validateOutputDirectoryOption(option.get(0).equals(OUTPUT_DIRECTORY_OPTION), docsPathArg);

    String basePath = option.get(1) + File.separator + DOCS_DIRECTORY;
    try {
      return Path.of(basePath);
    } catch (InvalidPathException e) {
      throw new IllegalArgumentException(
          String.format("Error while creating path '%s'", basePath), e);
    }
  }

  private static void validateOutputDirectoryOption(boolean expression, String docsPathArg) {
    checkArgument(
        expression, "%s must be of the form '%s=<value>'", docsPathArg, OUTPUT_DIRECTORY_OPTION);
  }
}
