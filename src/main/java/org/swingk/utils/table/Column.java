package org.swingk.utils.table;

import java.util.Objects;
import java.util.function.Function;

/**
 * {@link javax.swing.JTable} column descriptor for {@link TableStreamUtils#toJTable} collectors.
 * @param <K> Type of stream elements.
 * @see javax.swing.table.TableColumn
 */
public class Column<K> {

    public static final int DEFAULT_PREFERRED_WIDTH = 75; // pixels

    private final String name;
    private final int preferredWidth;
    private final Function<K, ? extends Object> valueProducer;
    private final Class<?> columnClass;
    private final boolean editable;

    /**
     * @param name Name of the column. Not null.
     * @param valueProducer Producer of values for the column. May be called on non-EDT thread (e.g. with parallel stream).
     * @param preferredWidth Preferred width of the column in pixels. See {@link #DEFAULT_PREFERRED_WIDTH}.
     * @param columnClass Class which will be returned from {@link javax.swing.table.TableModel#getColumnClass(int)} for this column. Not null.
     * @param editable True if cells of this column should be editable.
     */
    public Column(String name, Function<K, ? extends Object> valueProducer, int preferredWidth, Class<?> columnClass,
                  boolean editable) {
        this.name = Objects.requireNonNull(name);
        this.valueProducer = Objects.requireNonNull(valueProducer);
        this.preferredWidth = preferredWidth;
        this.columnClass = Objects.requireNonNull(columnClass);
        this.editable = editable;
    }

    public Column(String name, Function<K, ? extends Object> valueProducer, int preferredWidth, Class<?> columnClass) {
        this(name, valueProducer, preferredWidth, columnClass, false);
    }

    public Column(String name, Function<K, ? extends Object> valueProducer, int preferredWidth) {
        this(name, valueProducer, preferredWidth, Object.class);
    }

    public Column(String name, Function<K, ? extends Object> valueProducer) {
        this(name, valueProducer, DEFAULT_PREFERRED_WIDTH);
    }

    public Column(String name) {
        this(name, Function.identity());
    }

    public String getName() {
        return name;
    }

    public int getPreferredWidth() {
        return preferredWidth;
    }

    public Function<K, ? extends Object> getValueProducer() {
        return valueProducer;
    }

    public Class<?> getColumnClass() {
        return columnClass;
    }

    public boolean isEditable() {
        return editable;
    }
}
