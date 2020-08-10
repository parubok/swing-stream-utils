package org.swingk.utils.stream;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;

/**
 * Java-8 stream utils for Swing components.
 */
public class SwingStreamUtils {

    private SwingStreamUtils() {
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
                    if (!hasMoreCells) {
                        throw new NoSuchElementException();
                    }
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
     * @see #toTable(Supplier, Column[])
     */
    public static <T> Collector<T, List<List<Object>>, JTable> toTable(Column<T>... columns) {
        return toTable(JTable::new, columns);
    }

    /**
     * Stream collector to create {@link JTable} (an element from the stream produces a single table row,
     * the corresponding element may be retrieved via {@link SimpleTableModel#getRowObject(int)).
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
     * @param <T> Type of the stream elements.
     * @return The new table.
     */
    public static <T, K extends JTable> Collector<T, List<List<Object>>, K> toTable(Supplier<K> tableSupplier,
                                                                                    Column<T>... columns) {
        Objects.requireNonNull(tableSupplier);
        if (columns.length == 0) {
            throw new IllegalArgumentException("Columns must be specified");
        }
        return new AbstractCollector<T, K>(columns) {
            @Override
            public Function<List<List<Object>>, K> finisher() {
                return data -> {
                    SimpleTableModel<T> model = createSimpleModel(data, columns);
                    return finishToTable(tableSupplier, model, columns);
                };
            }
        };
    }

    private static <K extends JTable> K finishToTable(Supplier<K> tableSupplier, TableModel model,
                                                      Column<?>... columns) {
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
    }

    /**
     * Stream collector to create {@link JTable} (an element from the stream produces a single table row).
     * This collector accepts a supplier to create model of the resulting table.
     * <p>
     * <b>Note 1:</b> The collector ensures that the table component is created/accessed on EDT even if the streaming
     * is performed on a different thread (e.g. parallel stream).
     * </p>
     * <p>
     * <b>Note 2:</b> If the supplied model implements {@link ObjIntConsumer<T>}, method
     * {@link ObjIntConsumer#accept(T, int)} will be called for each stream element and its row index.
     * </p>
     *
     * @param tableSupplier Creates a concrete instance of {@link JTable} for the collector. Called on EDT.
     * @param modelSupplier Creates a concrete instance of {@link TableModel} for the collector. Receives number of
     *                      rows in the model. Should produce model with the correct number of rows and columns.
     *                      Called on the current thread.
     * @param columns The table column descriptors.
     * @param <T> Type of the stream elements.
     * @return The new table.
     */
    public static <T, K extends JTable, M extends TableModel> Collector<T, List<List<Object>>, K> toTable(Supplier<K> tableSupplier,
                                                                                                          IntFunction<M> modelSupplier,
                                                                                                          Column<T>... columns) {
        Objects.requireNonNull(tableSupplier);
        Objects.requireNonNull(modelSupplier);
        if (columns.length == 0) {
            throw new IllegalArgumentException("Columns must be specified");
        }
        return new AbstractCollector<T, K>(columns) {
            @Override
            public Function<List<List<Object>>, K> finisher() {
                return data -> {
                    final int rowCount = data.size();
                    M model = Objects.requireNonNull(modelSupplier.apply(rowCount), "model");
                    if (model.getRowCount() != rowCount) {
                        throw new RuntimeException("Expected number of rows: " + rowCount + ", actual: "
                                + model.getRowCount() + ".");
                    }
                    if (model.getColumnCount() != columns.length) {
                        throw new RuntimeException("Expected number of columns: " + columns.length +
                                ", actual: " + model.getColumnCount() + ".");
                    }
                    for (int row = 0; row < rowCount; row++) {
                        for (int column = 0; column < columns.length; column++) {
                            model.setValueAt(data.get(row).get(column), row, column);
                        }
                        if (model instanceof ObjIntConsumer) {
                            ((ObjIntConsumer) model).accept(data.get(row).get(columns.length), row);
                        }
                    }
                    return finishToTable(tableSupplier, model, columns);
                };
            }
        };
    }

    private static <T> SimpleTableModel<T> createSimpleModel(List<List<Object>> data, Column<T>[] columns) {
        List<Class<?>> columnClasses = new ArrayList<>(columns.length);
        List<String> columnNames = new ArrayList<>(columns.length);
        boolean[] editable = new boolean[columns.length];
        for (int i = 0; i < columns.length; i++) {
            columnNames.add(columns[i].getName());
            columnClasses.add(columns[i].getColumnClass());
            editable[i] = columns[i].isEditable();
        }
        return new SimpleTableModel<>(data, columnClasses, columnNames, editable);
    }

    /**
     * Stream collector to create {@link SimpleTableModel} (an element from the stream produces a single
     * table row, the corresponding element may be retrieved via {@link SimpleTableModel#getRowObject(int)}).
     *
     * @param columns The table column descriptors (column preferred width is ignored).
     * @param <T> Type of stream elements.
     * @return The table model.
     */
    public static <T> Collector<T, List<List<Object>>, SimpleTableModel<T>> toTableModel(Column<T>... columns) {
        if (columns.length == 0) {
            throw new IllegalArgumentException("Columns must be specified");
        }
        return new AbstractCollector<T, SimpleTableModel<T>>(columns) {
            @Override
            public Function<List<List<Object>>, SimpleTableModel<T>> finisher() {
                return data -> createSimpleModel(data, columns);
            }
        };
    }

    /**
     * Stream collector to create vanilla {@link JComboBox} with {@link DefaultComboBoxModel}.
     *
     * @see #toComboBox(Supplier, Supplier, BiConsumer)
     */
    public static <T> Collector<T, List<T>, JComboBox<T>> toComboBox() {
        return toComboBox(JComboBox::new, DefaultComboBoxModel::new, DefaultComboBoxModel::addElement);
    }

    /**
     * Stream collector to create {@link JComboBox}.
     * <p>
     * <b>Note:</b> The collector ensures that the combo box component is created/accessed on EDT even if the
     * streaming is performed on a different thread (e.g. parallel stream). The model supplier is called on the current
     * thread.
     * </p>
     *
     * @param comboSupplier Creates a concrete instance of {@link JComboBox} for the collector. Called on EDT.
     * @param modelSupplier Creates a concrete instance of {@link ComboBoxModel} for the collector. Called on the current thread.
     * @param itemAdder Adds item to the model. Called on the current thread.
     * @param <T> Type of the stream elements.
     * @param <D> Type of the resulting combo box items.
     * @param <K> Type of the resulting combo box.
     * @param <M> Type of the combo box model.
     * @return The new combo box.
     */
    public static <T, D, K extends JComboBox<D>, M extends ComboBoxModel<D>> Collector<T, List<T>, K> toComboBox(Supplier<K> comboSupplier,
                                                                                                                 Supplier<M> modelSupplier,
                                                                                                                 BiConsumer<M, T> itemAdder) {
        Objects.requireNonNull(comboSupplier);
        Objects.requireNonNull(modelSupplier);
        Objects.requireNonNull(itemAdder);
        return new Collector<T, List<T>, K>() {
            @Override
            public Supplier<List<T>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<List<T>, T> accumulator() {
                return List::add;
            }

            @Override
            public BinaryOperator<List<T>> combiner() {
                return CombinedList::new;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }

            @Override
            public Function<List<T>, K> finisher() {
                return data -> {
                    final M model = Objects.requireNonNull(modelSupplier.get(), "model");
                    data.forEach(item -> itemAdder.accept(model, item));
                    final AtomicReference<K> comboRef = new AtomicReference<>();
                    try {
                        Runnable finisherTask = () -> {
                            K combo = Objects.requireNonNull(comboSupplier.get(), "combo box");
                            combo.setModel(model);
                            comboRef.set(combo);
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
                    return comboRef.get();
                };
            }
        };
    }

    /**
     * Stream collector to create vanilla {@link DefaultComboBoxModel}.
     *
     * @see #toComboBoxModel(Supplier, BiConsumer)
     */
    public static <T> Collector<T, List<T>, DefaultComboBoxModel<T>> toComboBoxModel() {
        return toComboBoxModel(DefaultComboBoxModel::new, DefaultComboBoxModel::addElement);
    }

    /**
     * Stream collector to create {@link ComboBoxModel}. Works on the current thread.
     *
     * @param modelSupplier Creates a concrete instance of {@link ComboBoxModel} for the collector.
     * @param itemAdder Adds item to the model.
     * @param <T> Type of the stream elements.
     * @param <D> Type of the resulting combo box model items.
     * @param <M> Type of the combo box model.
     * @return The new combo box model.
     */
    public static <T, D, M extends ComboBoxModel<D>> Collector<T, List<T>, M> toComboBoxModel(Supplier<M> modelSupplier,
                                                                                              BiConsumer<M, T> itemAdder) {
        Objects.requireNonNull(modelSupplier);
        Objects.requireNonNull(itemAdder);
        return new Collector<T, List<T>, M>() {
            @Override
            public Supplier<List<T>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<List<T>, T> accumulator() {
                return List::add;
            }

            @Override
            public BinaryOperator<List<T>> combiner() {
                return CombinedList::new;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }

            @Override
            public Function<List<T>, M> finisher() {
                return data -> {
                    final M model = Objects.requireNonNull(modelSupplier.get(), "model");
                    data.forEach(item -> itemAdder.accept(model, item));
                    return model;
                };
            }
        };
    }

    /**
     * Must be invoked on EDT. The component hierarchy should not change during iteration.
     *
     * @param parent Parent component. Not null.
     * @return Iterable which iterates over all descendant components in the parent component (incl. the parent itself).
     * First returned item of the iterable is the root parent.
     * @see Container#getComponentCount()
     * @see Container#getComponent(int)
     */
    public static Iterable<Component> getDescendantsIterable(Component parent) {
        Objects.requireNonNull(parent);
        return () -> new Iterator<Component>() {

            /**
             * Path (in components tree) which last element was returned by the last call to {@link #next()}.
             * Never null. Initially empty.
             */
            private List<Component> currentPath = Collections.emptyList();

            /**
             * Stores next path (relative to the current path) to prevent multiple calls to {@link #getNextPath()} for
             * the same current path.
             */
            private List<Component> nextPath;

            private boolean completed;

            /**
             * Note: Should not modify {@code currentPath}.
             *
             * @return Next path relative to the current path or empty path if the iteration is completed and there is
             * no next path.
             */
            private List<Component> getNextPath() {
                assert !completed;
                if (currentPath.isEmpty()) {
                    return Collections.singletonList(parent); // start iteration with the root parent path
                }
                // try to go down first:
                Component com = currentPath.get(currentPath.size() - 1);
                if (com instanceof Container) {
                    Container container = (Container) com;
                    if (container.getComponentCount() > 0) {
                        List<Component> nextPath = new ArrayList<>(currentPath);
                        nextPath.add(container.getComponent(0));
                        return nextPath;
                    }
                }
                // try to go to the right:
                if (currentPath.size() > 1) {
                    int indexInPath = currentPath.size() - 2;
                    while (indexInPath > -1) {
                        Container parent = (Container) currentPath.get(indexInPath);
                        List<Component> children = asList(parent.getComponents());
                        int childIndex = children.indexOf(currentPath.get(indexInPath + 1));
                        if (childIndex < (children.size() - 1)) {
                            // take next child:
                            List<Component> nextPath = new ArrayList<>(currentPath.subList(0, indexInPath + 1));
                            nextPath.add(children.get(childIndex + 1));
                            return nextPath;
                        }
                        indexInPath--; // go 1 level up
                    }
                }
                return Collections.emptyList(); // unable to find next path - end of iteration
            }

            @Override
            public boolean hasNext() {
                if (completed) {
                    return false;
                }
                if (nextPath == null) {
                    nextPath = getNextPath();
                }
                return !nextPath.isEmpty();
            }

            @Override
            public Component next() {
                if (completed) {
                    throw new NoSuchElementException();
                }
                currentPath = nextPath != null ? nextPath : getNextPath();
                nextPath = getNextPath(); // current path has changed  - update next path
                completed = nextPath.isEmpty();
                return currentPath.get(currentPath.size() - 1);
            }
        };
    }

    /**
     * Must be invoked on EDT. The component hierarchy should not change during streaming.
     *
     * @param parent Parent container. Not null.
     * @return Stream of all descendant components in the parent container (incl. the parent itself). First element
     * of the stream is the root parent.
     * @see #getDescendantsIterable(Component)
     */
    public static Stream<Component> streamDescendants(Component parent) {
        return StreamSupport.stream(getDescendantsIterable(parent).spliterator(), false);
    }
}
