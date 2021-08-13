package io.github.parubok.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;

abstract class AbstractCollector<T, K> implements Collector<T, List<List<Object>>, K> {

    private final ColumnDef<T>[] columns;

    AbstractCollector(ColumnDef<T>[] columns) {
        if (columns.length == 0) {
            throw new IllegalArgumentException("Columns must be specified.");
        }
        this.columns = columns;
    }

    @Override
    public Supplier<List<List<Object>>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<List<Object>>, T> accumulator() {
        return (list, val) -> {
            List<Object> rowList = new ArrayList<>(columns.length + 1);
            for (int i = 0; i < columns.length; i++) {
                rowList.add(columns[i].getValueProducer().apply(val));
            }
            rowList.add(val);
            list.add(rowList);
        };
    }

    @Override
    public BinaryOperator<List<List<Object>>> combiner() {
        return CombinedList::new;
    }

    @Override
    public Set<Collector.Characteristics> characteristics() {
        return Collections.emptySet();
    }
}
