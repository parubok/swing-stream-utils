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

    /**
     * @param name Name of the column. Not null.
     * @param valueProducer Producer of values for the column. May be called on not EDT thread (e.g. with parallel stream).
     * @param preferredWidth Preferred width of the column in pixels. See {@link #DEFAULT_PREFERRED_WIDTH}.
     */
    public Column(String name, Function<K, ? extends Object> valueProducer, int preferredWidth) {
        this.name = Objects.requireNonNull(name);
        this.valueProducer = Objects.requireNonNull(valueProducer);
        this.preferredWidth = preferredWidth;
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
}
