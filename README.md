![Java CI with Maven](https://github.com/parubok/table-stream-utils/workflows/Java%20CI%20with%20Maven/badge.svg)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/parubok/table-stream-utils/blob/master/LICENSE)
[![Latest Version](https://img.shields.io/maven-central/v/io.github.parubok/swing-stream-utils)](https://search.maven.org/search?q=a:swing-stream-utils)
[![javadoc](https://javadoc.io/badge2/io.github.parubok/swing-stream-utils/javadoc.svg)](https://javadoc.io/doc/io.github.parubok/swing-stream-utils)

# swing-stream-utils
Utils for working with Java Swing components via Java 8 streams.

Functionality of the library is provided by the static methods of class [`SwingStreamUtils`](https://javadoc.io/doc/io.github.parubok/swing-stream-utils/latest/io/github/parubok/stream/SwingStreamUtils.html).
The methods may be divided into two categories: those which allow to iterate (or stream) over data items of Swing components 
(e.g. `SwingStreamUtils.stream`) and collectors which allow to create Swing components from a stream data (e.g. `SwingStreamUtils.toTable`).

Example 1 (for `JTable`, count how many times value `"London"` appears in the selected cells of column `"City"`):
```java
import javax.swing.JTable;

import io.github.parubok.stream.SwingStreamUtils;
import io.github.parubok.stream.TableCellData;

JTable table = ...;
long count = SwingStreamUtils.stream(table)
  .filter(TableCellData::isSelected)
  .filter(cellData -> cellData.getColumnName().equals("City"))
  .map(TableCellData::getValue)
  .filter("London"::equals)
  .count();
  
// or with for-each loop:
count = 0;
for (TableCellData<JTable> cellData: SwingStreamUtils.asIterable(table)) {
  if (cellData.isSelected() && cellData.getColumnName().equals("City") && "London".equals(cellData.getValue())) {
    count++;
  }
}
```

Example 2 (create table with 'Name' and 'Size' columns from a list of `File` objects):
```java
import java.io.File;
import java.util.List;

import io.github.parubok.stream.ColumnDef;

import static io.github.parubok.stream.SwingStreamUtils.toTable;

List<File> files = ...;
/* FileTable is a subclass of JTable */
FileTable table = files.stream()
             .collect(toTable(FileTable::new, new ColumnDef<>("Name", File::getName, 100, String.class), 
                                                new ColumnDef<>("Size", File::length, 70, Long.class)));
```

Example 3 (create a table with specific model class):
```java
import java.io.File;
import java.util.List;

import io.github.parubok.stream.ColumnDef;

import static io.github.parubok.stream.SwingStreamUtils.toTable;

List<File> files = ...;
FileTable table = files.stream()
             .collect(toTable(() -> new FileTable(), rowCount -> new FileTableModel(rowCount), 
                                                new ColumnDef<>("Name", File::getName, 100, String.class), 
                                                new ColumnDef<>("Size", File::length, 70, Long.class)));
```

### Tip: Using `enum` to define table columns
For a table with fixed (i.e. predefined) columns, Java `enum` provides a convenient way to define and access the column 
definitions - for example, when a column index is required after the table was created.

Example:
```java
import java.io.File;
import java.util.function.Supplier;

import io.github.parubok.stream.ColumnDef;

import static io.github.parubok.stream.SwingStreamUtils.toTable;

enum Column implements Supplier<ColumnDef<File>> {
    NAME(new ColumnDef<>("Name", File::getName, 100, String.class)),
    SIZE(new ColumnDef<>("Size", File::length, 70, Long.class));

    final ColumnDef<File> def;

    Column(ColumnDef<File> def) {
        this.def = def;
    }

    @Override
    public ColumnDef<File> get() {
        return def;
    }
}

List<File> files = ...;
// use enum values() method to get all column definitions:
JTable table = files.stream()
        .collect(toTable(ColumnDef.get(Column.values())));

// use enum ordinal() method to obtain column index:
String name = (String) table.getValueAt(0, FileTableColumn.NAME.ordinal());

// translate column index to ColumnDef:
int columnIndex = ...;
ColumnDef<File> def = FileTableColumn.values()[columnIndex].get();
```

Example 4 (create combo box with file names from a list of `File` objects):
```java
import java.io.File;
import java.util.List;
import javax.swing.JComboBox;

import static io.github.parubok.stream.SwingStreamUtils.toComboBox;

List<File> files = ...;
JComboBox<String> = files.stream()
             .map(File::getName)
             .collect(toComboBox());
```

Example 5 (find all visible descendants of a container):
```java
import java.util.List;
import java.util.stream.Collectors;
import java.awt.Component;
import java.awt.Container;

import static io.github.parubok.stream.SwingStreamUtils.streamDescendants;

Container container = ...;
List<Component> visibleDescendants = streamDescendants(container)
             .skip(1) // skip the container itself
             .filter(Component::isVisible)
             .collect(Collectors.toList());
```

Example 6 (find all unselected combo box items which start with a letter "z"):
```java


import io.github.parubok.stream.SwingStreamUtils;

JComboBox<String> comboBox = ...;
List<String> visibleDescendants = SwingStreamUtils.stream(comboBox)
             .filter(comboBoxItem -> !comboBoxItem.isSelected())
             .filter(comboBoxItem -> comboBoxItem.getItem().startsWith("z"))
             .collect(Collectors.toList());
```

Example 7 (search for the first tree path with the specified node):
```java
import java.util.Optional;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import io.github.parubok.stream.KTreePath;
import io.github.parubok.stream.SwingStreamUtils;
import io.github.parubok.stream.TreeTraversalType;

JTree tree = ...;
TreeNode node = ...;
Optional<KTreePath> nodePath = SwingStreamUtils.stream(tree, TreeTraversalType.PRE_ORDER)
             .filter(path -> path.isLastComponent(node))
             .findFirst();
```

It is worth mentioning that in most cases (check JavaDoc) the utility ensures that the Swing component creation and configuration are performed on EDT, even when the streaming code runs on a different thread. So the following example code is valid:
```java
import java.util.List;
...
import io.github.parubok.stream.ColumnDef;

import static io.github.parubok.stream.SwingStreamUtils.toTable;

// may be not EDT (for example, Swing worker background thread)
List<Server> servers = ...;
JTable table = servers.parallelStream() // OK to use parallel stream!
             .collect(toTable(new ColumnDef<>("Name", Server::getName, 100, String.class),
                               new ColumnDef<>("Users", Server::getUserCount, 50, Integer.class),
                               new ColumnDef<>("Status", Server::getStatus, 200, String.class));
```

`SimpleTableModel` (extends `javax.swing.table.DefaultTableModel`) may be build from a stream as following:
```java
import java.util.List;
...
import io.github.parubok.stream.ColumnDef;

import static io.github.parubok.stream.SwingStreamUtils.toTableModel;

// may be executed on any thread
List<User> users = ...;
SimpleTableModel<User> tableModel = users.parallelStream()
                            .collect(toTableModel(new ColumnDef<>("Name", User::getName, 100, String.class),
                                                  new ColumnDef<>("ID", User::getID, 50, Long.class),
                                                  new ColumnDef<>("Role", User::getRole, 200, String.class));
```

This project has no external dependencies (except JUnit 5, for testing).

Requires Java 8 or later.

### License

This project is licensed under [Apache License, version 2.0](https://www.apache.org/licenses/LICENSE-2.0)

### Installation

Releases are available in [Maven Central](https://repo1.maven.org/maven2/io/github/parubok/swing-stream-utils/)

#### Maven

Add this snippet to the pom.xml `dependencies` section:

```xml
<dependency>
    <groupId>io.github.parubok</groupId>
    <artifactId>swing-stream-utils</artifactId>
    <version>1.37</version>
</dependency>
```

### See Also

http://www.glazedlists.com/
