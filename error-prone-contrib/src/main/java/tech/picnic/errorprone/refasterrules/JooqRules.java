package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.stream.Stream;
import org.jooq.Record;
import org.jooq.ResultQuery;
import tech.picnic.errorprone.refaster.annotation.Description;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

@OnlineDocumentation
final class JooqRules {
  private JooqRules() {}

  /** Prefer eagerly fetching data over (lazy) streaming to avoid potentially leaking resources. */
  @Description(
      "Prefer eagerly fetching data over (lazy) streaming to avoid potentially leaking resources.")
  static final class ResultQueryFetchStream {
    @BeforeTemplate
    Stream<?> before(ResultQuery<? extends Record> resultQuery) {
      return resultQuery.stream();
    }

    @AfterTemplate
    Stream<?> after(ResultQuery<? extends Record> resultQuery) {
      return resultQuery.fetch().stream();
    }
  }
}
