package org.swingk.utils.table;

import java.util.Objects;
import java.util.function.Function;

public class Column<K> {

    private final String name;
    private final int preferredWidth;
    private final Function<K, ? extends Object> valuesProducer;

    public Column(String name, int preferredWidth, Function<K, ? extends Object> valuesProducer) {
        this.name = Objects.requireNonNull(name);
        this.preferredWidth = preferredWidth;
        this.valuesProducer = Objects.requireNonNull(valuesProducer);
    }

    public Column(String name, Function<K, ? extends Object> valuesProducer) {
        this(name, 75, valuesProducer);
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

    public Function<K, ? extends Object> getValuesProducer() {
        return valuesProducer;
    }
}
