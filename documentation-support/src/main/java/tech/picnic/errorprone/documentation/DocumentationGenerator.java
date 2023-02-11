package tech.picnic.errorprone.documentation;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.tools.javac.api.BasicJavacTask;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A compiler {@link Plugin} that analyzes and extracts relevant information for documentation
 * purposes from processed files.
 */
// XXX: Find a better name for this class; it doesn't generate documentation per se.
@AutoService(Plugin.class)
public final class DocumentationGenerator implements Plugin {
  @VisibleForTesting static final String OUTPUT_DIRECTORY_FLAG = "-XoutputDirectory";
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
            ((BasicJavacTask) javacTask).getContext(), getOutputPath(args[0])));
  }

  @VisibleForTesting
  static Path getOutputPath(String pathArg) {
    Matcher matcher = OUTPUT_DIRECTORY_FLAG_PATTERN.matcher(pathArg);
    checkArgument(
        matcher.matches(), "'%s' must be of the form '%s=<value>'", pathArg, OUTPUT_DIRECTORY_FLAG);

    String path = matcher.group(1);
    try {
      return Path.of(path);
    } catch (InvalidPathException e) {
      throw new IllegalArgumentException(String.format("Invalid path '%s'", path), e);
    }
  }
}
