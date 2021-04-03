package org.swingk.utils.stream;

import java.util.Iterator;
import java.util.Objects;

import static java.util.Collections.emptyIterator;

final class TreeStructureIterablePreOrder extends TreeStructureIterable {

    private final TreeStructure treeStructure;

    TreeStructureIterablePreOrder(TreeStructure treeStructure) {
        this.treeStructure = Objects.requireNonNull(treeStructure);
    }

    @Override
    public Iterator<KTreePath> iterator() {
        Object root = treeStructure.getRoot();
        if (root == null) {
            return emptyIterator();
        }
        return new TreeStructureIteratorPreOrder(treeStructure);
    }
}
