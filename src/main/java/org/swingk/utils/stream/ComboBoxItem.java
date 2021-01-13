package org.swingk.utils.stream;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

/**
 * Represents a single combo box item and its attributes (e.g. index).
 *
 * @param <E> Type of the combo box item.
 */
public class ComboBoxItem<E> {

    private final E item;
    private final int index;
    private final int modelSize;
    private final boolean selected;

    /**
     * Constructor.
     */
    public ComboBoxItem(E item, int index, int modelSize, boolean selected) {
        assert index < modelSize;
        this.item = item;
        this.index = index;
        this.modelSize = modelSize;
        this.selected = selected;
    }

    public E getItem() {
        return item;
    }

    /**
     * @return Index of the item in the combo box.
     * @see JComboBox#getItemAt(int)
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return Number of items in the combo box model.
     * @see ComboBoxModel#getSize()
     */
    public int getModelSize() {
        return modelSize;
    }

    /**
     * @return True is this item is the selected item of the combo box, false otherwise.
     * @see JComboBox#getSelectedItem()
     */
    public boolean isSelected() {
        return selected;
    }

    @Override
    public String toString() {
        return "ComboBoxItem{" +
                "item=" + item +
                ",index=" + index +
                ",modelSize=" + modelSize +
                ",selected=" + selected +
                '}';
    }
}
