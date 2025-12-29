package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableTable.toImmutableTable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableTableRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Table.class);
  }

  ImmutableTable.Builder<String, Integer, String> testImmutableTableBuilder() {
    return new ImmutableTable.Builder<>();
  }

  ImmutableTable<Object, Object, Object> testImmutableTableBuilderBuildOrThrow() {
    return ImmutableTable.builder().build();
  }

  ImmutableSet<ImmutableTable<String, Integer, String>> testCellToImmutableTable() {
    return ImmutableSet.of(
        ImmutableTable.<String, Integer, String>builder()
            .put(Tables.immutableCell("foo", 1, "bar"))
            .buildOrThrow(),
        Stream.of(Tables.immutableCell("baz", 2, "qux"))
            .collect(
                toImmutableTable(
                    Table.Cell::getRowKey, Table.Cell::getColumnKey, Table.Cell::getValue)));
  }

  ImmutableTable<Integer, String, Integer> testStreamOfCellsToImmutableTable() {
    return Stream.of(1, 2, 3)
        .map(n -> Tables.immutableCell(n, n.toString(), n * 2))
        .collect(
            toImmutableTable(
                Table.Cell::getRowKey, Table.Cell::getColumnKey, Table.Cell::getValue));
  }

  ImmutableTable<String, String, String> testImmutableTableOf() {
    return ImmutableTable.<String, String, String>builder().buildOrThrow();
  }

  ImmutableTable.Builder<String, String, Integer> testImmutableTableBuilderPut() {
    return ImmutableTable.<String, String, Integer>builder()
        .putAll(ImmutableTable.of("row", "col", 1));
  }
}
