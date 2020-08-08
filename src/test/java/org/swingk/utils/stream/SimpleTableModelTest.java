package org.swingk.utils.stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;

class SimpleTableModelTest {
    @Test
    void basic_test_1() {
        List<List<Object>> data = asList(asList(1, "A", "data1"), asList(2, "B", "data2"));
        SimpleTableModel<String> model = new SimpleTableModel<>(data, asList(Integer.class, String.class),
                asList("col1", "col2"), new boolean[]{false, true});
        Assertions.assertEquals(2, model.getRowCount());
        Assertions.assertEquals(2, model.getColumnCount());
        Assertions.assertEquals(1, model.getValueAt(0, 0));
        Assertions.assertEquals("A", model.getValueAt(0, 1));
        Assertions.assertEquals(2, model.getValueAt(1, 0));
        Assertions.assertEquals("B", model.getValueAt(1, 1));
        Assertions.assertEquals(Integer.class, model.getColumnClass(0));
        Assertions.assertEquals(String.class, model.getColumnClass(1));
        Assertions.assertEquals("col1", model.getColumnName(0));
        Assertions.assertEquals("col2", model.getColumnName(1));
        Assertions.assertFalse(model.isCellEditable(0, 0));
        Assertions.assertTrue(model.isCellEditable(0, 1));
        Assertions.assertFalse(model.isCellEditable(1, 0));
        Assertions.assertTrue(model.isCellEditable(1, 1));
        Assertions.assertEquals("data1", model.getRowObject(0));
        Assertions.assertEquals("data2", model.getRowObject(1));
    }

    @Test
    void add_remove_row_1() {
        List<List<Object>> data = asList(asList(1, "A", "data1"), asList(2, "B", "data2"));
        SimpleTableModel<String> model = new SimpleTableModel<>(data, asList(Integer.class, String.class),
                asList("col1", "col2"), new boolean[]{false, true});
        model.removeRow(0);
        Assertions.assertEquals(1, model.getRowCount());
        Assertions.assertEquals("data2", model.getRowObject(0));
        model.addRow(new Object[]{3, "C"});
        Assertions.assertEquals(2, model.getRowCount());
        Assertions.assertNull(model.getRowObject(1));
        model.setRowObject(1, "data3");
        Assertions.assertEquals("data3", model.getRowObject(1));
    }
}
