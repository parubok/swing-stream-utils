package org.swingk.utils.table;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Table model with fixed row/column count.
 */
public final class SimpleTableModel extends AbstractTableModel {
    private final List<List<Object>> data;
    private final int columnCount;
    private final List<Class<?>> columnClasses;
    private final List<String> columnNames;
    private final boolean[] columnsEditable;

    SimpleTableModel(List<List<Object>> data, int columnCount, List<Class<?>> columnClasses, List<String> columnNames,
                     boolean[] columnsEditable) {
        super();
        this.data = data;
        this.columnCount = columnCount;
        this.columnClasses = columnClasses;
        this.columnNames = columnNames;
        this.columnsEditable = columnsEditable;
    }

    /**
     * @return Data object (e.g. stream element) associated with this row.
     */
    public Object getRowObject(int rowIndex) {
        return data.get(rowIndex).get(columnCount);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames.get(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses.get(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnsEditable[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data.get(rowIndex).get(columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        data.get(rowIndex).set(columnIndex, aValue);
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}
