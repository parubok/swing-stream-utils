package org.swingk.utils.table;

import java.util.Objects;
import java.util.function.Function;

/**
 * {@link javax.swing.JTable} column descriptor for {@link TableStreamUtils#toJTable} collectors.
 * @param <K> Type of stream elements.
 */
public class Column<K> {

    public static final int DEFAULT_PREFERRED_WIDTH = 75; // pixels

    private final String name;
    private final int preferredWidth;
    private final Function<K, ? extends Object> valueProducer;

    /**
     * @param name Name of the column. Not null.
     * @param preferredWidth Preferred width of the column in pixels. See {@link #DEFAULT_PREFERRED_WIDTH}.
     * @param valueProducer Producer of values for the column. May be called on not EDT thread (e.g. with parallel stream).
     */
    public Column(String name, int preferredWidth, Function<K, ? extends Object> valueProducer) {
        this.name = Objects.requireNonNull(name);
        this.preferredWidth = preferredWidth;
        this.valueProducer = Objects.requireNonNull(valueProducer);
    }

    public Column(String name, Function<K, ? extends Object> valueProducer) {
        this(name, DEFAULT_PREFERRED_WIDTH, valueProducer);
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
