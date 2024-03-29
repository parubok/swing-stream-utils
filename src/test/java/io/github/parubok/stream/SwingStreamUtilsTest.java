package io.github.parubok.stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
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
import java.util.Vector;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class SwingStreamUtilsTest {

    @Test
    public void asIterable_JTable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DefaultTableModel model = new DefaultTableModel(1, 2);
            model.setColumnIdentifiers(new String[]{"col1", "col2"});
            model.setValueAt("c0", 0, 0);
            model.setValueAt("c1", 0, 1);
            JTable table = new JTable(model);
            Iterable<TableCellData<JTable>> iterable = SwingStreamUtils.asIterable(table);
            Assertions.assertNotNull(iterable);
            Iterator<TableCellData<JTable>> iterator = iterable.iterator();
            Assertions.assertNotNull(iterator);
            Assertions.assertTrue(iterator.hasNext());
            TableCellData<JTable> cell_0 = iterator.next();
            Assertions.assertEquals("col1", cell_0.getColumnName());
            Assertions.assertEquals("c0", cell_0.getValue());
            Assertions.assertEquals(0, cell_0.getRow());
            Assertions.assertEquals(0, cell_0.getColumn());
            Assertions.assertEquals(table, cell_0.getTable());
            Assertions.assertTrue(iterator.hasNext());
            TableCellData<JTable> cell_1 = iterator.next();
            Assertions.assertEquals("col2", cell_1.getColumnName());
            Assertions.assertEquals("c1", cell_1.getValue());
            Assertions.assertEquals(0, cell_1.getRow());
            Assertions.assertEquals(1, cell_1.getColumn());
            Assertions.assertEquals(table, cell_1.getTable());
            Assertions.assertFalse(iterator.hasNext());
        });
    }

    @Test
    public void asIterable_JTable_with_header_0_rows() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DefaultTableModel model = new DefaultTableModel(0, 2);
            model.setColumnIdentifiers(new Vector<>(asList("h1", "h2")));
            JTable table = new JTable(model);
            Iterable<TableCellData<JTable>> iterable = SwingStreamUtils.asIterable(table, true);
            Iterator<TableCellData<JTable>> iterator = iterable.iterator();
            Assertions.assertTrue(iterator.hasNext());
            TableCellData<JTable> h1 = iterator.next();
            Assertions.assertEquals(-1, h1.getRow());
            Assertions.assertEquals(0, h1.getColumn());
            Assertions.assertFalse(h1.isSelected());
            Assertions.assertFalse(h1.isEditable());
            Assertions.assertEquals("h1", h1.getValue());
            Assertions.assertTrue(iterator.hasNext());
            TableCellData<JTable> h2 = iterator.next();
            Assertions.assertEquals(-1, h2.getRow());
            Assertions.assertEquals(1, h2.getColumn());
            Assertions.assertFalse(h2.isSelected());
            Assertions.assertFalse(h2.isEditable());
            Assertions.assertEquals("h2", h2.getValue());
            Assertions.assertFalse(iterator.hasNext());
            Assertions.assertThrows(NoSuchElementException.class, iterator::next);
        });
    }

    @Test
    public void asIterable_JTable_empty_table() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DefaultTableModel model = new DefaultTableModel(0, 0);
            Iterable<TableCellData<JTable>> iterable = SwingStreamUtils.asIterable(new JTable(model));
            Iterator<TableCellData<JTable>> iterator = iterable.iterator();
            Assertions.assertFalse(iterator.hasNext());
            Assertions.assertThrows(NoSuchElementException.class, iterator::next);
        });
    }

    @Test
    public void asIterable_JTable_with_header_1_row() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DefaultTableModel model = new DefaultTableModel(1, 2);
            model.setColumnIdentifiers(new Vector<>(asList("h1", "h2")));
            model.setValueAt("c1", 0, 0);
            model.setValueAt("c2", 0, 1);
            JTable table = new JTable(model);
            Iterable<TableCellData<JTable>> iterable = SwingStreamUtils.asIterable(table, true);
            List<String> values = new ArrayList<>();
            iterable.forEach(c -> values.add((String) c.getValue()));
            Assertions.assertEquals(asList("h1", "h2", "c1", "c2"), values);
            List<Integer> rows = new ArrayList<>();
            iterable.forEach(c -> rows.add(c.getRow()));
            Assertions.assertEquals(asList(-1, -1, 0, 0), rows);
            List<Integer> columns = new ArrayList<>();
            iterable.forEach(c -> columns.add(c.getColumn()));
            Assertions.assertEquals(asList(0, 1, 0, 1), columns);
        });
    }

    @Test
    public void asIterable_JTable_single_cell() throws Exception {
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
    public void asStream_getValue() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TableModel model = new DefaultTableModel(1, 2);
            model.setValueAt("c0", 0, 0);
            model.setValueAt("c1", 0, 1);
            JTable table = new JTable(model);
            Assertions.assertIterableEquals(asList("c0", "c1"), SwingStreamUtils.stream(table)
                    .map(TableCellData::getValue)
                    .collect(Collectors.toList()));
        });
    }

    @Test
    public void asStream_2() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            final int rows = 5;
            final int columns = 3;
            TableModel model = new DefaultTableModel(rows, columns);
            for (int i = 0; i < rows; i++) {
                model.setValueAt(Integer.valueOf(i), i, 1);
            }
            JTable table = new JTable(model);
            Assertions.assertEquals(10, SwingStreamUtils.stream(table)
                    .filter(d -> d.getColumn() == 1)
                    .mapToInt(d -> (Integer) d.getValue())
                    .sum());
        });
    }

    @Test
    public void asStream_3() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            final int rows = 2;
            final int columns = 3;
            TableModel model = new DefaultTableModel(rows, columns);
            JTable table = new JTable(model);
            Assertions.assertEquals(6, SwingStreamUtils.stream(table)
                    .peek(d -> Assertions.assertSame(table, d.getTable()))
                    .peek(d -> Assertions.assertFalse(d.isSelected()))
                    .peek(d -> Assertions.assertTrue(d.isEditable()))
                    .count());
        });
    }

    @Test
    public void asStream_getRow() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            final int rows = 2;
            final int columns = 3;
            TableModel model = new DefaultTableModel(rows, columns);
            JTable table = new JTable(model);
            Assertions.assertIterableEquals(asList(0, 0, 0, 1, 1, 1), SwingStreamUtils.stream(table)
                    .map(d -> d.getRow())
                    .collect(Collectors.toList()));
        });
    }

    @Test
    public void asStream_getColumn() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            final int rows = 2;
            final int columns = 3;
            TableModel model = new DefaultTableModel(rows, columns);
            JTable table = new JTable(model);
            Assertions.assertIterableEquals(asList(0, 1, 2, 0, 1, 2), SwingStreamUtils.stream(table)
                    .map(d -> d.getColumn())
                    .collect(Collectors.toList()));
        });
    }

    @Test
    public void asStream_empty() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TableModel model = new DefaultTableModel(0, 0);
            JTable table = new JTable(model);
            Assertions.assertIterableEquals(Collections.emptyList(), SwingStreamUtils.stream(table)
                    .collect(Collectors.toList()));
        });
    }

    @Test
    public void concurrent_modification_rows() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DefaultTableModel model = new DefaultTableModel(3, 2);
            JTable table = new JTable(model);
            Iterable<TableCellData<JTable>> iterable = SwingStreamUtils.asIterable(table);
            Iterator<TableCellData<JTable>> iterator = iterable.iterator();
            iterator.next();
            model.addRow(new Object[]{"d1", "d2"});
            ConcurrentModificationException ex = Assertions.assertThrows(ConcurrentModificationException.class,
                    () -> iterator.next());
            Assertions.assertEquals("Expected row count: 3, actual row count: 4.", ex.getMessage());
        });
    }

    @Test
    public void toJTable_1() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<String> values = asList("valA", "valB", "valC");
            JTable table = values.stream().collect(SwingStreamUtils.toTable(new ColumnDef<>("col1")));
            Assertions.assertEquals(values.size(), table.getRowCount());
            Assertions.assertEquals(1, table.getColumnCount());
            Assertions.assertEquals("col1", table.getColumnName(0));
            for (int i = 0; i < values.size(); i++) {
                Assertions.assertEquals(values.get(i), table.getValueAt(i, 0));
            }
        });
    }

    @Test
    public void toJTable_2() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<Rectangle> values = asList(new Rectangle(0, 0, 2, 5), new Rectangle(1, 1, 3, 6));
            JTable table = values.stream()
                    .collect(SwingStreamUtils.toTable(new ColumnDef<>("Location", r -> r.getLocation(), 20, Point.class),
                            new ColumnDef<>("Size", r -> r.getSize(), 30, Dimension.class)));
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
    public void toJTable_custom_model() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<String> values = asList("val_1", "val2", "val_3");
            JTable table = values.stream()
                    .collect(SwingStreamUtils.toTable(JTable::new, rowCount -> new DefaultTableModel(rowCount, 2),
                            new ColumnDef<>("C1", Function.identity(), 50, String.class),
                            new ColumnDef<>("C2", String::length, 30, Integer.class)));
            Assertions.assertEquals(values.size(), table.getRowCount());
            Assertions.assertEquals(2, table.getColumnCount());
            Assertions.assertEquals("val_1", table.getValueAt(0, 0));
            Assertions.assertEquals(5, table.getValueAt(0, 1));
            Assertions.assertEquals("val2", table.getValueAt(1, 0));
            Assertions.assertEquals(4, table.getValueAt(1, 1));
        });
    }

    @Test
    public void toJTable_custom_model_with_row_data() throws Exception {
        SwingUtilities.invokeAndWait(() -> {

            List<List> rowObjects = new ArrayList<>();
            class MyModel extends DefaultTableModel implements ObjIntConsumer<String> {
                MyModel(int r, int c) {
                    super(r, c);
                }

                @Override
                public void accept(String s, int value) {
                    rowObjects.add(asList(s, value));
                }
            }

            List<String> values = asList("val_1", "val2", "val_3");
            JTable table = values.stream()
                    .collect(SwingStreamUtils.toTable(JTable::new, rowCount -> new MyModel(rowCount, 2),
                            new ColumnDef<>("C1", Function.identity(), 50, String.class),
                            new ColumnDef<>("C2", String::length, 30, Integer.class)));
            Assertions.assertEquals(values.size(), table.getRowCount());
            Assertions.assertEquals(2, table.getColumnCount());
            Assertions.assertEquals("val_1", table.getValueAt(0, 0));
            Assertions.assertEquals(5, table.getValueAt(0, 1));
            Assertions.assertEquals("val2", table.getValueAt(1, 0));
            Assertions.assertEquals(4, table.getValueAt(1, 1));

            Assertions.assertEquals(values.size(), rowObjects.size());
            Assertions.assertEquals(asList("val_1", 0), rowObjects.get(0));
            Assertions.assertEquals(asList("val2", 1), rowObjects.get(1));
            Assertions.assertEquals(asList("val_3", 2), rowObjects.get(2));
        });
    }

    @Test
    public void toTableModel_1() {
        int size = 100_0000;
        List<Point> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(new Point(i + 1, i + 2));
        }
        SimpleTableModel<Point> model = values.parallelStream().collect(SwingStreamUtils.toTableModel(
                new ColumnDef<>("col1", p -> p.x, ColumnDef.DEFAULT_PREFERRED_WIDTH, Integer.class),
                new ColumnDef<>("col2", p -> p.y, ColumnDef.DEFAULT_PREFERRED_WIDTH, Integer.class),
                new ColumnDef<>("col3", p -> p.x * p.y, ColumnDef.DEFAULT_PREFERRED_WIDTH, Integer.class, true)));
        Assertions.assertEquals(values.size(), model.getRowCount());
        Assertions.assertEquals(3, model.getColumnCount());
        Assertions.assertEquals("col1", model.getColumnName(0));
        Assertions.assertEquals("col2", model.getColumnName(1));
        Assertions.assertEquals("col3", model.getColumnName(2));
        for (int i = 0; i < values.size(); i++) {
            Point rowPoint = model.getRowObject(i);
            Assertions.assertEquals(values.get(i), rowPoint);
            Assertions.assertEquals(values.get(i).x, model.getValueAt(i, 0));
            Assertions.assertFalse(model.isCellEditable(i, 0));
            Assertions.assertEquals(values.get(i).y, model.getValueAt(i, 1));
            Assertions.assertFalse(model.isCellEditable(i, 1));
            Assertions.assertEquals(values.get(i).x * values.get(i).y, model.getValueAt(i, 2));
            Assertions.assertTrue(model.isCellEditable(i, 2));
        }
    }

    @Test
    public void toTableModel_setValueAt() {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            values.add("value " + i);
        }
        SimpleTableModel<String> model = values.stream().collect(SwingStreamUtils.toTableModel(
                new ColumnDef<>("col1"),
                new ColumnDef<>("col2")));
        Assertions.assertEquals("value 0", model.getRowObject(0));
        Assertions.assertEquals("value 1", model.getRowObject(1));
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
    public void toJTable_parallelStream() throws Exception {
        List<Integer> values = IntStream.range(0, 100_000).mapToObj(Integer::new).collect(Collectors.toList());
        JTable table = values.parallelStream().collect(SwingStreamUtils.toTable(new ColumnDef<>("col1")));
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
    public void toJTable_no_columns() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> asList("value").stream().collect(SwingStreamUtils.toTable()));
    }

    @Disabled("Disabled by default for performance reasons.")
    @Test
    public void toJTable_performance_parallel_vs_single() {
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
                    new ColumnDef<>("COL1", v -> String.format("Value: %d", v)),
                    new ColumnDef<>("COL2", v -> Integer.toHexString(Integer.parseInt(Integer.toString(v + v)))),
                    new ColumnDef<>("COL3", v -> Arrays.toString(Integer.toString(v + v).getBytes()))));
            long t = System.currentTimeMillis() - t0;
            totalTime += t;
            Assertions.assertEquals(c, table.getRowCount());
        }
        long aver = totalTime / repeats;
        System.out.println("Aver. (parallel: " + parallel + "): " + aver);
        return aver;
    }

    @Test
    public void toJComboBox() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<String> list = asList("str1", "str2");
            JComboBox<String> combo = list.stream().collect(SwingStreamUtils.toComboBox());
            Assertions.assertEquals(2, combo.getItemCount());
            Assertions.assertEquals("str1", combo.getItemAt(0));
            Assertions.assertEquals("str2", combo.getItemAt(1));
            Assertions.assertEquals(0, combo.getSelectedIndex());
            Assertions.assertEquals("str1", combo.getSelectedItem());
        });
    }

    @Test
    public void toComboBoxModel() {
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
    public void toComboBoxModel_2() {
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
    public void toComboBoxModel_3() {
        List<String> list = asList("str1", "str2", "str3");
        DefaultComboBoxModel<String> model = list.stream()
                .collect(SwingStreamUtils.toComboBoxModel(DefaultComboBoxModel::new,
                        DefaultComboBoxModel::addElement, items -> {
                            Assertions.assertEquals(list, items);
                            return 1;
                        }));
        Assertions.assertEquals(3, model.getSize());
        Assertions.assertEquals("str2", model.getSelectedItem());
    }

    @Test
    public void toComboBoxModel_4() {
        List<String> list = asList("str1", "str2", "str3");
        DefaultComboBoxModel<String> model = list.stream()
                .collect(SwingStreamUtils.toComboBoxModel(DefaultComboBoxModel::new,
                        DefaultComboBoxModel::addElement, items -> {
                            Assertions.assertEquals(list, items);
                            return -1;
                        }));
        Assertions.assertEquals(3, model.getSize());
        Assertions.assertNull(model.getSelectedItem());
    }

    @Test
    public void toComboBoxModel_5() {
        List<String> list = asList("str1", "str2", "str3");
        DefaultComboBoxModel<String> model = list.stream()
                .collect(SwingStreamUtils.toComboBoxModel(DefaultComboBoxModel::new,
                        DefaultComboBoxModel::addElement, null));
        Assertions.assertEquals(3, model.getSize());
        Assertions.assertEquals("str1", model.getSelectedItem());
    }

    /**
     * Types of stream elements and model items are different.
     */
    @Test
    public void toComboBoxModel_6() {
        List<Integer> list = asList(3, 5, 10, 15);
        DefaultComboBoxModel<String> model = list.stream()
                .collect(SwingStreamUtils.toComboBoxModel(DefaultComboBoxModel::new,
                        (m, item) -> m.addElement("str" + item), items -> 2));
        Assertions.assertEquals(4, model.getSize());
        Assertions.assertEquals("str10", model.getSelectedItem());
    }

    /**
     * Invalid selection index.
     */
    @Test
    public void toComboBoxModel_7() {
        List<String> list = asList("str1", "str2");
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.stream()
                .collect(SwingStreamUtils.toComboBoxModel(DefaultComboBoxModel::new,
                        DefaultComboBoxModel::addElement, items -> 200)));
    }

    @Test
    public void getDescendantsIterable_1() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new JPanel();
            List<Component> list = new ArrayList<>();
            SwingStreamUtils.getDescendantsIterable(panel).forEach(list::add);
            Assertions.assertEquals(asList(panel), list);
        });
    }

    @Test
    public void getDescendantsIterable_2() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            panel.add(label);
            List<Component> list = new ArrayList<>();
            SwingStreamUtils.getDescendantsIterable(panel).forEach(list::add);
            Assertions.assertEquals(asList(panel, label), list);
        });
    }

    @Test
    public void getDescendantsIterable_3() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new JPanel();
            JLabel label1 = new JLabel();
            JLabel label2 = new JLabel();
            panel.add(label1);
            panel.add(label2);
            List<Component> list = new ArrayList<>();
            SwingStreamUtils.getDescendantsIterable(panel).forEach(list::add);
            Assertions.assertEquals(asList(panel, label1, label2), list);
        });
    }

    @Test
    public void getDescendantsIterable_4() throws Exception {
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
            Assertions.assertEquals(asList(panel, label1, panel2, label2), list);
        });
    }

    @Test
    public void getDescendantsIterable_5() throws Exception {
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
            Assertions.assertEquals(asList(panel, label1, panel2, label2, label3), list);
        });
    }

    @Test
    public void getDescendantsIterable_6() throws Exception {
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
            Assertions.assertEquals(asList(panel, panel2, panel3, panel4), list);
        });
    }

    @Test
    public void getDescendantsIterable_7() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new JPanel();
            Iterator<Component> iterator = SwingStreamUtils.getDescendantsIterable(panel).iterator();
            Assertions.assertTrue(iterator.hasNext());
            Assertions.assertTrue(iterator.hasNext());
            Assertions.assertEquals(panel, iterator.next());
            Assertions.assertFalse(iterator.hasNext());
            Assertions.assertFalse(iterator.hasNext());
            Assertions.assertThrows(NoSuchElementException.class, () -> iterator.next());
            Assertions.assertFalse(iterator.hasNext()); // second time
            Assertions.assertThrows(NoSuchElementException.class, () -> iterator.next()); // second time
        });
    }

    @Test
    public void streamDescendants_1() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new JPanel();
            JPanel panel2 = new JPanel();
            JPanel panel3 = new JPanel();
            JPanel panel4 = new JPanel();
            panel.add(panel2);
            panel2.add(panel3);
            panel3.add(panel4);
            Assertions.assertEquals(asList(panel, panel2, panel3, panel4),
                    SwingStreamUtils.streamDescendants(panel).collect(Collectors.toList()));
        });
    }

    @Test
    public void streamComboBox_1() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JComboBox<String> combo = new JComboBox<>();
            Assertions.assertEquals(Collections.emptyList(), SwingStreamUtils.stream(combo)
                    .collect(Collectors.toList()));
        });
    }

    @Test
    public void streamComboBox_2() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            String[] s = {"item1", "item2", "item3"};
            ComboBoxModel<String> model = new DefaultComboBoxModel<>(s);
            JComboBox<String> combo = new JComboBox<>(model);
            Assertions.assertEquals(asList(s), SwingStreamUtils.stream(combo)
                    .map(ComboBoxItem::getItem)
                    .collect(Collectors.toList()));
        });
    }

    @Test
    public void streamComboBox_3() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            String[] s = {"item1", "item2", "item3"};
            ComboBoxModel<String> model = new DefaultComboBoxModel<>(s);
            JComboBox<String> combo = new JComboBox<>(model);
            combo.setSelectedIndex(2);
            List<ComboBoxItem<String>> items = SwingStreamUtils.stream(combo)
                    .collect(Collectors.toList());

            Assertions.assertEquals("item2", items.get(1).getItem());
            Assertions.assertEquals(1, items.get(1).getIndex());
            Assertions.assertEquals(3, items.get(1).getModelSize());
            Assertions.assertFalse(items.get(1).isSelected());

            Assertions.assertEquals("item3", items.get(2).getItem());
            Assertions.assertEquals(2, items.get(2).getIndex());
            Assertions.assertEquals(3, items.get(2).getModelSize());
            Assertions.assertTrue(items.get(2).isSelected());
        });
    }

    @Test
    public void comboBoxModelIterable_1() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            String[] s = {"item1", "item2", "item3"};
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(s);
            Iterable<ComboBoxItem<String>> iterable = SwingStreamUtils.asIterable(model);
            Iterator<ComboBoxItem<String>> iterator = iterable.iterator();
            Assertions.assertTrue(iterator.hasNext());
            Assertions.assertEquals("item1", iterator.next().getItem());
            model.addElement("item4");
            Assertions.assertTrue(iterator.hasNext());
            Assertions.assertThrows(ConcurrentModificationException.class, () -> iterator.next());
        });
    }

    @Test
    public void treeModelIterable_no_root() {
        // not EDT
        DefaultTreeModel model = new DefaultTreeModel(null);
        Iterable<KTreePath> iterable = SwingStreamUtils.asIterable(model);
        Assertions.assertNotNull(iterable);
        Iterator<KTreePath> iterator = iterable.iterator();
        Assertions.assertNotNull(iterator);
        Assertions.assertFalse(iterator.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, () -> iterator.next());
    }

    @Test
    public void treeModelIterable_1() {
        // not EDT
        TreeNode root = new DefaultMutableTreeNode("root");
        DefaultTreeModel model = new DefaultTreeModel(root);
        Iterable<KTreePath> iterable = SwingStreamUtils.asIterable(model);
        Assertions.assertNotNull(iterable);
        Iterator<KTreePath> iterator = iterable.iterator();
        Assertions.assertNotNull(iterator);
        Assertions.assertTrue(iterator.hasNext());
        TreePath path = iterator.next();
        Assertions.assertEquals(1, path.getPathCount());
        Assertions.assertEquals(root, path.getLastPathComponent());
        Assertions.assertFalse(iterator.hasNext());
        for (int i = 0; i < 10; i++) {
            Assertions.assertThrows(NoSuchElementException.class, () -> iterator.next());
        }
    }

    @Test
    public void treeModelIterable_2() {
        // not EDT
        TreeNode root = new DefaultMutableTreeNode("root");
        DefaultTreeModel model = new DefaultTreeModel(root);
        Iterable<KTreePath> iterable = SwingStreamUtils.asIterable(model);
        Iterator<KTreePath> iterator = iterable.iterator();
        TreePath path = iterator.next(); // without calling hasNext
        Assertions.assertEquals(1, path.getPathCount());
        Assertions.assertEquals(root, path.getLastPathComponent());
        Assertions.assertFalse(iterator.hasNext());
        for (int i = 0; i < 10; i++) {
            Assertions.assertThrows(NoSuchElementException.class, () -> iterator.next());
        }
    }

    @Test
    public void treeModelIterable_3() {
        // not EDT
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode node_1 = new DefaultMutableTreeNode("node_1");
        DefaultMutableTreeNode node_2_1 = new DefaultMutableTreeNode("node_2_1");
        DefaultMutableTreeNode node_2_2 = new DefaultMutableTreeNode("node_2_2");
        root.add(node_1);
        node_1.add(node_2_1);
        node_1.add(node_2_2);
        DefaultTreeModel model = new DefaultTreeModel(root);
        Iterable<KTreePath> iterable = SwingStreamUtils.asIterable(model);
        Iterator<KTreePath> iterator = iterable.iterator();

        Assertions.assertTrue(iterator.hasNext());
        TreePath path_0 = iterator.next();
        Assertions.assertEquals(1, path_0.getPathCount());
        Assertions.assertEquals(root, path_0.getLastPathComponent());

        Assertions.assertTrue(iterator.hasNext());
        TreePath path_1 = iterator.next();
        Assertions.assertEquals(2, path_1.getPathCount());
        Assertions.assertEquals(root, path_1.getPathComponent(0));
        Assertions.assertEquals(node_1, path_1.getLastPathComponent());

        Assertions.assertTrue(iterator.hasNext());
        TreePath path_2 = iterator.next();
        Assertions.assertEquals(3, path_2.getPathCount());
        Assertions.assertEquals(root, path_2.getPathComponent(0));
        Assertions.assertEquals(node_1, path_2.getPathComponent(1));
        Assertions.assertEquals(node_2_1, path_2.getPathComponent(2));

        Assertions.assertTrue(iterator.hasNext());
        TreePath path_3 = iterator.next();
        Assertions.assertEquals(3, path_3.getPathCount());
        Assertions.assertEquals(root, path_3.getPathComponent(0));
        Assertions.assertEquals(node_1, path_3.getPathComponent(1));
        Assertions.assertEquals(node_2_2, path_3.getPathComponent(2));

        Assertions.assertFalse(iterator.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, () -> iterator.next());
    }

    @Test
    public void treeModelIterable_4() {
        // not EDT
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        List<DefaultMutableTreeNode> children = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode("node_" + i);
            children.add(child);
            root.add(child);
        }
        DefaultTreeModel model = new DefaultTreeModel(root);
        Iterable<KTreePath> iterable = SwingStreamUtils.asIterable(model);
        Iterator<KTreePath> iterator = iterable.iterator();
        List<Object> leaves = new ArrayList<>();
        iterator.next(); // skip root path
        while (iterator.hasNext()) {
            TreePath p = iterator.next();
            Assertions.assertEquals(2, p.getPathCount());
            leaves.add(p.getLastPathComponent());
        }
        Assertions.assertEquals(children, leaves);
        Assertions.assertThrows(NoSuchElementException.class, () -> iterator.next());
    }

    @Test
    public void streamTreeModel_1() {
        // not EDT
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        List<DefaultMutableTreeNode> children = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode("node_" + i);
            children.add(child);
            root.add(child);
        }
        DefaultTreeModel model = new DefaultTreeModel(root);
        Assertions.assertEquals(children, SwingStreamUtils.stream(model)
                .filter(path -> path.getPathCount() > 1)
                .peek(path -> Assertions.assertEquals(root, path.getPathComponent(0)))
                .map(TreePath::getLastPathComponent)
                .collect(Collectors.toList()));
    }

    @Test
    public void streamTreeModel_2() {
        // not EDT
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("c1");
        DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("c2");
        DefaultMutableTreeNode c1_1 = new DefaultMutableTreeNode("c1_1");
        DefaultMutableTreeNode c1_2 = new DefaultMutableTreeNode("c1_2");
        DefaultMutableTreeNode c2_1 = new DefaultMutableTreeNode("c2_1");
        DefaultMutableTreeNode c2_2 = new DefaultMutableTreeNode("c2_2");
        c1.add(c1_1);
        c1.add(c1_2);
        c2.add(c2_1);
        c2.add(c2_2);
        root.add(c1);
        root.add(c2);
        DefaultTreeModel model = new DefaultTreeModel(root);
        Stream<KTreePath> stream = SwingStreamUtils.stream(model);
        Assertions.assertEquals(Arrays.asList(KTreePath.of(root),
                        KTreePath.of(root, c1),
                        KTreePath.of(root, c1, c1_1),
                        KTreePath.of(root, c1, c1_2),
                        KTreePath.of(root, c2),
                        KTreePath.of(root, c2, c2_1),
                        KTreePath.of(root, c2, c2_2)),
                stream.collect(Collectors.toList()));
        Assertions.assertThrows(IllegalStateException.class, stream::count);
    }

    @Test
    public void asIterable_treeModel_postOrder() {
        // not EDT
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("c1");
        DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("c2");
        DefaultMutableTreeNode c1_1 = new DefaultMutableTreeNode("c1_1");
        DefaultMutableTreeNode c1_2 = new DefaultMutableTreeNode("c1_2");
        DefaultMutableTreeNode c2_1 = new DefaultMutableTreeNode("c2_1");
        DefaultMutableTreeNode c2_2 = new DefaultMutableTreeNode("c2_2");
        c1.add(c1_1);
        c1.add(c1_2);
        c2.add(c2_1);
        c2.add(c2_2);
        root.add(c1);
        root.add(c2);
        DefaultTreeModel model = new DefaultTreeModel(root);
        Iterable<KTreePath> iterable = SwingStreamUtils.asIterable(model, TreeTraversalType.POST_ORDER);
        Iterator<KTreePath> iterator = iterable.iterator();
        Assertions.assertEquals(KTreePath.of(root, c1, c1_1), iterator.next());
        Assertions.assertEquals(KTreePath.of(root, c1, c1_2), iterator.next());
        Assertions.assertEquals(KTreePath.of(root, c1), iterator.next());
        Assertions.assertEquals(KTreePath.of(root, c2, c2_1), iterator.next());
        Assertions.assertEquals(KTreePath.of(root, c2, c2_2), iterator.next());
        Assertions.assertEquals(KTreePath.of(root, c2), iterator.next());
        Assertions.assertEquals(KTreePath.of(root), iterator.next());
        Assertions.assertFalse(iterator.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void asIterable_treeModel_postOrder_2() {
        // not EDT
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("c1");
        DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("c2");
        DefaultMutableTreeNode c1_1 = new DefaultMutableTreeNode("c1_1");
        DefaultMutableTreeNode c1_2 = new DefaultMutableTreeNode("c1_2");
        DefaultMutableTreeNode c2_1 = new DefaultMutableTreeNode("c2_1");
        DefaultMutableTreeNode c2_2 = new DefaultMutableTreeNode("c2_2");
        c1.add(c1_1);
        c1.add(c1_2);
        c2.add(c2_1);
        c2.add(c2_2);
        root.add(c1);
        root.add(c2);
        DefaultTreeModel model = new DefaultTreeModel(root);
        Iterable<KTreePath> iterable = SwingStreamUtils.asIterable(model, TreeTraversalType.POST_ORDER);
        Iterator<KTreePath> iterator = iterable.iterator();
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c1, c1_1), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c1, c1_2), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c1), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c2, c2_1), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c2, c2_2), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c2), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root), iterator.next());
        Assertions.assertFalse(iterator.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void asIterable_treeModel_postOrder_rootOnly() {
        // not EDT
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultTreeModel model = new DefaultTreeModel(root);
        Iterable<KTreePath> iterable = SwingStreamUtils.asIterable(model, TreeTraversalType.POST_ORDER);
        Iterator<KTreePath> iterator = iterable.iterator();
        Assertions.assertNotNull(iterator);
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(iterator.hasNext());
        }
        Assertions.assertEquals(KTreePath.of(root), iterator.next());
        for (int i = 0; i < 10; i++) {
            Assertions.assertFalse(iterator.hasNext());
            Assertions.assertThrows(NoSuchElementException.class, iterator::next);
        }
    }

    @Test
    public void asIterable_treeModel_postOrder_rootOnly_2() {
        // not EDT
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultTreeModel model = new DefaultTreeModel(root);
        Iterable<KTreePath> iterable = SwingStreamUtils.asIterable(model, TreeTraversalType.POST_ORDER);
        Iterator<KTreePath> iterator = iterable.iterator();
        Assertions.assertNotNull(iterator);
        Assertions.assertEquals(KTreePath.of(root), iterator.next());
        Assertions.assertFalse(iterator.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void asIterable_treeModel_preOrder_rootOnly() {
        // not EDT
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultTreeModel model = new DefaultTreeModel(root);
        Iterable<KTreePath> iterable = SwingStreamUtils.asIterable(model, TreeTraversalType.PRE_ORDER);
        Iterator<KTreePath> iterator = iterable.iterator();
        Assertions.assertNotNull(iterator);
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(iterator.hasNext());
        }
        Assertions.assertEquals(KTreePath.of(root), iterator.next());
        for (int i = 0; i < 10; i++) {
            Assertions.assertFalse(iterator.hasNext());
            Assertions.assertThrows(NoSuchElementException.class, iterator::next);
        }
    }

    @Test
    public void asIterable_treeModel_postOrder_4() {
        // not EDT
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("c1");
        DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("c2");
        DefaultMutableTreeNode c1_1 = new DefaultMutableTreeNode("c1_1");
        DefaultMutableTreeNode c1_2 = new DefaultMutableTreeNode("c1_2");
        DefaultMutableTreeNode c1_2_1 = new DefaultMutableTreeNode("c1_2_1");
        DefaultMutableTreeNode c2_1 = new DefaultMutableTreeNode("c2_1");
        DefaultMutableTreeNode c2_2 = new DefaultMutableTreeNode("c2_2");
        DefaultMutableTreeNode c2_2_1 = new DefaultMutableTreeNode("c2_2_1");
        DefaultMutableTreeNode c2_2_1_1 = new DefaultMutableTreeNode("c2_2_1_1");
        DefaultMutableTreeNode c2_3 = new DefaultMutableTreeNode("c2_3");
        c1.add(c1_1);
        c1.add(c1_2);
        c1_2.add(c1_2_1);
        c2.add(c2_1);
        c2.add(c2_2);
        c2_2.add(c2_2_1);
        c2_2_1.add(c2_2_1_1);
        c2.add(c2_3);
        root.add(c1);
        root.add(c2);
        DefaultTreeModel model = new DefaultTreeModel(root);
        Iterable<KTreePath> iterable = SwingStreamUtils.asIterable(model, TreeTraversalType.POST_ORDER);
        Iterator<KTreePath> iterator = iterable.iterator();
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c1, c1_1), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c1, c1_2, c1_2_1), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c1, c1_2), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c1), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c2, c2_1), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c2, c2_2, c2_2_1, c2_2_1_1), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c2, c2_2, c2_2_1), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c2, c2_2), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c2, c2_3), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root, c2), iterator.next());
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(KTreePath.of(root), iterator.next());
        Assertions.assertFalse(iterator.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void asIterable_treeModel_postOrder_5() {
        // not EDT
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        final int c = 100;
        for (int i = 0; i < c; i++) {
            root.add(new DefaultMutableTreeNode("c" + i));
        }
        DefaultTreeModel model = new DefaultTreeModel(root);
        Iterable<KTreePath> iterable = SwingStreamUtils.asIterable(model, TreeTraversalType.POST_ORDER);
        List<KTreePath> list = new ArrayList<>();
        for (KTreePath p : iterable) {
            list.add(p);
        }
        Assertions.assertEquals(101, list.size());
        for (int i = 0; i < c; i++) {
            Assertions.assertEquals(KTreePath.of(root, root.getChildAt(i)), list.get(i));
        }
        Assertions.assertEquals(KTreePath.of(root), list.get(list.size() - 1));
    }

    @Test
    public void stream_treeModel_postOrder() {
        // not EDT
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("c1");
        DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("c2");
        DefaultMutableTreeNode c1_1 = new DefaultMutableTreeNode("c1_1");
        DefaultMutableTreeNode c1_2 = new DefaultMutableTreeNode("c1_2");
        DefaultMutableTreeNode c1_2_1 = new DefaultMutableTreeNode("c1_2_1");
        DefaultMutableTreeNode c2_1 = new DefaultMutableTreeNode("c2_1");
        DefaultMutableTreeNode c2_2 = new DefaultMutableTreeNode("c2_2");
        DefaultMutableTreeNode c2_2_1 = new DefaultMutableTreeNode("c2_2_1");
        DefaultMutableTreeNode c2_2_1_1 = new DefaultMutableTreeNode("c2_2_1_1");
        DefaultMutableTreeNode c2_3 = new DefaultMutableTreeNode("c2_3");
        c1.add(c1_1);
        c1.add(c1_2);
        c1_2.add(c1_2_1);
        c2.add(c2_1);
        c2.add(c2_2);
        c2_2.add(c2_2_1);
        c2_2_1.add(c2_2_1_1);
        c2.add(c2_3);
        root.add(c1);
        root.add(c2);
        DefaultTreeModel model = new DefaultTreeModel(root);
        Stream<KTreePath> stream = SwingStreamUtils.stream(model, TreeTraversalType.POST_ORDER);
        Assertions.assertNotNull(stream);
        Assertions.assertFalse(stream.isParallel());
        List<KTreePath> paths = stream
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(KTreePath.of(root, c1, c1_1),
                KTreePath.of(root, c1, c1_2, c1_2_1),
                KTreePath.of(root, c1, c1_2),
                KTreePath.of(root, c1),
                KTreePath.of(root, c2, c2_1),
                KTreePath.of(root, c2, c2_2, c2_2_1, c2_2_1_1),
                KTreePath.of(root, c2, c2_2, c2_2_1),
                KTreePath.of(root, c2, c2_2),
                KTreePath.of(root, c2, c2_3),
                KTreePath.of(root, c2),
                KTreePath.of(root)), paths);
    }

    @Test
    public void stream_treeModel_preOrder() {
        // not EDT
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("c1");
        DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("c2");
        DefaultMutableTreeNode c1_1 = new DefaultMutableTreeNode("c1_1");
        DefaultMutableTreeNode c1_2 = new DefaultMutableTreeNode("c1_2");
        DefaultMutableTreeNode c1_2_1 = new DefaultMutableTreeNode("c1_2_1");
        DefaultMutableTreeNode c2_1 = new DefaultMutableTreeNode("c2_1");
        DefaultMutableTreeNode c2_2 = new DefaultMutableTreeNode("c2_2");
        DefaultMutableTreeNode c2_2_1 = new DefaultMutableTreeNode("c2_2_1");
        DefaultMutableTreeNode c2_2_1_1 = new DefaultMutableTreeNode("c2_2_1_1");
        DefaultMutableTreeNode c2_3 = new DefaultMutableTreeNode("c2_3");
        c1.add(c1_1);
        c1.add(c1_2);
        c1_2.add(c1_2_1);
        c2.add(c2_1);
        c2.add(c2_2);
        c2_2.add(c2_2_1);
        c2_2_1.add(c2_2_1_1);
        c2.add(c2_3);
        root.add(c1);
        root.add(c2);
        DefaultTreeModel model = new DefaultTreeModel(root);
        List<KTreePath> paths = SwingStreamUtils.stream(model, TreeTraversalType.PRE_ORDER)
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(KTreePath.of(root),
                KTreePath.of(root, c1),
                KTreePath.of(root, c1, c1_1),
                KTreePath.of(root, c1, c1_2),
                KTreePath.of(root, c1, c1_2, c1_2_1),
                KTreePath.of(root, c2),
                KTreePath.of(root, c2, c2_1),
                KTreePath.of(root, c2, c2_2),
                KTreePath.of(root, c2, c2_2, c2_2_1),
                KTreePath.of(root, c2, c2_2, c2_2_1, c2_2_1_1),
                KTreePath.of(root, c2, c2_3)), paths);
    }

    @Test
    public void streamTree_1() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TreeNode root = new DefaultMutableTreeNode("root");
            DefaultTreeModel model = new DefaultTreeModel(root);
            JTree tree = new JTree(model);
            Assertions.assertEquals(singletonList(new TreePath(root)), SwingStreamUtils.stream(tree)
                    .collect(Collectors.toList()));
        });
    }

    @Test
    public void streamTreeStructure_1() {
        // not EDT
        TreeStructure treeStructure = new TreeStructure() {
            @Override
            public Object getRoot() {
                return "root";
            }

            @Override
            public Object getChild(Object parent, int index) {
                Assertions.fail();
                return null;
            }

            @Override
            public int getChildCount(Object parent) {
                Assertions.assertEquals(getRoot(), parent);
                return 0;
            }
        };
        Stream<KTreePath> stream = SwingStreamUtils.stream(treeStructure);
        Assertions.assertNotNull(stream);
        Assertions.assertEquals(singletonList(KTreePath.of("root")), stream.collect(Collectors.toList()));
    }

    @Test
    public void streamTreeStructure_2() {
        // not EDT
        TreeStructure s = new TreeStructure() {
            @Override
            public Object getRoot() {
                return "root";
            }

            @Override
            public Object getChild(Object parent, int index) {
                Assertions.assertEquals(getRoot(), parent);
                Assertions.assertEquals(0, index);
                return "child";
            }

            @Override
            public int getChildCount(Object parent) {
                if (parent.equals(getRoot())) {
                    return 1;
                } else {
                    Assertions.assertEquals("child", parent);
                    return 0;
                }
            }
        };
        Assertions.assertEquals(asList(KTreePath.of("root"), KTreePath.of("root", "child")),
                SwingStreamUtils.stream(s).collect(Collectors.toList()));
    }

    @Test
    public void modifyingTreeModelDuringIterationShouldResultInConcurrentModificationException() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
            DefaultTreeModel model = new DefaultTreeModel(root);
            Iterator<KTreePath> iterator = SwingStreamUtils.asIterable(model).iterator();
            model.insertNodeInto(new DefaultMutableTreeNode("child"), root, 0);
            // now the iterator is unusable:
            Assertions.assertThrows(ConcurrentModificationException.class, iterator::hasNext);
            Assertions.assertThrows(ConcurrentModificationException.class, iterator::next);

            // a new iterator will work just fine:
            iterator = SwingStreamUtils.asIterable(model).iterator();
            Assertions.assertTrue(iterator.hasNext());
            Assertions.assertEquals(KTreePath.of(root), iterator.next());
            // until we modify the model:
            model.insertNodeInto(new DefaultMutableTreeNode("child2"), root, 1);
            Assertions.assertThrows(ConcurrentModificationException.class, iterator::hasNext);
            Assertions.assertThrows(ConcurrentModificationException.class, iterator::next);
        });
    }

    @Test
    public void modifyingTreeModelAfterIteration() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
            DefaultTreeModel model = new DefaultTreeModel(root);
            Iterator<KTreePath> iterator = SwingStreamUtils.asIterable(model).iterator();
            while (iterator.hasNext()) {
                iterator.next();
            }
            model.insertNodeInto(new DefaultMutableTreeNode("child"), root, 0);
            Assertions.assertFalse(iterator.hasNext());
        });
    }

    @Test
    public void treeModelListeners() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<TreeModelListener> listeners = new ArrayList<>();
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
            DefaultTreeModel model = new DefaultTreeModel(root) {
                @Override
                public void addTreeModelListener(TreeModelListener l) {
                    listeners.add(l);
                    super.addTreeModelListener(l);
                }

                @Override
                public void removeTreeModelListener(TreeModelListener l) {
                    listeners.remove(l);
                    super.removeTreeModelListener(l);
                }
            };
            Assertions.assertEquals(0, listeners.size());
            Iterator<KTreePath> iterator = SwingStreamUtils.asIterable(model).iterator();
            Assertions.assertEquals(1, listeners.size()); // must be removed after iteration
            while (iterator.hasNext()) {
                iterator.next();
            }
            Assertions.assertEquals(0, listeners.size());

            // without modification check - no listener is added
            iterator = SwingStreamUtils.asIterable(model, SwingStreamUtils.DEFAULT_TREE_TRAVERSAL_TYPE, false).iterator();
            Assertions.assertEquals(0, listeners.size());
        });
    }
}
