package org.parubok.swing.utils.table;

import javax.swing.JTable;

/**
 * Represents table cell and its data.
 *
 * @param <K> Type of the cell data.
 * @param <T> Type of the table.
 */
public final class TableCellData<K, T extends JTable> {

    private final int row;
    private final int column;
    private final K value;
    private final T table;

    public TableCellData(int row, int column, K value, T table) {
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

    public K getValue() {
        return value;
    }

    public T getTable() {
        return table;
    }
}
