package tech.picnic.errorprone.refaster.benchmark;

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

/** A compiler {@link Plugin} that generates JMH benchmarks for Refaster rules. */
// XXX: Review whether this can be an annotation processor instead.
@AutoService(Plugin.class)
public final class RefasterRuleBenchmarkGenerator implements Plugin {
  @VisibleForTesting static final String OUTPUT_DIRECTORY_FLAG = "-XoutputDirectory";
  private static final Pattern OUTPUT_DIRECTORY_FLAG_PATTERN =
      Pattern.compile(Pattern.quote(OUTPUT_DIRECTORY_FLAG) + "=(.*)");

  /** Instantiates a new {@link RefasterRuleBenchmarkGenerator} instance. */
  public RefasterRuleBenchmarkGenerator() {}

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public void init(JavacTask javacTask, String... args) {
    checkArgument(args.length == 1, "Precisely one path must be provided");

    // XXX: Drop all path logic: instead generate in the same package as the Refaster rule
    // collection. (But how do we then determine the base directory?)
    javacTask.addTaskListener(
        new RefasterRuleBenchmarkGeneratorTaskListener(
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
