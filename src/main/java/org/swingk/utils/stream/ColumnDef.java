package org.swingk.utils.stream;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Table column definition for {@link SwingStreamUtils#toTable} collectors.
 *
 * @param <K> Type of stream elements.
 * @see javax.swing.table.TableColumn
 */
public class ColumnDef<K> {

    public static final int DEFAULT_PREFERRED_WIDTH = 75; // pixels

    /**
     * Extracts instances of {@link ColumnDef} from the suppliers.
     * <p>
     * Useful when column definitions are provided via enum, so a call to {@code Enum.values()} results in an array
     * of {@link ColumnDef} suppliers (see README.md).
     */
    @SafeVarargs
    public static <T> ColumnDef<T>[] get(Supplier<ColumnDef<T>>... columnSuppliers) {
        ColumnDef<T>[] columns = new ColumnDef[columnSuppliers.length];
        for (int i = 0; i < columns.length; i++) {
            Supplier<ColumnDef<T>> s = Objects.requireNonNull(columnSuppliers[i]);
            columns[i] = Objects.requireNonNull(s.get());
        }
        return columns;
    }

    private final String name;
    private final int preferredWidth;
    private final Function<K, ?> valueProducer;
    private final Class<?> columnClass;
    private final boolean editable;

    /**
     * @param name Name of the column. Not null.
     * @param valueProducer Producer of values for the column. May be called on non-EDT thread (e.g. with parallel stream).
     * @param preferredWidth Preferred width of the column in pixels. See {@link #DEFAULT_PREFERRED_WIDTH}.
     * @param columnClass Class which will be returned from {@link javax.swing.table.TableModel#getColumnClass(int)} for this column. Not null.
     * @param editable True if cells of this column should be editable.
     */
    public ColumnDef(String name, Function<K, ?> valueProducer, int preferredWidth, Class<?> columnClass,
                     boolean editable) {
        if (preferredWidth < 1) {
            throw new IllegalArgumentException();
        }
        this.name = Objects.requireNonNull(name);
        this.valueProducer = Objects.requireNonNull(valueProducer);
        this.preferredWidth = preferredWidth;
        this.columnClass = Objects.requireNonNull(columnClass);
        this.editable = editable;
    }

    public ColumnDef(String name, Function<K, ?> valueProducer, int preferredWidth, Class<?> columnClass) {
        this(name, valueProducer, preferredWidth, columnClass, false);
    }

    public ColumnDef(String name, Function<K, ?> valueProducer, int preferredWidth) {
        this(name, valueProducer, preferredWidth, Object.class);
    }

    public ColumnDef(String name, Function<K, ?> valueProducer) {
        this(name, valueProducer, DEFAULT_PREFERRED_WIDTH);
    }

    public ColumnDef(String name) {
        this(name, Function.identity());
    }

    /**
     * @return Name of the column.
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    public String getName() {
        return name;
    }

    /**
     * @return Preferred width of this column in pixels.
     * @see javax.swing.table.TableColumn#setPreferredWidth(int)
     * @see javax.swing.table.TableColumn#getPreferredWidth()
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }

    public Function<K, ?> getValueProducer() {
        return valueProducer;
    }

    public Class<?> getColumnClass() {
        return columnClass;
    }

    public boolean isEditable() {
        return editable;
    }
}
