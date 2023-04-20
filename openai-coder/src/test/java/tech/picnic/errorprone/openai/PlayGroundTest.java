package tech.picnic.errorprone.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.picnic.errorprone.openai.OpenAi.OPENAI_TOKEN_VARIABLE;

import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.ParseResult;

// XXX: Drop this code.
@TestInstance(Lifecycle.PER_CLASS)
@EnabledIfSystemProperty(named = OPENAI_TOKEN_VARIABLE, matches = ".*")
final class PlayGroundTest {
  private final OpenAi openAi = OpenAi.create();

  @AfterAll
  void tearDown() {
    openAi.close();
  }

  @Test
  @Disabled
  void test() {
    String input =
        """
        I would like to generate Refaster rules that match a Java expression A and transform it into an equivalent Java expression B.

        This is an example of a Refaster rule that unwraps `Comparator#comparing` arguments to `Comparator#thenComparing`:

        import static java.util.Comparator.comparing;
        import com.google.errorprone.refaster.annotation.AfterTemplate;
        import com.google.errorprone.refaster.annotation.BeforeTemplate;
        import java.util.Comparator;
        import java.util.function.Function;

        /** Don't explicitly create {@link Comparator}s unnecessarily. */
        final class ThenComparing<S, T extends Comparable<? super T>> {
          @BeforeTemplate
          Comparator<S> before(Comparator<S> cmp, Function<? super S, ? extends T> function) {
            return cmp.thenComparing(comparing(function));
          }

          @AfterTemplate
          Comparator<S> after(Comparator<S> cmp, Function<? super S, ? extends T> function) {
            return cmp.thenComparing(function);
          }
        }

        ###

        Write a Refaster rule that transforms `mono.blockOptional().map(function)` into `mono.map(function).blockOptional()`.

        Requirements:
        - Just write the new code. Don't explain yourself.
        - If the rule requires type parameters, declare those on the class.
        - Add all relevant imports.
        """
            .stripTrailing();

    // XXX: ^ That trailing whitespace removal is crucial!!

    assertThat(openAi.requestChatCompletion(input)).isEqualTo("XXX");
  }

  @Test
  void cliTest() {

    //    OptionSpec help = OptionSpec.builder("help", "h")
    //            .usageHelp(true).build();

    //    CommandSpec help =
    //        CommandSpec.create()
    //            .name("help")
    //            .aliases("h")
    //            .addPositional(
    //                PositionalParamSpec.builder().paramLabel("COMMAND").arity("0..1").build());
    CommandSpec issues = CommandSpec.create().name("issues").aliases("i");
    CommandSpec submit =
        CommandSpec.create()
            .name("submit")
            .aliases("s")
            .addPositional(
                PositionalParamSpec.builder()
                    .paramLabel("ISSUES")
                    .type(List.class)
                    .auxiliaryTypes(Integer.class)
                    .description("The subset of issues to submit (default: all)")
                    .build());
    CommandSpec apply = CommandSpec.create().name("apply").aliases("a");
    CommandSpec next = CommandSpec.create().name("next").aliases("n");
    CommandSpec previous = CommandSpec.create().name("previous").aliases("prev", "p");
    CommandSpec quit = CommandSpec.create().name("quit").aliases("q");
    CommandSpec commands =
        CommandSpec.create()
            .name("")
            //            .addSubcommand(null, help)
            .addSubcommand(null, issues)
            .addSubcommand(null, submit)
            .addSubcommand(null, apply)
            .addSubcommand(null, next)
            .addSubcommand(null, previous)
            .addSubcommand(null, quit)
        //                .addOption(help)
        ;

    CommandLine commandLine = new CommandLine(commands);

    // set an execution strategy (the run(ParseResult) method) that will be called
    // by CommandLine.execute(args) when user input was valid
    commandLine.setExecutionStrategy(PlayGroundTest::run);
    //    int exitCode = commandLine.execute("-c", "4", "file1", "file2");
    //        int exitCode = commandLine.execute("issues");
    //    int exitCode = commandLine.execute("submit", "4");
    int exitCode = commandLine.execute("submit", "-h");
    assertThat(exitCode).isEqualTo(0);
  }

  static int run(ParseResult pr) {

    // handle requests for help or version information
    Integer helpExitCode = CommandLine.executeHelpRequest(pr);
    if (helpExitCode != null) {
      return helpExitCode;
    }

    ParseResult subcommand = Objects.requireNonNull(pr.subcommand(), "subcommand");
    switch (subcommand.commandSpec().name()) {
      case "help":
        PositionalParamSpec x = subcommand.matchedPositional(0);
    }

    // implement the business logic
    //    int count = pr.matchedOptionValue('c', 1);
    //    List<File> files = pr.matchedPositionalValue(0, Collections.<File>emptyList());
    return 0;
  }
}
