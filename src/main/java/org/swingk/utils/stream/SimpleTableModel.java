package org.swingk.utils.stream;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.function.IntFunction;

/**
 * Table model with fixed row/column count.
 * Implements {@link IntFunction} to access row data objects.
 *
 * @param <T> Type of stream elements.
 */
public final class SimpleTableModel<T> extends AbstractTableModel implements IntFunction<T> {
    private final List<List<Object>> data;
    private final int columnCount;
    private final List<Class<?>> columnClasses;
    private final List<String> columnNames;
    private final boolean[] columnsEditable;

    SimpleTableModel(List<List<Object>> data, List<Class<?>> columnClasses, List<String> columnNames,
                     boolean[] columnsEditable) {
        super();
        this.data = data;
        this.columnCount = columnClasses.size();
        this.columnClasses = columnClasses;
        this.columnNames = columnNames;
        this.columnsEditable = columnsEditable;
    }

    /**
     * @return Data object (e.g. stream element) associated with this row.
     */
    public T getRowObject(int rowIndex) {
        return (T) data.get(rowIndex).get(columnCount);
    }

    @Override
    public T apply(int rowIndex) {
        return getRowObject(rowIndex);
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
