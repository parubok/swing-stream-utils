package org.parubok.swing.utils.table;

import javax.swing.JTable;

/**
 * Represents table cell and its data.
 *
 * @param <T> Type of the table.
 */
public final class TableCellData<T extends JTable> {

    private final int row;
    private final int column;
    private final Object value;
    private final T table;

    public TableCellData(int row, int column, Object value, T table) {
        this.row = row;
        this.column = column;
        this.value = value;
        this.table = table;
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
}
