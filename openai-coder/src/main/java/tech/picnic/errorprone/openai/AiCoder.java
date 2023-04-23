package tech.picnic.errorprone.openai;

import java.io.PrintWriter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "", mixinStandardHelpOptions = true, description = "OpenAI Coder CLI.")
public final class AiCoder {
  private final PrintWriter out;

  private AiCoder(PrintWriter out) {
    this.out = out;
  }

  @Command(
      name = "analyze-build-output",
      mixinStandardHelpOptions = true,
      description = "Attempts to resolve issues extracted from build output.")
  void analyzeBuildOutput(
      @Option(
              names = {"-a", "--auto-fix"},
              description = "Submit all issues to OpenAI and accept the results.")
          boolean autoFix,
      @Parameters(description = "The path to the file containing the build output to analyze.")
          String buildOutputPath) {
    if (autoFix) {
      // XXX: Implement auto-fixing.
      System.out.println("Auto-fixing issues in " + buildOutputPath);
    } else {
      // XXX: Implement analyzing.
      System.out.println("Analyzing issues in " + buildOutputPath);
    }
  }

  public static void main(String[] args) {
    // XXX: Instead of using System.out consider using the ansi terminal.
    CommandLine commandLine = new CommandLine(new AiCoder(new PrintWriter(System.out)));
    commandLine.getCommandSpec().version(AiCoder.class.getPackage().getImplementationVersion());

    System.exit(commandLine.execute(args));
  }
}
