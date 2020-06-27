package org.swingk.utils.table;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class TableStreamUtilsTest {

    @Test
    void asIterable_1() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TableModel model = new DefaultTableModel(1, 2);
            model.setValueAt("c0", 0, 0);
            model.setValueAt("c1", 0, 1);
            JTable table = new JTable(model);
            Iterable<TableCellData<JTable>> iterable = TableStreamUtils.asIterable(table);
            Assertions.assertNotNull(iterable);
            Iterator<TableCellData<JTable>> iterator = iterable.iterator();
            Assertions.assertNotNull(iterator);
            Assertions.assertTrue(iterator.hasNext());
            TableCellData<JTable> cell_0 = iterator.next();
            Assertions.assertEquals("c0", cell_0.getValue());
            Assertions.assertEquals(0, cell_0.getRow());
            Assertions.assertEquals(0, cell_0.getColumn());
            Assertions.assertEquals(table, cell_0.getTable());
            Assertions.assertTrue(iterator.hasNext());
            TableCellData<JTable> cell_1 = iterator.next();
            Assertions.assertEquals("c1", cell_1.getValue());
            Assertions.assertEquals(0, cell_1.getRow());
            Assertions.assertEquals(1, cell_1.getColumn());
            Assertions.assertEquals(table, cell_1.getTable());
            Assertions.assertFalse(iterator.hasNext());
        });
    }

    @Test
    void asStream_getValue() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TableModel model = new DefaultTableModel(1, 2);
            model.setValueAt("c0", 0, 0);
            model.setValueAt("c1", 0, 1);
            JTable table = new JTable(model);
            Assertions.assertIterableEquals(Arrays.asList("c0", "c1"), TableStreamUtils.asStream(table)
                    .map(TableCellData::getValue)
                    .collect(Collectors.toList()));
        });
    }

    @Test
    void asStream_2() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            final int rows = 5;
            final int columns = 3;
            TableModel model = new DefaultTableModel(rows, columns);
            for (int i = 0; i < rows; i++) {
                model.setValueAt(Integer.valueOf(i), i, 1);
            }
            JTable table = new JTable(model);
            Assertions.assertEquals(10, TableStreamUtils.asStream(table)
                    .filter(d -> d.getColumn() == 1)
                    .mapToInt(d -> (Integer) d.getValue())
                    .sum());
        });
    }

    @Test
    void asStream_3() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            final int rows = 2;
            final int columns = 3;
            TableModel model = new DefaultTableModel(rows, columns);
            JTable table = new JTable(model);
            Assertions.assertEquals(6, TableStreamUtils.asStream(table)
                    .peek(d -> Assertions.assertSame(table, d.getTable()))
                    .peek(d -> Assertions.assertFalse(d.isSelected()))
                    .peek(d -> Assertions.assertTrue(d.isEditable()))
                    .count());
        });
    }

    @Test
    void asStream_getRow() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            final int rows = 2;
            final int columns = 3;
            TableModel model = new DefaultTableModel(rows, columns);
            JTable table = new JTable(model);
            Assertions.assertIterableEquals(Arrays.asList(0, 0, 0, 1, 1, 1), TableStreamUtils.asStream(table)
                    .map(d -> d.getRow())
                    .collect(Collectors.toList()));
        });
    }

    @Test
    void asStream_getColumn() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            final int rows = 2;
            final int columns = 3;
            TableModel model = new DefaultTableModel(rows, columns);
            JTable table = new JTable(model);
            Assertions.assertIterableEquals(Arrays.asList(0, 1, 2, 0, 1, 2), TableStreamUtils.asStream(table)
                    .map(d -> d.getColumn())
                    .collect(Collectors.toList()));
        });
    }

    @Test
    void asStream_empty() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TableModel model = new DefaultTableModel(0, 0);
            JTable table = new JTable(model);
            Assertions.assertIterableEquals(Collections.emptyList(), TableStreamUtils.asStream(table)
                    .collect(Collectors.toList()));
        });
    }

    @Test
    void concurrent_modification_rows() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DefaultTableModel model = new DefaultTableModel(3, 2);
            JTable table = new JTable(model);
            Iterable<TableCellData<JTable>> iterable = TableStreamUtils.asIterable(table);
            Iterator<TableCellData<JTable>> iterator = iterable.iterator();
            iterator.next();
            model.addRow(new Object[] { "d1", "d2" });
            ConcurrentModificationException ex = Assertions.assertThrows(ConcurrentModificationException.class,
                    () -> iterator.next());
            Assertions.assertEquals("Expected row count: 3, actual row count: 4.", ex.getMessage());
        });
    }

    @Test
    void toJTable_1() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<String> values = Arrays.asList("valA", "valB", "valC");
            JTable table = values.stream().collect(TableStreamUtils.toJTable(new Column<>("col1")));
            Assertions.assertEquals(values.size(), table.getRowCount());
            Assertions.assertEquals(1, table.getColumnCount());
            Assertions.assertEquals("col1", table.getColumnName(0));
            for (int i = 0; i < values.size(); i++) {
                Assertions.assertEquals(values.get(i), table.getValueAt(i, 0));
            }
        });
    }

    @Test
    void toJTable_parallelStream() throws Exception {
        List<Integer> values = IntStream.range(0, 100_000).mapToObj(Integer::new).collect(Collectors.toList());
        JTable table = values.parallelStream().collect(TableStreamUtils.toJTable(new Column<>("col1")));
        SwingUtilities.invokeAndWait(() -> {
            Assertions.assertEquals(values.size(), table.getRowCount());
            Assertions.assertEquals(1, table.getColumnCount());
            Assertions.assertEquals("col1", table.getColumnName(0));
            for (int i = 0; i < values.size(); i++) {
                Assertions.assertEquals(values.get(i), table.getValueAt(i, 0));
            }
        });
    }

    @Test
    void toJTable_no_columns() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> Arrays.asList("value").stream().collect(TableStreamUtils.toJTable()));
    }
}
