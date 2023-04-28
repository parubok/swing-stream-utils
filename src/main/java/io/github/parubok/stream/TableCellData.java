package io.github.parubok.stream;

import javax.swing.JTable;

/**
 * Represents a table cell and its attributes.
 *
 * @param <T> Type of the table.
 */
public class TableCellData<T extends JTable> {

    private final int row;
    private final int column;
    private final String columnName;
    private final Object value;
    private final T table;
    private final boolean selected;
    private final boolean editable;

    public TableCellData(int row, int column, String columnName, Object value, T table, boolean selected,
                         boolean editable) {
        this.row = row;
        this.column = column;
        this.columnName = columnName;
        this.value = value;
        this.table = table;
        this.selected = selected;
        this.editable = editable;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public TableCell getCell() {
        return new TableCell(row, column);
    }

    /**
     * @see JTable#getColumnName(int)
     */
    public String getColumnName() {
        return columnName;
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

    @Override
    public String toString() {
        return "TableCellData{" +
                "row=" + row +
                ",column=" + column +
                ",value=" + value +
                ",selected=" + selected +
                ",editable=" + editable +
                ",table=" + table +
                '}';
    }
}
