package org.swingk.utils.table;

import javax.swing.JTable;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utils to iterate over cells of {@link JTable}.
 * Must be invoked on EDT.
 */
public class TableStreamUtils {

    private TableStreamUtils() {
    }

    /**
     * Note: This method assumes that the table does not change its number of rows and columns during the iteration.
     *
     * @param table Table which cells to iterate. Not null.
     * @param <T>   Type of the table.
     * @return Cell iterator for the provided table.
     * @throws IllegalArgumentException If the table is null.
     */
    public static <T extends JTable> Iterable<TableCellData<T>> asIterable(T table) {
        if (table == null) {
            throw new IllegalArgumentException("table is null");
        }
        return () -> {
            final int lastRow = table.getRowCount() - 1;
            final int lastColumn = table.getColumnCount() - 1;

            if (lastRow < 0 || lastColumn < 0) {
                return Collections.emptyIterator();
            }

            return new Iterator<TableCellData<T>>() {

                private int row = 0;
                private int column = 0;
                private boolean hasMoreCells = true;

                @Override
                public boolean hasNext() {
                    return hasMoreCells;
                }

                @Override
                public TableCellData<T> next() {
                    Object value = table.getValueAt(row, column);
                    TableCellData<T> cellData = new TableCellData<>(row, column, value, table);
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
     * Note: This method assumes that the table does not change its number of rows and columns during the streaming.
     *
     * @param table Table which cells will be streamed. Not null.
     * @param <T>   Type of the table.
     * @return Stream of {@link TableCellData} for the provided table.
     * @see #asIterable(JTable)
     */
    public static <T extends JTable> Stream<TableCellData<T>> asStream(T table) {
        return StreamSupport.stream(asIterable(table).spliterator(), false);
    }
}
