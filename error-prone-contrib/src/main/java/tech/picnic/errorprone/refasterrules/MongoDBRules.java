package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static com.mongodb.client.model.Filters.eq;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to MongoDB client expressions and statements. */
@OnlineDocumentation
final class MongoDBRules {
  private MongoDBRules() {}

  /** Avoid {@link Enum#toString()} invocations when invoking {@link Filters#eq(String, Object)}. */
  static final class Eq {
    @BeforeTemplate
    Bson before(String field, Enum<?> value) {
      return eq(field, value.toString());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Bson after(String field, Enum<?> value) {
      return eq(field, value);
    }
  }
}
