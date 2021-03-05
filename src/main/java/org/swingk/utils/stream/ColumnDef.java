package org.swingk.utils.stream;

import java.util.Objects;
import java.util.function.Function;

/**
 * Table column definition for {@link SwingStreamUtils#toTable} collectors.
 *
 * @param <K> Type of stream elements.
 * @see javax.swing.table.TableColumn
 */
public class ColumnDef<K> {

    public static final int DEFAULT_PREFERRED_WIDTH = 75; // pixels

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

    public String getName() {
        return name;
    }

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