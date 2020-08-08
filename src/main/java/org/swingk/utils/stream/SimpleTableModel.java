package org.swingk.utils.stream;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.IntFunction;

/**
 * Table model which extends {@link DefaultTableModel}.
 * Implements {@link IntFunction} to access row data objects.
 *
 * @param <T> Type of the source stream elements.
 * @see SwingStreamUtils#toTableModel(Column[])
 */
public final class SimpleTableModel<T> extends DefaultTableModel implements IntFunction<T> {
    private final List<T> rowsObjects;
    private final List<Class<?>> columnClasses;
    private final boolean[] columnsEditable;

    SimpleTableModel(List<List<Object>> data, List<Class<?>> columnClasses, List<String> columnNames,
                     boolean[] columnsEditable) {
        super(data.size(), columnClasses.size());
        final int colCount = getColumnCount();
        this.rowsObjects = new ArrayList<>(getRowCount());
        for (int row = 0; row < getRowCount(); row++) {
            this.rowsObjects.add((T) data.get(row).get(colCount));
            for (int column = 0; column < colCount; column++) {
                Vector rowVector = (Vector) dataVector.elementAt(row);
                rowVector.setElementAt(data.get(row).get(column), column);
            }
        }
        setColumnIdentifiers(new Vector(columnNames));
        this.columnClasses = columnClasses;
        this.columnsEditable = columnsEditable;
    }

    @Override
    public void setNumRows(int rowCount) {
        while (rowCount > rowsObjects.size()){
            rowsObjects.add(null);
        }
        while (rowCount < rowsObjects.size()) {
            rowsObjects.remove(rowsObjects.size() - 1);
        }
        assert rowCount == rowsObjects.size();
        super.setNumRows(rowCount);
    }

    @Override
    public void moveRow(int start, int end, int to) {
        List<T> subList = rowsObjects.subList(start, end + 1);
        List<T> temp = new ArrayList<>(subList);
        subList.clear();
        rowsObjects.addAll(to, temp);
        super.moveRow(start, end, to);
    }

    @Override
    public void removeRow(int row) {
        rowsObjects.remove(row);
        super.removeRow(row);
    }

    @Override
    public void insertRow(int row, Vector rowData) {
        rowsObjects.add(row, null);
        super.insertRow(row, rowData);
    }

    /**
     * @return Data object (e.g. stream element) associated with this row.
     */
    public T getRowObject(int rowIndex) {
        return rowsObjects.get(rowIndex);
    }

    /**
     * @param rowIndex Row index.
     * @param rowObject Data object (e.g. stream element) associated with this row.
     */
    public void setRowObject(int rowIndex, T rowObject) {
        rowsObjects.set(rowIndex, rowObject);
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
}
