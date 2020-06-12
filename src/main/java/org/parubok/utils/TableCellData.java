package org.parubok.utils;

import javax.swing.JTable;

public final class TableCellData<K, T extends JTable> {

    public final int row;
    public final int column;
    public final K value;
    public final T table;

    public TableCellData(int row, int column, K value, T table) {
        super();
        this.row = row;
        this.column = column;
        this.value = value;
        this.table = table;
    }
}
