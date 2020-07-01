package org.swingk.utils.table;

import java.util.AbstractList;
import java.util.List;

/**
 * A list which 'combines' two other lists to present them as a single list.
 * @param <T> List element type.
 */
final class CombinedList<T> extends AbstractList<T> {

    private final List<T> list1;
    private final int size1;
    private final List<T> list2;

    CombinedList(List<T> list1, List<T> list2) {
        this.list1 = list1;
        this.size1 = list1.size();
        this.list2 = list2;
    }

    @Override
    public T get(int index) {
        return index < size1 ? list1.get(index) : list2.get(index - size1);
    }

    @Override
    public int size() {
        return list1.size() + list2.size();
    }
}
