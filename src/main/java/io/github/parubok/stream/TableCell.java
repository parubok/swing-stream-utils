package io.github.parubok.stream;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Table cell with row and column indexes.
 * <p>
 * Immutable object - may be shared by reference.
 * <p>
 * Implements {@link Comparable}. The comparison is performed by row and then by column.
 * <p>
 * Also contains some utility methods.
 */
public class TableCell implements Comparable<TableCell> {

    public static OptionalInt getMaxRow(Collection<TableCell> cells) {
        return cells.stream()
                .mapToInt(TableCell::getRow)
                .max();
    }

    public static OptionalInt getMaxCol(Collection<TableCell> cells) {
        return cells.stream()
                .mapToInt(TableCell::getColumn)
                .max();
    }

    private final int row;
    private final int col;

    public enum Field {
        ROW, COLUMN
    }

    public static final Field ROW = Field.ROW;
    public static final Field COLUMN = Field.COLUMN;

    public TableCell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public TableCell(TableCell source, int rowDelta, int columnDelta) {
        this(source.getRow() + rowDelta, source.getColumn() + columnDelta);
    }

    public int getColumn() {
        return col;
    }

    public int getRow() {
        return row;
    }

    /**
     * @param field TableCell.ROW or TableCell.COL.
     * @return Value of the field for this cell.
     */
    public int get(Field field) {
        return field.equals(ROW) ? getRow() : getColumn();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(row + col);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof TableCell) && equals(((TableCell) obj).row, ((TableCell) obj).col);
    }

    public boolean equals(int row, int col) {
        return this.row == row && this.col == col;
    }

    @Override
    public String toString() {
        return "TableCell{row=" + row + ",column=" + col + "}";
    }

    /**
     * @return true if the specified cells form a continuous selection rectangle.
     * @implNote Returns true for empty set.
     */
    public static boolean isContinuousSelection(Set<TableCell> cells) {
        int[] d = getDimensions(cells);
        return (d[0] * d[1]) == cells.size();
    }

    /**
     * @return int[] { row count, column count}
     */
    public static int[] getDimensions(Set<TableCell> cells) {
        if (cells.isEmpty()) {
            return new int[]{0, 0};
        }
        TableCell leftUpper = Collections.min(cells);
        TableCell rightLower = Collections.max(cells);
        int rowCount = rightLower.getRow() - leftUpper.getRow() + 1;
        int colCount = rightLower.getColumn() - leftUpper.getColumn() + 1;
        return new int[]{rowCount, colCount};
    }

    /**
     * Compare cells by row or by column if rows are equal.<br>
     * Call of {@link java.util.Collections#min(Collection)} returns the leftmost TableCell in the topmost row within
     * {@link Collection}.<br>
     * Call of {@link java.util.Collections#max(Collection)} returns the rightmost TableCell in the bottommost row
     * within {@link Collection}.
     */
    @Override
    public int compareTo(TableCell c) {
        int result = (this.getRow() - c.getRow());
        if (result == 0) {
            result = COLUMNS_COMPARATOR.compare(this, c);
        }
        return result;
    }

    public boolean isValid() {
        return row > -1 && col > -1;
    }

    /**
     * Compares two {@link TableCell}s by columns ignoring rows.
     */
    public static final Comparator<TableCell> COLUMNS_COMPARATOR = new Comparator<TableCell>() {
        @Override
        public int compare(TableCell c0, TableCell c1) {
            return c0.getColumn() - c1.getColumn();
        }
    };
}
