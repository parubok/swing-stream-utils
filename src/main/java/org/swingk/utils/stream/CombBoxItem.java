package org.swingk.utils.stream;

/**
 * Represents a single item in combo box and its attributes.
 *
 * @param <E> Type of the combo box item.
 */
public class CombBoxItem<E> {

    private final E item;
    private final int index;
    private final int modelSize;
    private final boolean selected;

    public CombBoxItem(E item, int index, int modelSize, boolean selected) {
        assert index < modelSize;
        this.item = item;
        this.index = index;
        this.modelSize = modelSize;
        this.selected = selected;
    }

    public E getItem() {
        return item;
    }

    public int getIndex() {
        return index;
    }

    public int getModelSize() {
        return modelSize;
    }

    public boolean isSelected() {
        return selected;
    }
}
