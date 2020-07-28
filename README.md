![Java CI with Maven](https://github.com/parubok/table-stream-utils/workflows/Java%20CI%20with%20Maven/badge.svg)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/parubok/table-stream-utils/blob/master/LICENSE)

# swing-stream-utils
Utils for working with Java Swing components via Java 8 streams.

Example 1 (count how many times cell value "London" appears in the selected cells of column 3):
```java
import org.swingk.utils.stream.TableStreamUtils;
import org.swingk.utils.stream.TableCellData;

JTable table = ...;
long count = TableStreamUtils.asStream(table)
  .filter(TableCellData::isSelected)
  .filter(cellData -> cellData.getColumn() == 3)
  .map(TableCellData::getValue)
  .filter(value -> "London".equals(value))
  .count();
```

Example 2 (create table with 'Name' and 'Size' columns from a list of `File` objects):
```java
import java.io.File;
import java.util.List;

import org.swingk.utils.stream.Column;

import static org.swingk.utils.stream.SwingStreamUtils.toJTable;

List<File> files = ...;
/* FileTable is a subclass of JTable */
FileTable table = files.stream()
             .collect(toJTable(FileTable::new, new Column<>("Name", File::getName, 100, String.class), 
                                                new Column<>("Size", File::length, 70, Long.class));
```

It is worth mentioning that the utility ensures that the `JTable` creation and configuration are performed on EDT, even when the streaming code runs on a different thread. So the following example code is valid:
```java
import java.util.List;
...
import org.swingk.utils.stream.Column;

import static org.swingk.utils.stream.SwingStreamUtils.toJTable;

// not EDT
List<Server> servers = ...;
JTable table = servers.parallelStream() // OK to use parallel stream!
             .collect(toJTable(new Column<>("Name", Server::getName, 100, String.class),
                               new Column<>("Users", Server::getUserCount, 50, Integer.class),
                               new Column<>("Status", Server::getStatus, 200, String.class));
SwingUtilities.invokeLater(() -> panel.add(table))); // continue with the table on EDT
```

`SimpleTableModel` may be build from a stream as following:
```java
import java.util.List;
...
import org.swingk.utils.stream.Column;

import static org.swingk.utils.stream.SwingStreamUtils.toTableModel;

// may be executed on any thread
List<User> users = ...;
SimpleTableModel tableModel = users.parallelStream()
                            .collect(toTableModel(new Column<>("Name", User::getName, 100, String.class),
                                                  new Column<>("ID", User::getID, 50, Long.class),
                                                  new Column<>("Role", User::getRole, 200, String.class));
```

Requires Java 8 or later.
