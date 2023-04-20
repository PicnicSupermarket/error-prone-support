package tech.picnic.errorprone.openai;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.fusesource.jansi.AnsiConsole;
import org.jline.builtins.ConfigurationPath;
import org.jline.console.SystemRegistry;
import org.jline.console.impl.Builtins;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.keymap.KeyMap;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.Parser;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.widget.TailTipWidgets;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Model.UsageMessageSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.ParseResult;
import picocli.shell.jline3.PicocliCommands;
import picocli.shell.jline3.PicocliCommands.PicocliCommandsFactory;
import tech.picnic.errorprone.openai.IssueExtractor.Issue;

// XXX: Review whether to enable a *subset* of JLine's built-ins. See
// https://github.com/remkop/picocli/tree/main/picocli-shell-jline3#jline-316-and-picocli-44-example.
public final class Cli {
  public static void main(String... args) {
    IssueResolutionController issueResolutionController =
        new IssueResolutionController(ImmutableSetMultimap.of());

    AnsiConsole.systemInstall();
    try {
      CommandSpec issues = command("issues", 'i', "List issues.");
      CommandSpec submit =
          command("submit", 's', "Submit issues to OpenAI.")
              .addPositional(
                  PositionalParamSpec.builder()
                      .paramLabel("ISSUES")
                      .type(List.class)
                      .auxiliaryTypes(Integer.class)
                      .description("The subset of issues to submit (default: all)")
                      .build());
      CommandSpec apply = command("apply", 'a', "Apply the changes suggested by OpenAI");
      CommandSpec next = command("next", 'n', "Move to the next issue.");
      CommandSpec previous = command("previous", 'p', "Move to the previous issue.");

      PicocliCommandsFactory factory = new PicocliCommandsFactory();
      // Or, if you have your own factory, you can chain them like this:
      // MyCustomFactory customFactory = createCustomFactory(); // your application custom factory
      // PicocliCommandsFactory factory = new PicocliCommandsFactory(customFactory); // chain the
      // factories

      CommandLine cmd =
          new CommandLine(
              CommandSpec.create()
                  .name("")
                  .addSubcommand(null, issues)
                  .addSubcommand(null, submit)
                  .addSubcommand(null, apply)
                  .addSubcommand(null, next)
                  .addSubcommand(null, previous),
              factory);
      // XXX: Rename to `commands` if we don't enable the built-ins.
      PicocliCommands commands = new PicocliCommands(cmd);

      Parser parser = new DefaultParser();
      // XXX: Check `TerminalBuilder.builder().build()` options.
      try (Terminal terminal = TerminalBuilder.terminal()) {
        SystemRegistry systemRegistry =
            new SystemRegistryImpl(parser, terminal, () -> Path.of("").toAbsolutePath(), null);
        systemRegistry.setCommandRegistries(commands);
        systemRegistry.register("help", commands);

        LineReader reader =
            LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(systemRegistry.completer())
                .parser(parser)
                .build();
        cmd.setExecutionStrategy(
            pr -> run(pr, reader.getTerminal().writer(), issueResolutionController));
        factory.setTerminal(terminal);
        new TailTipWidgets(
                reader, systemRegistry::commandDescription, 5, TailTipWidgets.TipType.COMPLETER)
            .enable();
        reader.getKeyMaps().get("main").bind(new Reference("tailtip-toggle"), KeyMap.ctrl('t'));

        while (true) {
          try {
            systemRegistry.cleanUp();
            systemRegistry.execute(reader.readLine("prompt> ", null, (MaskingCallback) null, null));
          } catch (UserInterruptException e) {
            // XXX: Review whether indeed to ignore this.
          } catch (EndOfFileException e) {
            return;
          } catch (Exception e) {
            systemRegistry.trace(e);
          }
        }
      }
    } catch (Throwable t) {
      // XXX: Review!
      t.printStackTrace();
    } finally {
      AnsiConsole.systemUninstall();
    }
  }

  static final class IssueResolutionController {
    private final ImmutableList<ImmutableList<Issue<Path>>> issuesGroupedByPath;
    private int currentIndex = 0;

    // XXX: Maybe shouldn't already group by path at call site?
    IssueResolutionController(ImmutableSetMultimap<Path, Issue<Path>> issues) {
      this.issuesGroupedByPath =
          issues.asMap().values().stream().map(ImmutableList::copyOf).collect(toImmutableList());
    }

    void list(PrintWriter out) {
      if (currentIndex >= issuesGroupedByPath.size()) {
        out.println("No more issues.");
        return;
      }

      ImmutableList<Issue<Path>> issues = issuesGroupedByPath.get(currentIndex);
      out.printf("Issues for %s:%n", issues.get(0).file());
      for (int i = 0; i < issues.size(); i++) {
        out.printf(Locale.ROOT, "%02d. %s%n", i, issues.get(i).description());
      }
    }

    void next(PrintWriter out) {
      currentIndex++;
      list(out);
    }

    void previous(PrintWriter out) {
      currentIndex--;
      list(out);
    }
  }

  static int run(ParseResult pr, PrintWriter out, IssueResolutionController controller) {
    Integer helpExitCode = CommandLine.executeHelpRequest(pr);
    if (helpExitCode != null) {
      return helpExitCode;
    }

    ParseResult subcommand = Objects.requireNonNull(pr.subcommand(), "subcommand");
    switch (subcommand.commandSpec().name()) {
      case "issues" -> controller.list(out);
      case "submit" -> out.println("submit");
      case "apply" -> out.println("apply");
      case "next" -> controller.next(out);
      case "previous" -> controller.previous(out);
      default -> throw new IllegalStateException(
          "Unknown command: " + subcommand.commandSpec().name());
    }
    return 0;
  }

  private static CommandSpec command(String name, char alias, String description) {
    return CommandSpec.create()
        .name(name)
        .aliases(Character.toString(alias))
        .usageMessage(new UsageMessageSpec().description(description))
        .addOption(helpOption());
  }

  private static OptionSpec helpOption() {
    return OptionSpec.builder("-h", "--help")
        .usageHelp(true)
        .description("Show this help message and exit.")
        .build();
  }
}
