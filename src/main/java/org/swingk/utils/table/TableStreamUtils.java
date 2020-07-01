package org.swingk.utils.table;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Java-8 stream utils for Swing {@link JTable}.
 */
public class TableStreamUtils {

    private TableStreamUtils() {
    }

    /**
     * Note: The iteration order is from left to right, from top to bottom.
     *
     * @param table Table which cells to iterate. Not null.
     * @param <T>   Type of the table.
     * @return Cell iterator for the provided table.
     * @throws IllegalArgumentException If the table is null.
     */
    public static <T extends JTable> Iterable<TableCellData<T>> asIterable(T table) {
        Objects.requireNonNull(table, "table");
        return () -> {
            final int lastRow = table.getRowCount() - 1;
            final int lastColumn = table.getColumnCount() - 1;

            if (lastRow < 0 || lastColumn < 0) {
                return Collections.emptyIterator();
            }

            return new Iterator<TableCellData<T>>() {

                private int row = 0;
                private int column = 0;
                private boolean hasMoreCells = true;

                private void checkForConcurrentModification() {
                    int tableRowCount = table.getRowCount();
                    int tableColumnCount = table.getColumnCount();
                    if (lastRow != (tableRowCount - 1)) {
                        throw new ConcurrentModificationException("Expected row count: " + (lastRow + 1)
                                + ", actual row count: " + tableRowCount + ".");
                    }
                    if (lastColumn != (tableColumnCount - 1)) {
                        throw new ConcurrentModificationException("Expected column count: " + (lastColumn + 1)
                                + ", actual column count: " + tableColumnCount + ".");
                    }
                }

                @Override
                public boolean hasNext() {
                    return hasMoreCells;
                }

                @Override
                public TableCellData<T> next() {
                    checkForConcurrentModification();
                    Object value = table.getValueAt(row, column);
                    TableCellData<T> cellData = new TableCellData<>(row, column, value, table);
                    if (column < lastColumn) {
                        column++;
                    } else if (row < lastRow) {
                        row++;
                        column = 0;
                    } else {
                        hasMoreCells = false;
                    }
                    return cellData;
                }
            };
        };
    }

    /**
     * Must be invoked on EDT.
     * The table traversal order is from left to right, from top to bottom.
     *
     * @param table Table which cells will be streamed. Not null.
     * @param <T>   Type of the table.
     * @return Stream of {@link TableCellData} for the provided table.
     * @see #asIterable(JTable)
     */
    public static <T extends JTable> Stream<TableCellData<T>> asStream(T table) {
        return StreamSupport.stream(asIterable(table).spliterator(), false);
    }

    /**
     * @see #toJTable(Supplier, Column[])
     */
    public static <T> Collector<T, List<List<Object>>, JTable> toJTable(Column<T>... columns) {
        return toJTable(JTable::new, columns);
    }

    /**
     * Collector for Java 8 streams to create {@link JTable} (an element from the stream produces a single table row).
     * <p>
     * <b>Note 1:</b> The collector ensures that the table component is created/accessed on EDT even if the streaming
     * is performed on a different thread (e.g. parallel stream).
     * </p>
     * <p>
     * <b>Note 2:</b> Model of the resulting {@link JTable} is instance of {@link SimpleTableModel}.
     * </p>
     *
     * @param tableSupplier Creates a concrete instance of {@link JTable} for the collector. Called on EDT.
     * @param columns The table column descriptors.
     * @param <T> Type of stream elements.
     * @return The new table.
     */
    public static <T, K extends JTable> Collector<T, List<List<Object>>, K> toJTable(Supplier<K> tableSupplier,
                                                                                     Column<T>... columns) {
        Objects.requireNonNull(tableSupplier);
        return new AbstractCollector<T, K>(columns) {
            @Override
            public Function<List<List<Object>>, K> finisher() {
                return data -> {
                    TableModel model = createModel(data, columns);
                    final AtomicReference<K> tableRef = new AtomicReference<>();
                    try {
                        Runnable finisherTask = () -> {
                            K table = Objects.requireNonNull(tableSupplier.get(), "table");
                            table.setModel(model);
                            for (int i = 0; i < columns.length; i++) {
                                TableColumn tableColumn = table.getColumnModel().getColumn(i);
                                tableColumn.setPreferredWidth(columns[i].getPreferredWidth());
                            }
                            tableRef.set(table);
                        };
                        // Swing components must be created/accessed on EDT:
                        if (SwingUtilities.isEventDispatchThread()) {
                            finisherTask.run();
                        } else {
                            SwingUtilities.invokeAndWait(finisherTask);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e.getCause());
                    }
                    return tableRef.get();
                };
            }
        };
    }

    private static <T> SimpleTableModel createModel(List<List<Object>> data, Column<T>[] columns) {
        List<Class<?>> columnClasses = new ArrayList<>(columns.length);
        List<String> columnNames = new ArrayList<>(columns.length);
        boolean[] editable = new boolean[columns.length];
        for (int i = 0; i < columns.length; i++) {
            columnNames.add(columns[i].getName());
            columnClasses.add(columns[i].getColumnClass());
            editable[i] = columns[i].isEditable();
        }
        return new SimpleTableModel(data, columns.length, columnClasses, columnNames, editable);
    }

    /**
     * Collector for Java 8 streams to create {@link SimpleTableModel} (an element from the stream produces a single
     * table row).
     *
     * @param columns The table column descriptors (column preferred width is ignored).
     * @param <T> Type of stream elements.
     * @return The table model.
     */
    public static <T> Collector<T, List<List<Object>>, SimpleTableModel> toTableModel(Column<T>... columns) {
        return new AbstractCollector<T, SimpleTableModel>(columns) {
            @Override
            public Function<List<List<Object>>, SimpleTableModel> finisher() {
                return data -> createModel(data, columns);
            }
        };
    }
}
