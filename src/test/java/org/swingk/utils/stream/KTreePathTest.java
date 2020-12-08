package org.swingk.utils.stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.tree.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class KTreePathTest {
    @Test
    void basic_test_1() {
        String root = "root";
        List<String> rootList = Collections.singletonList(root);
        KTreePath p = new KTreePath(rootList);
        Assertions.assertEquals(rootList, p.asList(String.class));
        Assertions.assertEquals(root, p.getLastPathComponent(String.class));

        String child = "child";
        KTreePath p2 = p.pathByAddingChild(child);
        Assertions.assertEquals(Arrays.asList(root, child), p2.asList(String.class));
        Assertions.assertEquals(child, p2.getLastPathComponent(String.class));

        Assertions.assertEquals(new TreePath(root), KTreePath.of(root));
        Assertions.assertEquals(new TreePath(new Object[]{root, child}), KTreePath.of(root, child));
        Assertions.assertEquals(Arrays.asList(root, child), KTreePath.of(root, child).asList());
    }
}
