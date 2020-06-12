# table-stream-utils
Utils for iterating over JTable cells via Java 8 streams.

Example 1 (count how many times cell value "London" appears in the selected cells of column 3):
```
JTable table = ...;
long count = TableStreamUtils.asStream(table)
  .filter(TableCellData::isSelected)
  .filter(cellData -> cellData.getColumn() == 3)
  .filter(cellData -> "London".equals(cellData.getValue()))
  .count();
```
