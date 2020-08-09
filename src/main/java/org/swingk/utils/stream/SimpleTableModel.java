package org.swingk.utils.stream;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Vector;
import java.util.function.IntFunction;
import java.util.function.ObjIntConsumer;

/**
 * Table model which extends {@link DefaultTableModel}.
 * Implements {@link IntFunction} to access row data objects and {@link ObjIntConsumer} to set row data objects.
 *
 * @param <T> Type of the source stream elements.
 * @see SwingStreamUtils#toTableModel(Column[])
 */
public final class SimpleTableModel<T> extends DefaultTableModel implements IntFunction<T>, ObjIntConsumer<T> {
    /**
     * Single-column model to keep row objects.
     */
    private final DefaultTableModel rowObjects;

    private final List<Class<?>> columnClasses;
    private final boolean[] columnsEditable;

    SimpleTableModel(List<List<Object>> data, List<Class<?>> columnClasses, List<String> columnNames,
                     boolean[] columnsEditable) {
        super(data.size(), columnClasses.size());
        final int rowCount = getRowCount();
        final int colCount = getColumnCount();
        this.rowObjects = new DefaultTableModel(rowCount, 1);
        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < colCount; column++) {
                Vector rowVector = (Vector) dataVector.elementAt(row);
                rowVector.setElementAt(data.get(row).get(column), column);
            }
            this.rowObjects.setValueAt((T) data.get(row).get(colCount), row, 0);
        }
        setColumnIdentifiers(new Vector<>(columnNames));
        this.columnClasses = columnClasses;
        this.columnsEditable = columnsEditable;
    }

    @Override
    public void setNumRows(int rowCount) {
        rowObjects.setNumRows(rowCount);
        super.setNumRows(rowCount);
    }

    @Override
    public void moveRow(int start, int end, int to) {
        rowObjects.moveRow(start, end, to);
        super.moveRow(start, end, to);
    }

    @Override
    public void removeRow(int row) {
        rowObjects.removeRow(row);
        super.removeRow(row);
    }

    @Override
    public void insertRow(int row, Vector rowData) {
        rowObjects.insertRow(row, new Object[]{null});
        super.insertRow(row, rowData);
    }

    /**
     * @return Data object (e.g. stream element) associated with this row.
     */
    public T getRowObject(int rowIndex) {
        return (T) rowObjects.getValueAt(rowIndex, 0);
    }

    /**
     * @param rowIndex Row index.
     * @param rowObject Data object (e.g. stream element) associated with this row.
     */
    public void setRowObject(int rowIndex, T rowObject) {
        rowObjects.setValueAt(rowObject, rowIndex, 0);
    }

    @Override
    public T apply(int rowIndex) {
        return getRowObject(rowIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex >= columnClasses.size()) {
            return Object.class;
        }
        return columnClasses.get(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex >= columnsEditable.length) {
            return false;
        }
        return columnsEditable[columnIndex];
    }

    @Override
    public void accept(T rowData, int rowIndex) {
        setRowObject(rowIndex, rowData);
    }
}
