package tech.picnic.errorprone.documentation;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.tools.javac.api.BasicJavacTask;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A compiler {@link Plugin} that analyzes and extracts relevant information for documentation
 * purposes from processed files.
 */
@AutoService(Plugin.class)
public final class DocumentationGenerator implements Plugin {
  @VisibleForTesting static final String DOCS_DIRECTORY = "docs";
  @VisibleForTesting static final String OUTPUT_DIRECTORY_FLAG = "-XdocsOutputDirectory";
  private static final Pattern OUTPUT_DIRECTORY_FLAG_PATTERN =
      Pattern.compile(Pattern.quote(OUTPUT_DIRECTORY_FLAG) + "=(.*)");

  /** Instantiates a new {@link DocumentationGenerator} instance. */
  public DocumentationGenerator() {}

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public void init(JavacTask javacTask, String... args) {
    checkArgument(args.length == 1, "Precisely one path must be provided");

    javacTask.addTaskListener(
        new DocumentationGeneratorTaskListener(
            ((BasicJavacTask) javacTask).getContext(), getDocsPath(args[0])));
  }

  @VisibleForTesting
  static Path getDocsPath(String docsPathArg) {
    Matcher matcher = OUTPUT_DIRECTORY_FLAG_PATTERN.matcher(docsPathArg);
    checkArgument(
        matcher.matches(),
        "'%s' must be of the form '%s=<value>'",
        docsPathArg,
        OUTPUT_DIRECTORY_FLAG);

    String basePath = matcher.group(1) + File.separator + DOCS_DIRECTORY;
    try {
      return Path.of(basePath);
    } catch (InvalidPathException e) {
      throw new IllegalArgumentException(String.format("Invalid path '%s'", basePath), e);
    }
  }
}
