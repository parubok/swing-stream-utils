package org.parubok.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

class TableStreamUtilsTest {

    @Test
    void asIterable_1() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TableModel model = new DefaultTableModel(1, 2);
            model.setValueAt("c0", 0, 0);
            model.setValueAt("c1", 0, 1);
            JTable table = new JTable(model);
            Iterable<TableCellData<Object, JTable>> iterable = TableStreamUtils.asIterable(table);
            Assertions.assertNotNull(iterable);
            Iterator<TableCellData<Object, JTable>> iterator = iterable.iterator();
            Assertions.assertNotNull(iterator);
            Assertions.assertTrue(iterator.hasNext());
            TableCellData<Object, JTable> cell_0 = iterator.next();
            Assertions.assertEquals("c0", cell_0.getValue());
            Assertions.assertEquals(0, cell_0.getRow());
            Assertions.assertEquals(0, cell_0.getColumn());
            Assertions.assertEquals(table, cell_0.getTable());
            Assertions.assertTrue(iterator.hasNext());
            TableCellData<Object, JTable> cell_1 = iterator.next();
            Assertions.assertEquals("c1", cell_1.getValue());
            Assertions.assertEquals(0, cell_1.getRow());
            Assertions.assertEquals(1, cell_1.getColumn());
            Assertions.assertEquals(table, cell_1.getTable());
            Assertions.assertFalse(iterator.hasNext());
        });
    }

    @Test
    void asStream_1() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TableModel model = new DefaultTableModel(1, 2);
            model.setValueAt("c0", 0, 0);
            model.setValueAt("c1", 0, 1);
            JTable table = new JTable(model);
            Assertions.assertEquals(Arrays.asList("c0", "c1"), TableStreamUtils.asStream(table)
                    .map(TableCellData::getValue)
                    .collect(Collectors.toList()));
        });
    }
}
