package org.swingk.utils.stream;

import java.util.Iterator;
import java.util.Objects;

import static java.util.Collections.emptyIterator;

final class TreeStructureIterablePostOrder extends TreeStructureIterable {

    private final TreeStructure treeStructure;

    TreeStructureIterablePostOrder(TreeStructure treeStructure) {
        this.treeStructure = Objects.requireNonNull(treeStructure);
    }

    @Override
    public Iterator<KTreePath> iterator() {
        Object root = treeStructure.getRoot();
        if (root == null) {
            return emptyIterator();
        }

        return new TreeStructureIteratorPostOrder(treeStructure);
    }
}
