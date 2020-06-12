package org.parubok.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.Iterator;

class TableStreamUtilsTest {

    @Test
    void asIterable() throws Exception {
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
            Assertions.assertEquals("c0", cell_0.value);
            Assertions.assertEquals(0, cell_0.row);
            Assertions.assertEquals(0, cell_0.column);
            Assertions.assertEquals(table, cell_0.table);
            Assertions.assertTrue(iterator.hasNext());
            TableCellData<Object, JTable> cell_1 = iterator.next();
            Assertions.assertEquals("c1", cell_1.value);
            Assertions.assertEquals(0, cell_1.row);
            Assertions.assertEquals(1, cell_1.column);
            Assertions.assertEquals(table, cell_1.table);
            Assertions.assertFalse(iterator.hasNext());
        });
    }

    @Test
    void asStream() {
    }
}
