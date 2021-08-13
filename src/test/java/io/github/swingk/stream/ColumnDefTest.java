package io.github.swingk.stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;

public class ColumnDefTest {
    @Test
    public void get() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Assertions.assertEquals(0, ColumnDef.get().length);

            ColumnDef<String> def = new ColumnDef<>("col1");
            ColumnDef<String>[] defs = ColumnDef.get(() -> def);
            Assertions.assertEquals(1, defs.length);
            Assertions.assertEquals(def, defs[0]);
        });
    }
}
