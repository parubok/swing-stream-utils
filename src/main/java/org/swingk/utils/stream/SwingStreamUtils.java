package org.swingk.utils.stream;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

/**
 * Java-8 stream utils for Swing components.
 * <p>
 * https://github.com/parubok/swing-stream-utils
 * </p>
 */
public final class SwingStreamUtils {

    private SwingStreamUtils() {
    }

    /**
     * Note: The iteration order is from left to right, from top to bottom.
     *
     * @param table Table which cells to iterate. Not null.
     * @param <T>   Type of the table.
     * @return Cell iterator for the provided table.
     */
    public static <T extends JTable> Iterable<TableCellData<T>> asIterable(T table) {
        requireNonNull(table, "table");
        return () -> {
            final int lastRow = table.getRowCount() - 1;
            final int lastColumn = table.getColumnCount() - 1;

            if (lastRow < 0 || lastColumn < 0) {
                return emptyIterator();
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
     * Streams cells of {@link JTable}. The table traversal order is from left to right, from top to bottom.
     * Must be invoked on EDT.
     *
     * @param table Table which cells will be streamed. Not null.
     * @param <T>   Type of the table.
     * @return Stream of {@link TableCellData} for the provided table.
     * @see #asIterable(JTable)
     */
    public static <T extends JTable> Stream<TableCellData<T>> stream(T table) {
        return StreamSupport.stream(asIterable(table).spliterator(), false);
    }

    /**
     * Streams paths of the provided {@link JTree}.
     *
     * @param tree Tree to stream. Not null.
     * @see #stream(TreeModel)
     * @see TreePath
     */
    public static Stream<KTreePath> stream(JTree tree) {
        requireNonNull(tree, "tree");
        return stream(tree.getModel());
    }

    /**
     * Streams paths of the provided {@link TreeModel}.
     * <p>
     * <b>Note:</b> The tree structure should not change during the streaming.
     * </p>
     *
     * @param treeModel Tree model to stream. Not null.
     * @return Stream of paths of the provided tree model. The traversing is performed using depth-first search.
     * @see #asIterable(TreeModel)
     * @see TreePath
     */
    public static Stream<KTreePath> stream(TreeModel treeModel) {
        return StreamSupport.stream(asIterable(treeModel).spliterator(), false);
    }

    /**
     * Streams paths of the provided {@link TreeStructure}.
     * <p>
     * <b>Note:</b> The tree structure should not change during the streaming.
     * </p>
     *
     * @param treeStructure Tree structure to stream. Not null.
     * @return Stream of paths of the provided tree structure. The traversing is performed using depth-first search.
     * @see #asIterable(TreeStructure)
     * @see TreePath
     */
    public static Stream<KTreePath> stream(TreeStructure treeStructure) {
        return StreamSupport.stream(asIterable(treeStructure).spliterator(), false);
    }

    /**
     * <p>
     * <b>Note:</b> The tree structure should not change during the iteration.
     * </p>
     *
     * @param treeModel Tree model to iterate. Not null.
     * @return Iterable to iterate over paths of the provided tree model using depth-first search.
     * @see TreePath
     */
    public static Iterable<KTreePath> asIterable(TreeModel treeModel) {
        requireNonNull(treeModel);
        return asIterable(new TreeStructure() {
            @Override
            public Object getRoot() {
                return treeModel.getRoot();
            }

            @Override
            public Object getChild(Object parent, int index) {
                return treeModel.getChild(parent, index);
            }

            @Override
            public int getChildCount(Object parent) {
                return treeModel.getChildCount(parent);
            }
        });
    }

    /**
     * <p>
     * <b>Note:</b> The tree structure should not change during the iteration.
     * </p>
     *
     * @param treeStructure Tree structure to iterate. Not null.
     * @return Iterable to iterate over paths of the provided tree structure using depth-first search.
     * @see TreePath
     */
    public static Iterable<KTreePath> asIterable(TreeStructure treeStructure) {
        requireNonNull(treeStructure, "treeStructure");
        return new TreeStructureIterable(treeStructure);
    }

    /**
     * @return {@link Iterable} over combo box items of the provided model.
     * @see ComboBoxItem
     */
    public static <E> Iterable<ComboBoxItem<E>> asIterable(ComboBoxModel<E> model) {
        requireNonNull(model, "model");
        return () ->
        {
            final int itemCount = model.getSize();
            if (itemCount < 1) {
                return emptyIterator();
            }
            final Object selectedItem = model.getSelectedItem();
            return new Iterator<ComboBoxItem<E>>() {
                private int index = 0;

                private void checkForConcurrentModification() {
                    final int c = model.getSize();
                    if (itemCount != c) {
                        throw new ConcurrentModificationException("Expected item count: " + itemCount
                                + ", actual item count: " + c + ".");
                    }
                    Object sItem = model.getSelectedItem();
                    if (!Objects.equals(selectedItem, sItem)) {
                        throw new ConcurrentModificationException("Expected selected item: " + selectedItem
                                + ", actual selected item: " + sItem + ".");
                    }
                }

                @Override
                public boolean hasNext() {
                    return index < itemCount;
                }

                @Override
                public ComboBoxItem<E> next() {
                    checkForConcurrentModification();
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    E item = model.getElementAt(index);
                    ComboBoxItem<E> comboBoxItem = new ComboBoxItem<>(item, index, itemCount,
                            Objects.equals(item, selectedItem));
                    index++;
                    return comboBoxItem;
                }
            };
        };
    }

    /**
     * Streams items of the provided {@link JComboBox}.
     *
     * @see #stream(ComboBoxModel)
     * @see ComboBoxItem
     */
    public static <E> Stream<ComboBoxItem<E>> stream(JComboBox<E> comboBox) {
        return stream(comboBox.getModel());
    }

    /**
     * Streams items of the provided {@link ComboBoxModel}.
     *
     * @see #stream(JComboBox)
     * @see ComboBoxItem
     */
    public static <E> Stream<ComboBoxItem<E>> stream(ComboBoxModel<E> model) {
        return StreamSupport.stream(asIterable(model).spliterator(), false);
    }

    /**
     * Stream collector to create {@link JTable}.
     *
     * @see #toTable(Supplier, ColumnDef[])
     */
    @SafeVarargs
    public static <T> Collector<T, List<List<Object>>, JTable> toTable(ColumnDef<T>... columns) {
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
     * @param columns The table column definitions.
     * @param <T> Type of the stream elements.
     * @return The new table.
     */
    @SafeVarargs
    public static <T, K extends JTable> Collector<T, List<List<Object>>, K> toTable(Supplier<K> tableSupplier,
                                                                                    ColumnDef<T>... columns) {
        requireNonNull(tableSupplier);
        checkColumnsArg(columns);
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
                                                      ColumnDef<?>... columns) {
        final AtomicReference<K> tableRef = new AtomicReference<>();
        try {
            Runnable finisherTask = () -> {
                K table = requireNonNull(tableSupplier.get(), "table");
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

    private static void checkColumnsArg(ColumnDef<?>... columns) {
        if (columns.length == 0) {
            throw new IllegalArgumentException("Columns must be specified.");
        }
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
     *                      rows in the model. Should produce model with the correct number of rows and columns,
     *                      configured according to the provided column definitions. Called on the current thread.
     * @param columns The table column definitions.
     * @param <T> Type of the stream elements.
     * @return The new table.
     */
    @SafeVarargs
    public static <T, K extends JTable, M extends TableModel> Collector<T, List<List<Object>>, K> toTable(Supplier<K> tableSupplier,
                                                                                                          IntFunction<M> modelSupplier,
                                                                                                          ColumnDef<T>... columns) {
        requireNonNull(tableSupplier);
        requireNonNull(modelSupplier);
        checkColumnsArg(columns);
        return new AbstractCollector<T, K>(columns) {
            @Override
            public Function<List<List<Object>>, K> finisher() {
                return data -> {
                    final int rowCount = data.size();
                    M model = requireNonNull(modelSupplier.apply(rowCount), "model");
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
     *                      rows in the model. Should produce model with the correct number of rows and columns,
     *                      configured according to the provided column definitions. Called on the current thread.
     * @param columnSuppliers Suppliers of the table column definitions.
     * @param <T> Type of the stream elements.
     * @return The new table.
     */
    @SafeVarargs
    public static <T, K extends JTable, M extends TableModel> Collector<T, List<List<Object>>, K> toTable(Supplier<K> tableSupplier,
                                                                                                          IntFunction<M> modelSupplier,
                                                                                                          Supplier<ColumnDef<T>>... columnSuppliers) {
        ColumnDef<T>[] columns = new ColumnDef[columnSuppliers.length];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columnSuppliers[i].get();
        }
        return toTable(tableSupplier, modelSupplier, columns);
    }

    private static <T> SimpleTableModel<T> createSimpleModel(List<List<Object>> data, ColumnDef<T>[] columns) {
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
     * @param columns The table column definitions (column preferred width is ignored).
     * @param <T> Type of stream elements.
     * @return The table model.
     */
    @SafeVarargs
    public static <T> Collector<T, List<List<Object>>, SimpleTableModel<T>> toTableModel(ColumnDef<T>... columns) {
        checkColumnsArg(columns);
        return new AbstractCollector<T, SimpleTableModel<T>>(columns) {
            @Override
            public Function<List<List<Object>>, SimpleTableModel<T>> finisher() {
                return data -> createSimpleModel(data, columns);
            }
        };
    }

    /**
     * Stream collector to create {@link SimpleTableModel} (an element from the stream produces a single
     * table row, the corresponding element may be retrieved via {@link SimpleTableModel#getRowObject(int)}).
     *
     * @param columnSuppliers Suppliers of the table column definition (column preferred width is ignored).
     * @param <T> Type of stream elements.
     * @return The table model.
     */
    @SafeVarargs
    public static <T> Collector<T, List<List<Object>>, SimpleTableModel<T>> toTableModel(Supplier<ColumnDef<T>>... columnSuppliers) {
        ColumnDef<T>[] columns = new ColumnDef[columnSuppliers.length];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columnSuppliers[i].get();
        }
        return toTableModel(columns);
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
     * Passes {@code null} as a value of {@code indexToSelectProvider} parameter so the collector won't try to select
     * an item in the resulting combo box.
     *
     * @see #toComboBox(Supplier, Supplier, BiConsumer, ToIntFunction)
     */
    public static <T, D, K extends JComboBox<D>, M extends ComboBoxModel<D>> Collector<T, List<T>, K> toComboBox(Supplier<K> comboSupplier,
                                                                                                                 Supplier<M> modelSupplier,
                                                                                                                 BiConsumer<M, T> itemAdder) {
        return toComboBox(comboSupplier, modelSupplier, itemAdder, null);
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
     * @param modelSupplier Creates a concrete instance of {@link ComboBoxModel} for the collector. Called on the
     * current thread.
     * @param itemAdder Adds item to the model. Called on the current thread.
     * @param indexToSelectProvider If not null, provides selected item index for the combo box model. Accepts list of
     * the resulting stream elements. Index -1 means no selection. {@link IndexOutOfBoundsException} will be thrown if
     * the index exceeds the combo box model size. Called on the current thread.
     * @param <T> Type of the stream elements.
     * @param <D> Type of the resulting combo box items.
     * @param <K> Type of the resulting combo box.
     * @param <M> Type of the combo box model.
     * @return The new combo box.
     */
    public static <T, D, K extends JComboBox<D>, M extends ComboBoxModel<D>> Collector<T, List<T>, K> toComboBox(Supplier<K> comboSupplier,
                                                                                                                 Supplier<M> modelSupplier,
                                                                                                                 BiConsumer<M, T> itemAdder,
                                                                                                                 ToIntFunction<List<T>> indexToSelectProvider) {
        requireNonNull(comboSupplier);
        requireNonNull(modelSupplier);
        requireNonNull(itemAdder);
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
                return emptySet();
            }

            @Override
            public Function<List<T>, K> finisher() {
                return data -> {
                    final M model = createComboBoxModel(modelSupplier, itemAdder, indexToSelectProvider, data);
                    final AtomicReference<K> comboRef = new AtomicReference<>();
                    try {
                        Runnable finisherTask = () -> {
                            K combo = requireNonNull(comboSupplier.get(), "combo box");
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
     * Stream collector to create {@link ComboBoxModel}.
     * Passes {@code null} as a value of {@code indexToSelectProvider} parameter so the collector won't try to select
     * an item in the resulting model.
     *
     * @see #toComboBoxModel(Supplier, BiConsumer, ToIntFunction)
     */
    public static <T, D, M extends ComboBoxModel<D>> Collector<T, List<T>, M> toComboBoxModel(Supplier<M> modelSupplier,
                                                                                              BiConsumer<M, T> itemAdder) {
        return toComboBoxModel(modelSupplier, itemAdder, null);
    }

    /**
     * Stream collector to create {@link ComboBoxModel}. Works on the current thread.
     *
     * @param modelSupplier Creates a concrete instance of {@link ComboBoxModel} for the collector.
     * @param itemAdder Adds item to the model.
     * @param indexToSelectProvider If not null, provides selected item index for the new model. Accepts list of the
     * resulting stream elements. Index -1 means no selection. {@link IndexOutOfBoundsException} will be thrown if the
     * index exceeds the model size.
     * @param <T> Type of the stream elements.
     * @param <D> Type of the resulting combo box model items.
     * @param <M> Type of the combo box model.
     * @return The new combo box model.
     */
    public static <T, D, M extends ComboBoxModel<D>> Collector<T, List<T>, M> toComboBoxModel(Supplier<M> modelSupplier,
                                                                                              BiConsumer<M, T> itemAdder,
                                                                                              ToIntFunction<List<T>> indexToSelectProvider) {
        requireNonNull(modelSupplier);
        requireNonNull(itemAdder);
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
                return emptySet();
            }

            @Override
            public Function<List<T>, M> finisher() {
                return data -> createComboBoxModel(modelSupplier, itemAdder, indexToSelectProvider, data);
            }
        };
    }

    private static <T, D, M extends ComboBoxModel<D>> M createComboBoxModel(Supplier<M> modelSupplier,
                                                                            BiConsumer<M, T> itemAdder,
                                                                            ToIntFunction<List<T>> indexToSelectProvider,
                                                                            List<T> data) {
        final M model = requireNonNull(modelSupplier.get(), "model");
        data.forEach(item -> itemAdder.accept(model, item));
        if (indexToSelectProvider != null) {
            int indexToSelect = indexToSelectProvider.applyAsInt(data);
            if (indexToSelect > -1) {
                final int modelSize = model.getSize();
                if (modelSize <= indexToSelect) {
                    throw new IndexOutOfBoundsException("Invalid selection index " + indexToSelect
                            + ". Model size is " + modelSize + ".");
                }
                model.setSelectedItem(model.getElementAt(indexToSelect));
            } else {
                model.setSelectedItem(null); // clear selection
            }
        }
        return model;
    }

    /**
     * Note: Must be invoked on EDT. The component hierarchy should not change during the iteration.
     *
     * @param root Root parent component. Not null.
     * @return Iterable which iterates over all descendant components in the root component (incl. the root itself).
     * First returned item of the iterable is the root component. Iteration order: depth-first search.
     */
    public static Iterable<Component> getDescendantsIterable(Component root) {
        requireNonNull(root, "root");
        return () -> new Iterator<Component>() {
            private final Iterator<KTreePath> pathIterator = asIterable(new ComponentTreeStructure(root)).iterator();

            @Override
            public boolean hasNext() {
                return pathIterator.hasNext();
            }

            @Override
            public Component next() {
                return pathIterator.next().getLastPathComponent(Component.class);
            }
        };
    }

    /**
     * Note: Must be invoked on EDT. The component hierarchy should not change during streaming.
     *
     * @param parent Parent container. Not null.
     * @return Stream of all descendant components in the parent container (incl. the parent itself). First element
     * of the stream is the root parent. Streaming order: depth-first search.
     * @see #getDescendantsIterable(Component)
     */
    public static Stream<Component> streamDescendants(Component parent) {
        return StreamSupport.stream(getDescendantsIterable(parent).spliterator(), false);
    }
}
