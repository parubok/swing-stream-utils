package io.github.parubok.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TableCellTest
{
    @Test
    public void getMaxRow()
    {
        Assertions.assertEquals(OptionalInt.empty(), TableCell.getMaxRow(Collections.emptyList()));
        Assertions.assertEquals(OptionalInt.of(10), TableCell.getMaxRow(Arrays.asList(new TableCell(10, 12))));
        Assertions.assertEquals(OptionalInt.of(11), TableCell.getMaxRow(Arrays.asList(new TableCell(10, 12),
                new TableCell(11, 13))));
    }

    @Test
    public void getMaxCol()
    {
        Assertions.assertEquals(OptionalInt.empty(), TableCell.getMaxCol(Collections.emptyList()));
        Assertions.assertEquals(OptionalInt.of(12), TableCell.getMaxCol(Arrays.asList(new TableCell(10, 12))));
        Assertions.assertEquals(OptionalInt.of(13), TableCell.getMaxCol(Arrays.asList(new TableCell(10, 12),
                new TableCell(11, 13))));
    }

    @Test
    public void getDimensions_emptySet()
    {
        int[] empty = TableCell.getDimensions(Collections.emptySet());
        Assertions.assertEquals(2, empty.length);
        Assertions.assertEquals(0, empty[0]);
        Assertions.assertEquals(0, empty[1]);
    }

    @Test
    public void getDimensions_verticalPair()
    {
        Set<TableCell> set = new HashSet<>();
        set.addAll(Arrays.asList(new TableCell[] { new TableCell(0, 0), new TableCell(99, 0) }));
        int[] d = TableCell.getDimensions(set);

        Assertions.assertEquals(2, d.length);
        Assertions.assertEquals(100, d[0]);
        Assertions.assertEquals(1, d[1]);
    }

    @Test
    public void getDimensions_diagonalAsc()
    {

        Set<TableCell> set = new HashSet<>();
        set.addAll(Arrays.asList(new TableCell[] { new TableCell(0, 100), new TableCell(99, 101) }));
        int[] d = TableCell.getDimensions(set);

        Assertions.assertEquals(2, d.length);
        Assertions.assertEquals(100, d[0]);
        Assertions.assertEquals(2, d[1]);
    }

    @Test
    public void getDimensions_diagonalDesc()
    {
        Set<TableCell> set = new HashSet<>();
        set.addAll(Arrays.asList(new TableCell[] { new TableCell(1, 1), new TableCell(99, 5) }));
        int[] d = TableCell.getDimensions(set);

        Assertions.assertEquals(2, d.length);
        Assertions.assertEquals(99, d[0]);
        Assertions.assertEquals(5, d[1]);
    }

    @Test
    public void getDimensions_continuousDescDiagonal()
    {
        ArrayList<TableCell> list = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            list.add(new TableCell(i, i));
        }

        Set<TableCell> set = new HashSet<>();
        set.addAll(list);
        int[] d = TableCell.getDimensions(set);

        Assertions.assertEquals(2, d.length);
        Assertions.assertEquals(10, d[0]);
        Assertions.assertEquals(10, d[1]);
    }

    @Test
    public void getDimensions_singleCell()
    {
        Set<TableCell> set = new HashSet<>();
        set.add(new TableCell(1, 1) );

        int[] oneCell = TableCell.getDimensions(set);

        Assertions.assertEquals(2, oneCell.length);
        Assertions.assertEquals(1, oneCell[0]);
        Assertions.assertEquals(1, oneCell[1]);
    }

    @Test
    public void constructor_2()
    {
        TableCell c = new TableCell(2, 3);
        TableCell c2 = new TableCell(c, -1, 1);

        Assertions.assertEquals(1, c2.getRow());
        Assertions.assertEquals(4, c2.getColumn());
    }

    @Test
    public void equals_2()
    {
        TableCell c = new TableCell(10, 20);
        Assertions.assertEquals(c, c);

        TableCell c2 = new TableCell(2, 3);
        Assertions.assertEquals(c2, new TableCell(c2, 0, 0));

        Assertions.assertNotEquals(c2, "");
        Assertions.assertNotEquals(c2, null);
        Assertions.assertNotEquals(c2, new Integer(0));
        Assertions.assertNotEquals(c2, new TableCell(c2, 0, 1));
        Assertions.assertNotEquals(c2, new TableCell(c2, 1, 0));
    }

    @Test
    public void hashCode_2()
    {
        // equal cells should have equal hash code
        for (int i = 0; i < 100; i++)
        {
            for (int j = 0; j < 100; j++)
            {
                Assertions.assertEquals(new TableCell(i, j).hashCode(), new TableCell(i, j).hashCode());
            }
        }
    }

    @Test
    public void hashCode_1()
    {
        HashSet<TableCell> set = new HashSet<>();
        // equal cells should have equal hash code
        for (int i = 0; i < 100; i++)
        {
            for (int j = 0; j < 100; j++)
            {
                set.add(new TableCell(i, j));
            }
        }
        Assertions.assertEquals(10_000, set.size());
        for (int i = 0; i < 100; i++)
        {
            for (int j = 0; j < 100; j++)
            {
                Assertions.assertTrue(set.contains(new TableCell(i, j)));
            }
        }
        TableCell min = Collections.min(set);
        Assertions.assertEquals(new TableCell(0, 0), min);

        TableCell max = Collections.max(set);
        Assertions.assertEquals(new TableCell(99, 99), max);
    }

    @Test
    public void compareCells_1()
    {
        List<TableCell> list = Arrays.asList(new TableCell(123, 0), new TableCell(7, 17), new TableCell(57, 28),
                new TableCell(7, 18));
        TableCell min = Collections.min(list);
        Assertions.assertEquals(new TableCell(7, 17), min);

        TableCell max = Collections.max(list);
        Assertions.assertEquals(new TableCell(123, 0), max);
    }

    @Test
    public void compareCells_2()
    {
        Assertions.assertThrows(NullPointerException.class, () -> new TableCell(7, 18).compareTo(null));
    }

    @Test
    public void compareColumns_1()
    {
        List<TableCell> list = Arrays.asList(new TableCell(123, 0), new TableCell(7, 17), new TableCell(57, 28),
                new TableCell(7, 18));
        TableCell min = Collections.min(list, TableCell.COLUMNS_COMPARATOR);
        Assertions.assertEquals(new TableCell(123, 0), min);

        TableCell max = Collections.max(list, TableCell.COLUMNS_COMPARATOR);
        Assertions.assertEquals(new TableCell(57, 28), max);
    }

    @Test
    public void isContinuousSelection() {
        Set<TableCell> set = new HashSet<>();
        Assertions.assertTrue(TableCell.isContinuousSelection(set));
        set.add(new TableCell(10, 2));
        Assertions.assertTrue(TableCell.isContinuousSelection(set));
        set.add(new TableCell(11, 2));
        Assertions.assertTrue(TableCell.isContinuousSelection(set));
        set.add(new TableCell(13, 2));
        Assertions.assertFalse(TableCell.isContinuousSelection(set));
        set.add(new TableCell(12, 2));
        Assertions.assertTrue(TableCell.isContinuousSelection(set));
    }

    @Test
    public void get_field() {
        TableCell c = new TableCell(10, 2);
        Assertions.assertEquals(10, c.get(TableCell.ROW));
        Assertions.assertEquals(2, c.get(TableCell.COLUMN));
    }

    @Test
    public void hashCode_() {
        for (int i = 0; i < 100; i++) {
            Assertions.assertEquals(new TableCell(i, i).hashCode(), new TableCell(i, i).hashCode());
            Assertions.assertEquals(new TableCell(0, i).hashCode(), new TableCell(0, i).hashCode());
            Assertions.assertEquals(new TableCell(i, 0).hashCode(), new TableCell(i, 0).hashCode());
        }
    }
}
