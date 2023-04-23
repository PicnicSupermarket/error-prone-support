package tech.picnic.errorprone.openai;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.WillClose;
import javax.annotation.WillNotClose;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import tech.picnic.errorprone.openai.IssueExtractor.Issue;

@Command(name = "", mixinStandardHelpOptions = true, description = "OpenAI Coder CLI.")
public final class AiCoder {
  /** The name of the environment variable containing the OpenAI token. */
  private static final String OPENAI_TOKEN_VARIABLE = "OPENAI_TOKEN";

  private final OpenAi openAi;
  private final PrintStream out;
  private final PrintStream err;

  private AiCoder(OpenAi openAi, @WillNotClose PrintStream out, @WillNotClose PrintStream err) {
    this.openAi = openAi;
    this.out = out;
    this.err = err;
  }

  @Command(
      name = "process-build-output",
      mixinStandardHelpOptions = true,
      showEndOfOptionsDelimiterInUsageHelp = true,
      description = "Attempts to resolve issues extracted from build output.")
  void processBuildOutput(
      @Option(
              names = {"-a", "--auto-fix"},
              description = "Submit all issues to OpenAI and accept the results.")
          boolean autoFix,
      @Option(
              names = {"-c", "--command"},
              description =
                  "Interpret the positional arguments as a command to be executed, rather than output files.")
          boolean command,
      @Parameters(description = "The files containing the build output or the command to run.")
          List<String> filesOrCommand) {
    // XXX: Replace this code.
    if (autoFix) {
      // XXX: Implement auto-fixing.
      System.out.println("Auto-fixing issues in " + filesOrCommand);
    } else {
      // XXX: Implement analyzing.
      System.out.println("Analyzing issues in " + filesOrCommand);
    }

    Supplier<ImmutableSet<Issue<Path>>> issueSupplier =
        command
            ? () -> extractIssues(ImmutableList.copyOf(filesOrCommand), out, err)
            : () ->
                filesOrCommand.stream()
                    .flatMap(f -> extractIssues(Path.of(f)).stream())
                    .collect(toImmutableSet());

    InteractiveBuildOutputProcessor.run(openAi, issueSupplier);
  }

  private static ImmutableSet<Issue<Path>> extractIssues(
      ImmutableList<String> command, PrintStream out, PrintStream err) {
    try {
      // XXX: If `ctrl-c` is pressed while the command is running, then seemingly JLine does't
      // intercept it. Investigate.
      Process process = new ProcessBuilder(command).start();

      StringBuilder collectedOutput = new StringBuilder();
      try (InputStream processOutput = process.getInputStream();
          BufferedReader output = new BufferedReader(new InputStreamReader(processOutput, UTF_8))) {
        String line = null;
        for (line = output.readLine(); line != null; line = output.readLine()) {
          out.println(line);
          collectedOutput.append(line).append('\n');
        }
        process.waitFor();
        // XXX: Report the exit code?
        // XXX: Process the output.
      } catch (InterruptedException e) {
        err.println("Interrupted while waiting for process to finish.");
        Thread.currentThread().interrupt();
      }

      // XXX: This `ByteArrayInputStream` usage is dodgy. Review.
      return extractIssues(new ByteArrayInputStream(collectedOutput.toString().getBytes(UTF_8)));
    } catch (IOException e) {
      throw new UncheckedIOException(
          String.format(
              "Failed to execute or parse result of command '%s'", String.join(" ", command)),
          e);
    }
  }

  private static ImmutableSet<Issue<Path>> extractIssues(Path file) {
    try (InputStream is = Files.newInputStream(file)) {
      return extractIssues(is);
    } catch (IOException e) {
      throw new UncheckedIOException(String.format("Failed to parse file '%s'", file), e);
    }
  }

  private static ImmutableSet<Issue<Path>> extractIssues(@WillClose InputStream inputStream)
      throws IOException {
    // XXX: Here and elsewhere: should we allow the cwd to be changed?
    IssueExtractor<Path> issueExtractor =
        new PathResolvingIssueExtractor(
            new PathFinder(FileSystems.getDefault(), Path.of("")),
            new SelectFirstIssueExtractor<>(
                ImmutableSet.of(
                    new MavenCheckstyleIssueExtractor(), new PlexusCompilerIssueExtractor())));

    return LogLineExtractor.mavenErrorAndWarningExtractor().extract(inputStream).stream()
        .flatMap(issueExtractor::extract)
        .collect(toImmutableSet());
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
}
