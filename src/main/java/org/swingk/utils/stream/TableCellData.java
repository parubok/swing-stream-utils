package org.swingk.utils.stream;

import javax.swing.JTable;

/**
 * Represents a table cell and its attributes.
 *
 * @param <T> Type of the table.
 */
public final class TableCellData<T extends JTable> {

    private final int row;
    private final int column;
    private final Object value;
    private final T table;
    private final boolean selected;
    private final boolean editable;

    public TableCellData(int row, int column, Object value, T table) {
        this.row = row;
        this.column = column;
        this.value = value;
        this.table = table;
        this.selected = table.isCellSelected(row, column);
        this.editable = table.isCellEditable(row, column);
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public Object getValue() {
        return value;
    }

    public T getTable() {
        return table;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isEditable() {
        return editable;
    }
}
