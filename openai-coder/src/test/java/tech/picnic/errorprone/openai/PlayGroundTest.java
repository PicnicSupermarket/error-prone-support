package tech.picnic.errorprone.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.picnic.errorprone.openai.OpenAi.OPENAI_TOKEN_VARIABLE;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

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
}
