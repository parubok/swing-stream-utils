package org.parubok.swing.utils.table;

import javax.swing.JTable;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utils to work with {@link JTable} and related classes via streams.
 * Must be invoked on EDT.
 */
public class TableStreamUtils {

    private TableStreamUtils() {
    }

    /**
     * @param table Table which cells to iterate. Not null.
     * @param <T>   Type of the table.
     * @return Cell iterator for the provided table.
     * @throws IllegalArgumentException If the table is null.
     */
    public static <T extends JTable> Iterable<TableCellData<Object, T>> asIterable(T table) {
        if (table == null) {
            throw new IllegalArgumentException("table is null");
        }
        return () -> {
            final int lastRow = table.getRowCount() - 1;
            final int lastColumn = table.getColumnCount() - 1;

            if (lastRow < 0 || lastColumn < 0) {
                return Collections.emptyIterator();
            }

            return new Iterator<TableCellData<Object, T>>() {

                private int row = 0;
                private int column = 0;
                private boolean hasMoreCells = true;

                @Override
                public boolean hasNext() {
                    return hasMoreCells;
                }

                @Override
                public TableCellData<Object, T> next() {
                    Object value = table.getValueAt(row, column);
                    TableCellData<Object, T> cellData = new TableCellData<>(row, column, value, table);
                    if (column < lastColumn) {
                        column++;
                    } else if (row < lastRow) {
                        row++;
                        column = 0;
                    } else {
                        hasMoreCells = false;
                    }
                    return cellData;
                }
            };
        };
    }

    /**
     * @param table Table which cells will be streamed. Not null.
     * @param <T>   Type of the table.
     * @return Stream of {@link TableCellData} for the provided table.
     * @see #asIterable(JTable)
     */
    public static <T extends JTable> Stream<TableCellData<Object, T>> asStream(T table) {
        return StreamSupport.stream(asIterable(table).spliterator(), false);
    }
}
