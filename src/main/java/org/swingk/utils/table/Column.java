package org.swingk.utils.table;

import java.util.Objects;
import java.util.function.Function;

/**
 * {@link javax.swing.JTable} column descriptor for {@link TableStreamUtils#toJTable} collectors.
 * @param <K> Type of stream elements.
 */
public class Column<K> {

    private final String name;
    private final int preferredWidth;
    private final Function<K, ? extends Object> valueProducer;

    public Column(String name, int preferredWidth, Function<K, ? extends Object> valueProducer) {
        this.name = Objects.requireNonNull(name);
        this.preferredWidth = preferredWidth;
        this.valueProducer = Objects.requireNonNull(valueProducer);
    }

    public Column(String name, Function<K, ? extends Object> valueProducer) {
        this(name, 75, valueProducer);
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
