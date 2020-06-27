package org.swingk.utils.table;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utils to iterate over cells of {@link JTable}.
 * Must be invoked on EDT.
 */
public class TableStreamUtils {

    private TableStreamUtils() {
    }

    /**
     * Note: This method assumes that the table does not change its number of rows and columns during the iteration.
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
     * Note: This method assumes that the table does not change its number of rows and columns during the streaming.
     *
     * @param table Table which cells will be streamed. Not null.
     * @param <T>   Type of the table.
     * @return Stream of {@link TableCellData} for the provided table.
     * @see #asIterable(JTable)
     */
    public static <T extends JTable> Stream<TableCellData<T>> asStream(T table) {
        return StreamSupport.stream(asIterable(table).spliterator(), false);
    }

    public static <T> Collector<T, DefaultTableModel, JTable> toJTable(String columnNameA, Function<T, Object> columnValuesA) {
        return toJTable(JTable.class, columnNameA, columnValuesA);
    }

    public static <T, K extends JTable> Collector<T, DefaultTableModel, K> toJTable(Class<K> tableClass, String columnNameA, Function<T, Object> columnValuesA) {
        return new Collector<T, DefaultTableModel, K>() {

            @Override
            public Supplier<DefaultTableModel> supplier() {
                return () -> new DefaultTableModel(0, 1);
            }

            @Override
            public BiConsumer<DefaultTableModel, T> accumulator() {
                return (model, val) -> model.addRow(new Object[]{columnValuesA.apply(val)});
            }

            @Override
            public BinaryOperator<DefaultTableModel> combiner() {
                return (m1, m2) -> {
                    Vector newData = new Vector(m1.getRowCount() + m2.getRowCount());
                    newData.addAll(m1.getDataVector());
                    newData.addAll(m2.getDataVector());
                    DefaultTableModel combinedModel = new DefaultTableModel(0, m1.getColumnCount());
                    combinedModel.setDataVector(newData, null);
                    return combinedModel;
                };
            }

            @Override
            public Function<DefaultTableModel, K> finisher() {
                return model -> {
                    final AtomicReference<K> tableRef = new AtomicReference<>();
                    try {
                        Runnable finisherTask = () -> {
                            model.setColumnIdentifiers(new Object[]{columnNameA});
                            try {
                                tableRef.set(tableClass.newInstance());
                            } catch (InstantiationException | IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                            tableRef.get().setModel(model);
                        };
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

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
    }
}
