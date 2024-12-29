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
    return ImmutableTable.builder();
  }

  ImmutableTable<Object, Object, Object> testImmutableTableBuilderBuildOrThrow() {
    return ImmutableTable.builder().buildOrThrow();
  }

  ImmutableSet<ImmutableTable<String, Integer, String>> testCellToImmutableTable() {
    return ImmutableSet.of(
        ImmutableTable.of(
            Tables.immutableCell("foo", 1, "bar").getRowKey(),
            Tables.immutableCell("foo", 1, "bar").getColumnKey(),
            Tables.immutableCell("foo", 1, "bar").getValue()),
        ImmutableTable.of(
            Tables.immutableCell("baz", 2, "qux").getRowKey(),
            Tables.immutableCell("baz", 2, "qux").getColumnKey(),
            Tables.immutableCell("baz", 2, "qux").getValue()));
  }

  ImmutableTable<Integer, String, Integer> testStreamOfCellsToImmutableTable() {
    return Stream.of(1, 2, 3).collect(toImmutableTable(n -> n, n -> n.toString(), n -> n * 2));
  }

  ImmutableTable<String, String, String> testImmutableTableOf() {
    return ImmutableTable.of();
  }
}
