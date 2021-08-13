package io.github.parubok.stream;

import java.util.Iterator;
import java.util.Objects;

import static java.util.Collections.emptyIterator;

public class TreeStructureIterable implements Iterable<KTreePath> {

    private final TreeStructure treeStructure;
    private final TreeTraversalType traversalType;

    public TreeStructureIterable(TreeStructure treeStructure, TreeTraversalType traversalType) {
        this.treeStructure = Objects.requireNonNull(treeStructure);
        this.traversalType = Objects.requireNonNull(traversalType);
    }

    @Override
    public Iterator<KTreePath> iterator() {
        Object root = treeStructure.getRoot();
        if (root == null) {
            return emptyIterator();
        }
        return traversalType.createIterator(treeStructure);
    }
}
