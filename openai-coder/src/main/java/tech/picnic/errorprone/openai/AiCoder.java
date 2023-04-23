package tech.picnic.errorprone.openai;


import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.WillNotClose;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "", mixinStandardHelpOptions = true, description = "OpenAI Coder CLI.")
public final class AiCoder {
  /** The name of the environment variable containing the OpenAI token. */
  private static final String OPENAI_TOKEN_VARIABLE = "OPENAI_TOKEN";

  private final OpenAi openAi;
  // XXX: Drop these fields if unused.
  private final PrintStream out;
  private final PrintStream err;

  private AiCoder(OpenAi openAi, @WillNotClose PrintStream out, @WillNotClose PrintStream err) {
    this.openAi = openAi;
    this.out = out;
    this.err = err;
  }

  // XXX: Drop the `IOException` and properly handle it.
  @Command(
      name = "analyze-build-output",
      mixinStandardHelpOptions = true,
      description = "Attempts to resolve issues extracted from build output.")
  void analyzeBuildOutput(
      @Option(
              names = {"-a", "--auto-fix"},
              description = "Submit all issues to OpenAI and accept the results.")
          boolean autoFix,
      @ArgGroup(multiplicity = "1") BuildOutputSource buildOutputSource)
      throws IOException {
    if (autoFix) {
      // XXX: Implement auto-fixing.
      System.out.println(
          "Auto-fixing issues in "
              + buildOutputSource.buildCommand
              + " / "
              + buildOutputSource.buildOutputFile);
    } else {
      // XXX: Implement analyzing.
      System.out.println(
          "Analyzing issues in "
              + buildOutputSource.buildCommand
              + " / "
              + buildOutputSource.buildOutputFile);
    }

    InteractiveShell.run(openAi, buildOutputSource.buildOutputFile.orElseThrow());
  }

  public static void main(String... args) {
    AnsiConsole.systemInstall();
    try {
      String openAiToken = System.getenv(OPENAI_TOKEN_VARIABLE);
      if (openAiToken == null) {
        AnsiConsole.err().printf("Environment variable %s not set.%n", OPENAI_TOKEN_VARIABLE);
        System.exit(1);
      }

      try (OpenAi openAi = OpenAi.create(openAiToken)) {
        System.exit(createCommandLine(openAi).execute(args));
      }
    } finally {
      AnsiConsole.systemUninstall();
    }
  }

  private static CommandLine createCommandLine(OpenAi openAi) {
    CommandLine commandLine =
        new CommandLine(new AiCoder(openAi, AnsiConsole.out(), AnsiConsole.err()));
    commandLine.getCommandSpec().version(AiCoder.class.getPackage().getImplementationVersion());
    return commandLine;
  }

  // XXX: Update this class if PicoCLI ever supports (record) constructor injection. See
  // https://github.com/remkop/picocli/issues/1358.
  static final class BuildOutputSource {
    @Option(
        names = {"-f", "--file"},
        description = "The path to the file containing the build output to analyze.")
    private Optional<Path> buildOutputFile;

    @Option(
        names = {"-c", "--command"},
        description = "The command to run to produce the build output to analyze.")
    private Optional<String> buildCommand;
  }
}
