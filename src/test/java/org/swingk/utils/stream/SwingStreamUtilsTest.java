package org.swingk.utils.stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class SwingStreamUtilsTest {

    @Test
    void asIterable_1() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TableModel model = new DefaultTableModel(1, 2);
            model.setValueAt("c0", 0, 0);
            model.setValueAt("c1", 0, 1);
            JTable table = new JTable(model);
            Iterable<TableCellData<JTable>> iterable = SwingStreamUtils.asIterable(table);
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
    void asIterable_2() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TableModel model = new DefaultTableModel(1, 1);
            JTable table = new JTable(model);
            Iterable<TableCellData<JTable>> iterable = SwingStreamUtils.asIterable(table);
            Assertions.assertNotNull(iterable);
            Iterator<TableCellData<JTable>> iterator = iterable.iterator();
            Assertions.assertTrue(iterator.hasNext());
            Assertions.assertNull(iterator.next().getValue());
            Assertions.assertFalse(iterator.hasNext());
            Assertions.assertThrows(NoSuchElementException.class, () -> iterator.next());
        });
    }

    @Test
    void asStream_getValue() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TableModel model = new DefaultTableModel(1, 2);
            model.setValueAt("c0", 0, 0);
            model.setValueAt("c1", 0, 1);
            JTable table = new JTable(model);
            Assertions.assertIterableEquals(Arrays.asList("c0", "c1"), SwingStreamUtils.asStream(table)
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
            Assertions.assertEquals(10, SwingStreamUtils.asStream(table)
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
            Assertions.assertEquals(6, SwingStreamUtils.asStream(table)
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
            Assertions.assertIterableEquals(Arrays.asList(0, 0, 0, 1, 1, 1), SwingStreamUtils.asStream(table)
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
            Assertions.assertIterableEquals(Arrays.asList(0, 1, 2, 0, 1, 2), SwingStreamUtils.asStream(table)
                    .map(d -> d.getColumn())
                    .collect(Collectors.toList()));
        });
    }

    @Test
    void asStream_empty() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TableModel model = new DefaultTableModel(0, 0);
            JTable table = new JTable(model);
            Assertions.assertIterableEquals(Collections.emptyList(), SwingStreamUtils.asStream(table)
                    .collect(Collectors.toList()));
        });
    }

    @Test
    void concurrent_modification_rows() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DefaultTableModel model = new DefaultTableModel(3, 2);
            JTable table = new JTable(model);
            Iterable<TableCellData<JTable>> iterable = SwingStreamUtils.asIterable(table);
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
            JTable table = values.stream().collect(SwingStreamUtils.toTable(new Column<>("col1")));
            Assertions.assertEquals(values.size(), table.getRowCount());
            Assertions.assertEquals(1, table.getColumnCount());
            Assertions.assertEquals("col1", table.getColumnName(0));
            for (int i = 0; i < values.size(); i++) {
                Assertions.assertEquals(values.get(i), table.getValueAt(i, 0));
            }
        });
    }

    @Test
    void toJTable_2() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<Rectangle> values = Arrays.asList(new Rectangle(0, 0, 2, 5), new Rectangle(1, 1, 3, 6));
            JTable table = values.stream()
                    .collect(SwingStreamUtils.toTable(new Column<>("Location", r -> r.getLocation(), 20, Point.class),
                                                        new Column<>("Size", r -> r.getSize(), 30, Dimension.class)));
            Assertions.assertEquals(values.size(), table.getRowCount());
            Assertions.assertEquals(2, table.getColumnCount());
            Assertions.assertEquals("Location", table.getColumnName(0));
            Assertions.assertEquals("Size", table.getColumnName(1));
            Assertions.assertEquals(Point.class, table.getColumnClass(0));
            Assertions.assertEquals(Dimension.class, table.getColumnClass(1));
            Assertions.assertEquals(new Point(0, 0), table.getValueAt(0, 0));
            Assertions.assertEquals(new Point(1, 1), table.getValueAt(1, 0));
            Assertions.assertEquals(new Dimension(2, 5), table.getValueAt(0, 1));
            Assertions.assertEquals(new Dimension(3, 6), table.getValueAt(1, 1));
        });
    }

    @Test
    void toTableModel_1() {
        List<Point> values = new ArrayList<>();
        for (int i = 0; i < 100_0000; i++) {
            values.add(new Point(i + 1, i + 2));
        }
        SimpleTableModel model = values.parallelStream().collect(SwingStreamUtils.toTableModel(
                new Column<>("col1", p -> p.x, Column.DEFAULT_PREFERRED_WIDTH, Integer.class),
                new Column<>("col2", p -> p.y, Column.DEFAULT_PREFERRED_WIDTH, Integer.class),
                new Column<>("col3", p -> p.x * p.y, Column.DEFAULT_PREFERRED_WIDTH, Integer.class, true)));
        Assertions.assertEquals(values.size(), model.getRowCount());
        Assertions.assertEquals(3, model.getColumnCount());
        Assertions.assertEquals("col1", model.getColumnName(0));
        Assertions.assertEquals("col2", model.getColumnName(1));
        Assertions.assertEquals("col3", model.getColumnName(2));
        for (int i = 0; i < values.size(); i++) {
            Assertions.assertEquals(values.get(i), model.getRowObject(i));
            Assertions.assertEquals(values.get(i).x, model.getValueAt(i, 0));
            Assertions.assertFalse(model.isCellEditable(i, 0));
            Assertions.assertEquals(values.get(i).y, model.getValueAt(i, 1));
            Assertions.assertFalse(model.isCellEditable(i, 1));
            Assertions.assertEquals(values.get(i).x * values.get(i).y, model.getValueAt(i, 2));
            Assertions.assertTrue(model.isCellEditable(i, 2));
        }
    }

    @Test
    void toTableModel_setValueAt() {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            values.add("value " + i);
        }
        SimpleTableModel model = values.stream().collect(SwingStreamUtils.toTableModel(
                new Column<>("col1"),
                new Column<>("col2")));
        List<TableModelEvent> events = new ArrayList<>();
        model.addTableModelListener(events::add);
        final String value = "abc";
        model.setValueAt(value, 0, 1);
        Assertions.assertEquals(value, model.getValueAt(0, 1));
        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals(TableModelEvent.UPDATE, events.get(0).getType());
        Assertions.assertEquals(0, events.get(0).getFirstRow());
        Assertions.assertEquals(0, events.get(0).getLastRow());
        Assertions.assertEquals(1, events.get(0).getColumn());
        Assertions.assertEquals(model, events.get(0).getSource());
    }

    @Test
    void toJTable_parallelStream() throws Exception {
        List<Integer> values = IntStream.range(0, 100_000).mapToObj(Integer::new).collect(Collectors.toList());
        JTable table = values.parallelStream().collect(SwingStreamUtils.toTable(new Column<>("col1")));
        SwingUtilities.invokeAndWait(() -> {
            Assertions.assertEquals(values.size(), table.getRowCount());
            Assertions.assertEquals(1, table.getColumnCount());
            Assertions.assertEquals("col1", table.getColumnName(0));
            for (int i = 0; i < values.size(); i++) {
                Assertions.assertEquals(values.get(i), table.getValueAt(i, 0));
                Assertions.assertEquals(values.get(i), ((SimpleTableModel) table.getModel()).getRowObject(i));
            }
        });
    }

    @Test
    void toJTable_no_columns() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> Arrays.asList("value").stream().collect(SwingStreamUtils.toTable()));
    }

    @Test
    void toJTable_performance_parallel_vs_single() {
        Assertions.assertTrue(performance_toJTable_stream(true) < performance_toJTable_stream(false),
                "Parallel stream must be faster than regular stream.");
    }

    private long performance_toJTable_stream(boolean parallel) {
        final int c = 1_000_000;
        List<Integer> values = IntStream.range(0, c).mapToObj(Integer::new).collect(Collectors.toList());
        final int repeats = 30;
        long totalTime = 0;
        for (int i = 0; i < repeats; i++) {
            long t0 = System.currentTimeMillis();
            JTable table = (parallel ? values.parallelStream() : values.stream()).collect(SwingStreamUtils.toTable(
                    new Column<>("COL1", v -> String.format("Value: %d", v)),
                    new Column<>("COL2", v -> Integer.toHexString(Integer.parseInt(Integer.toString(v + v)))),
                    new Column<>("COL3", v -> Arrays.toString(Integer.toString(v + v).getBytes()))));
            long t = System.currentTimeMillis() - t0;
            totalTime += t;
            Assertions.assertEquals(c, table.getRowCount());
        }
        long aver = totalTime / repeats;
        System.out.println("Aver. (parallel: " + parallel + "): " + aver);
        return aver;
    }

    @Test
    void toJComboBox() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<String> list = Arrays.asList("str1", "str2");
            JComboBox<String> combo = list.stream().collect(SwingStreamUtils.toComboBox());
            Assertions.assertEquals(2, combo.getItemCount());
            Assertions.assertEquals("str1", combo.getItemAt(0));
            Assertions.assertEquals("str2", combo.getItemAt(1));
            Assertions.assertEquals(0, combo.getSelectedIndex());
            Assertions.assertEquals("str1", combo.getSelectedItem());
        });
    }

    @Test
    void toComboBoxModel() {
        List<String> list = new ArrayList<>();
        final int c = 10_000;
        for (int i = 0; i < c; i++) {
            list.add("item_" + i);
        }
        DefaultComboBoxModel<String> model = list.parallelStream().collect(SwingStreamUtils.toComboBoxModel());
        Assertions.assertEquals(c, model.getSize());
        for (int i = 0; i < c; i++) {
            Assertions.assertEquals(list.get(i), model.getElementAt(i));
        }
    }

    @Test
    void toComboBoxModel_2() {
        List<Integer> list = new ArrayList<>();
        final int c = 10_000;
        for (int i = 0; i < c; i++) {
            list.add(i);
        }
        DefaultComboBoxModel<String> model = list.parallelStream()
                .collect(SwingStreamUtils.toComboBoxModel(DefaultComboBoxModel::new,
                        (m, index) -> m.addElement(Integer.toString(index))));
        Assertions.assertEquals(c, model.getSize());
        for (int i = 0; i < c; i++) {
            Assertions.assertEquals(Integer.toString(list.get(i)), model.getElementAt(i));
        }
    }

    @Test
    void getDescendantsIterable_1() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new JPanel();
            List<Component> list = new ArrayList<>();
            SwingStreamUtils.getDescendantsIterable(panel).forEach(list::add);
            Assertions.assertEquals(Arrays.asList(panel), list);
        });
    }

    @Test
    void getDescendantsIterable_2() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            panel.add(label);
            List<Component> list = new ArrayList<>();
            SwingStreamUtils.getDescendantsIterable(panel).forEach(list::add);
            Assertions.assertEquals(Arrays.asList(panel, label), list);
        });
    }

    @Test
    void getDescendantsIterable_3() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new JPanel();
            JLabel label1 = new JLabel();
            JLabel label2 = new JLabel();
            panel.add(label1);
            panel.add(label2);
            List<Component> list = new ArrayList<>();
            SwingStreamUtils.getDescendantsIterable(panel).forEach(list::add);
            Assertions.assertEquals(Arrays.asList(panel, label1, label2), list);
        });
    }

    @Test
    void getDescendantsIterable_4() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new JPanel();
            JPanel panel2 = new JPanel();
            JLabel label1 = new JLabel();
            JLabel label2 = new JLabel();
            panel.add(label1);
            panel.add(panel2);
            panel2.add(label2);
            List<Component> list = new ArrayList<>();
            SwingStreamUtils.getDescendantsIterable(panel).forEach(list::add);
            Assertions.assertEquals(Arrays.asList(panel, label1, panel2, label2), list);
        });
    }

    @Test
    void getDescendantsIterable_5() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new JPanel();
            JPanel panel2 = new JPanel();
            JLabel label1 = new JLabel();
            JLabel label2 = new JLabel();
            JLabel label3 = new JLabel();
            panel.add(label1);
            panel.add(panel2);
            panel2.add(label2);
            panel2.add(label3);
            List<Component> list = new ArrayList<>();
            SwingStreamUtils.getDescendantsIterable(panel).forEach(list::add);
            Assertions.assertEquals(Arrays.asList(panel, label1, panel2, label2, label3), list);
        });
    }

    @Test
    void getDescendantsIterable_6() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new JPanel();
            JPanel panel2 = new JPanel();
            JPanel panel3 = new JPanel();
            JPanel panel4 = new JPanel();
            panel.add(panel2);
            panel2.add(panel3);
            panel3.add(panel4);
            List<Component> list = new ArrayList<>();
            SwingStreamUtils.getDescendantsIterable(panel).forEach(list::add);
            Assertions.assertEquals(Arrays.asList(panel, panel2, panel3, panel4), list);
        });
    }

    @Test
    void getDescendantsIterable_7() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new JPanel();
            Iterator<Component> iterator = SwingStreamUtils.getDescendantsIterable(panel).iterator();
            Assertions.assertTrue(iterator.hasNext());
            Assertions.assertEquals(panel, iterator.next());
            Assertions.assertFalse(iterator.hasNext());
            Assertions.assertThrows(NoSuchElementException.class, () -> iterator.next());
        });
    }

    @Test
    void streamDescendants_1() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new JPanel();
            JPanel panel2 = new JPanel();
            JPanel panel3 = new JPanel();
            JPanel panel4 = new JPanel();
            panel.add(panel2);
            panel2.add(panel3);
            panel3.add(panel4);
            Assertions.assertEquals(Arrays.asList(panel, panel2, panel3, panel4),
                    SwingStreamUtils.streamDescendants(panel).collect(Collectors.toList()));
        });
    }
}
