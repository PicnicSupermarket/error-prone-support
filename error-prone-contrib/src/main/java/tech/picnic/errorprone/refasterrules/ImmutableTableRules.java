package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableTable.toImmutableTable;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link ImmutableTable}s. */
@OnlineDocumentation
final class ImmutableTableRules {
  private ImmutableTableRules() {}

  /** Prefer {@link ImmutableTable#builder()} over the associated constructor. */
  // XXX: This rule may drop generic type information, leading to non-compilable code.
  static final class ImmutableTableBuilder<R, C, V> {
    @BeforeTemplate
    ImmutableTable.Builder<R, C, V> before() {
      return new ImmutableTable.Builder<>();
    }

    @AfterTemplate
    ImmutableTable.Builder<R, C, V> after() {
      return ImmutableTable.builder();
    }
  }

  /** Prefer {@link ImmutableTable.Builder#buildOrThrow()} over less explicit alternatives. */
  static final class BuilderBuildOrThrow<R, C, V> {
    @BeforeTemplate
    ImmutableTable<R, C, V> before(ImmutableTable.Builder<R, C, V> builder) {
      return builder.build();
    }

    @AfterTemplate
    ImmutableTable<R, C, V> after(ImmutableTable.Builder<R, C, V> builder) {
      return builder.buildOrThrow();
    }
  }

  /**
   * Prefer {@link ImmutableTable#of(Object, Object, Object)} over less efficient or more contrived
   * alternatives.
   */
  static final class ImmutableTableOfCellGetRowKeyCellGetColumnKeyCellGetValue<
      R, C, V, R2 extends R, C2 extends C, V2 extends V> {
    @BeforeTemplate
    ImmutableTable<R, C, V> before(Table.Cell<R2, C2, V2> cell) {
      return Refaster.anyOf(
          ImmutableTable.<R, C, V>builder().put(cell).buildOrThrow(),
          Stream.of(cell)
              .collect(
                  toImmutableTable(
                      Table.Cell::getRowKey, Table.Cell::getColumnKey, Table.Cell::getValue)));
    }

    @AfterTemplate
    ImmutableTable<R, C, V> after(Table.Cell<R2, C2, V2> cell) {
      return ImmutableTable.of(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
    }
  }

  /** Prefer {@code stream.collect(toImmutableTable(...))} over more contrived alternatives. */
  abstract static class StreamCollectToImmutableTable<E, R, C, V> {
    @Placeholder(allowsIdentity = true)
    abstract R rowFunction(@MayOptionallyUse E element);

    @Placeholder(allowsIdentity = true)
    abstract C columnFunction(@MayOptionallyUse E element);

    @Placeholder(allowsIdentity = true)
    abstract V valueFunction(@MayOptionallyUse E element);

    @BeforeTemplate
    ImmutableTable<R, C, V> before(Stream<E> stream) {
      return stream
          .map(e -> Tables.immutableCell(rowFunction(e), columnFunction(e), valueFunction(e)))
          .collect(
              toImmutableTable(
                  Table.Cell::getRowKey, Table.Cell::getColumnKey, Table.Cell::getValue));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ImmutableTable<R, C, V> after(Stream<E> stream) {
      return stream.collect(
          toImmutableTable(e -> rowFunction(e), e -> columnFunction(e), e -> valueFunction(e)));
    }
  }

  /** Prefer {@link ImmutableTable#of()} over less efficient alternatives. */
  static final class ImmutableTableOf<R, C, V> {
    @BeforeTemplate
    ImmutableTable<R, C, V> before() {
      return ImmutableTable.<R, C, V>builder().buildOrThrow();
    }

    @AfterTemplate
    ImmutableTable<R, C, V> after() {
      return ImmutableTable.of();
    }
  }

  /**
   * Prefer {@link ImmutableTable.Builder#put(Object, Object, Object)} over more contrived
   * alternatives.
   */
  static final class BuilderPut<R, C, V> {
    @BeforeTemplate
    ImmutableTable.Builder<R, C, V> before(
        ImmutableTable.Builder<R, C, V> builder, R rowKey, C columnKey, V value) {
      return Refaster.anyOf(
          builder.put(Tables.immutableCell(rowKey, columnKey, value)),
          builder.putAll(ImmutableTable.of(rowKey, columnKey, value)));
    }

    @AfterTemplate
    ImmutableTable.Builder<R, C, V> after(
        ImmutableTable.Builder<R, C, V> builder, R rowKey, C columnKey, V value) {
      return builder.put(rowKey, columnKey, value);
    }
  }
}
