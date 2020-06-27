# table-stream-utils
Utils for working with `JTable` via Java 8 streams.

Example 1 (count how many times cell value "London" appears in the selected cells of column 3):
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

Example 2 (create `JTable` with 'Name' and 'Size' columns from a list of `File` objects):
```java
import java.io.File;
import java.util.List;

import org.swingk.utils.table.Column;

import static org.swingk.utils.table.TableStreamUtils.toJTable;

List<File> files = ...;
FileTable table; // FileTable is a subclass of JTable
table = files.parallelStream()
             .collect(toJTable(FileTable::new, new Column<>("Name", 100, File::getName), 
                                                new Column<>("Size", 70, File::length));
```
