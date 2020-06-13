# table-stream-utils
Utils for iterating over JTable cells via Java 8 streams.

The iteration order is from left to right, from top to bottom.

Example (count how many times cell value "London" appears in the selected cells of column 3):
```java
import org.swingk.utils.table.TableStreamUtils;
import org.swingk.utils.table.TableCellData;

JTable table = ...;
long count = TableStreamUtils.asStream(table)
  .filter(TableCellData::isSelected)
  .filter(cellData -> cellData.getColumn() == 3)
  .map(TableCellData::getValue)
  .filter(value -> "London".equals(value))
  .count();
```
