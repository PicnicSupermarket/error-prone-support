package tech.picnic.errorprone.openai;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;
import org.fusesource.jansi.AnsiConsole;
import org.jline.console.SystemRegistry;
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
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Parameters;
import picocli.shell.jline3.PicocliCommands;
import picocli.shell.jline3.PicocliCommands.PicocliCommandsFactory;
import tech.picnic.errorprone.openai.IssueExtractor.Issue;

// XXX: Review whether to enable a *subset* of JLine's built-ins. See
// https://github.com/remkop/picocli/tree/main/picocli-shell-jline3#jline-316-and-picocli-44-example.

public final class Cli {
  public static void main(String... args) {
    AnsiConsole.systemInstall();
    try (Terminal terminal = TerminalBuilder.terminal()) {
      IssueResolutionController issueResolutionController =
          new IssueResolutionController(
              terminal.writer(),
              ImmutableSet.of(
                  new Issue<>(Path.of("xxx"), OptionalInt.of(1), OptionalInt.empty(), "msg"),
                  new Issue<>(Path.of("yyy"), OptionalInt.of(2), OptionalInt.empty(), "msg"),
                  new Issue<>(Path.of("xxx"), OptionalInt.of(3), OptionalInt.empty(), "msg")));

      PicocliCommandsFactory factory = new PicocliCommandsFactory();
      factory.setTerminal(terminal);

      CommandLine commandLine = new CommandLine(issueResolutionController, factory);
      PicocliCommands commands = new PicocliCommands(commandLine);

      Parser parser = new DefaultParser();

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
      new TailTipWidgets(
              reader, systemRegistry::commandDescription, 5, TailTipWidgets.TipType.COMPLETER)
          .enable();
      reader.getKeyMaps().get("main").bind(new Reference("tailtip-toggle"), KeyMap.ctrl('t'));

      while (true) {
        try {
          systemRegistry.cleanUp();
          systemRegistry.execute(
              reader.readLine(
                  issueResolutionController.prompt(), null, (MaskingCallback) null, null));
        } catch (UserInterruptException e) {
          // XXX: Review whether indeed to ignore this.
        } catch (EndOfFileException e) {
          return;
        } catch (Exception e) {
          systemRegistry.trace(e);
        }
      }
    } catch (Throwable t) {
      // XXX: Review!
      t.printStackTrace();
    } finally {
      AnsiConsole.systemUninstall();
    }
  }

  // XXX: Should we even support those `files.isEmpty()` cases?
  // XXX: Allow issues to be dropped?
  // XXX: Mark files modified/track modification count?
  // XXX: List full diff over multiple rounds?
  @Command(name = "")
  static final class IssueResolutionController {
    private final PrintWriter out;
    private final ImmutableList<FileIssues> files;
    private int currentIndex = 0;

    IssueResolutionController(PrintWriter output, ImmutableSet<Issue<Path>> issues) {
      this.out = output;
      this.files =
          issues.stream()
              .collect(
                  collectingAndThen(
                      groupingBy(Issue::file),
                      m ->
                          m.entrySet().stream()
                              .map(
                                  e ->
                                      new FileIssues(
                                          e.getKey(), ImmutableList.copyOf(e.getValue())))
                              .collect(toImmutableList())));
    }

    @Command(aliases = "i", subcommands = HelpCommand.class, description = "List issues.")
    void issues() {
      if (files.isEmpty()) {
        out.println("No issues.");
        return;
      }

      FileIssues fileIssues = files.get(currentIndex);
      out.printf("Issues for %s:%n", fileIssues.file());
      ImmutableList<Issue<Path>> issues = fileIssues.issues();
      for (int i = 0; i < issues.size(); i++) {
        out.printf(Locale.ROOT, "%2d. %s%n", i, issues.get(i).description());
      }
    }

    @Command(
        aliases = "s",
        subcommands = HelpCommand.class,
        description = "Submit issues to OpenAI.")
    void submit(
        @Parameters(arity = "0..*", description = "The subset of issues to submit (default: all)")
            List<Integer> issues) {
      // XXX: Validate indices.
      // XXX: Implement.
    }

    @Command(
        aliases = "a",
        subcommands = HelpCommand.class,
        description = "Apply the changes suggested by OpenAI")
    void apply() {
      // XXX: Implement.
    }

    @Command(
        aliases = "n",
        subcommands = HelpCommand.class,
        description = "Move to the next issue.")
    void next() {
      if (currentIndex < files.size() - 1) {
        currentIndex++;
        issues();
      } else {
        out.println("No next issues.");
      }
    }

    @Command(
        aliases = "p",
        subcommands = HelpCommand.class,
        description = "Move to the previous issue.")
    void previous() {
      if (currentIndex > 0) {
        currentIndex--;
        issues();
      } else {
        out.println("No previous issues.");
      }
    }

    String prompt() {
      return files.isEmpty()
          ? "No issues>"
          : String.format(
              "File %s (%s/%s)>", files.get(currentIndex).file(), currentIndex + 1, files.size());
    }

    record FileIssues(Path file, ImmutableList<Issue<Path>> issues) {}
  }
}
