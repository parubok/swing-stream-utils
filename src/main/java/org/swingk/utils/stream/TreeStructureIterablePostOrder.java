package org.swingk.utils.stream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

        return new AbstractTreePathIterator() {
            @Override
            protected KTreePath getNextPath() {
                assert !completed;

                KTreePath p2 = currentPath;

                if (p2 == EMPTY_PATH) {
                    // start of iteration
                    p2 = KTreePath.of(root);
                    while (treeStructure.getChildCount(p2.getLastPathComponent()) > 0) {
                        Object child = treeStructure.getChild(p2.getLastPathComponent(), 0);
                        p2 = p2.pathByAddingChild(child);
                    }
                    return p2;
                }

                if (p2.getPathCount() == 1) {
                    return EMPTY_PATH; // end of iteration
                }

                int index = p2.getPathCount() - 2;
                Object indexNode = p2.getPathComponent(index);
                if (treeStructure.getChildCount(indexNode) < 2) {
                    List<Object> p3 = new ArrayList<>();
                    for (int i = 0; i <= index; i++) {
                        p3.add(p2.getPathComponent(i));
                    }
                    return new KTreePath(p3);
                }

                // try to go right and down:
                List<Object> children = getChildren(treeStructure, indexNode);
                KTreePath currentP = p2;
                int childIndex = children.indexOf(currentP.getPathComponent(index + 1));
                if (childIndex < (children.size() - 1)) {
                    // take next child:
                    List<Object> p = new ArrayList<>();
                    for (int i = 0; i <= index; i++) {
                        p.add(currentP.getPathComponent(i));
                    }
                    p.add(children.get(childIndex + 1));
                    currentP = new KTreePath(p);
                    while (treeStructure.getChildCount(currentP.getLastPathComponent()) > 0) {
                        Object child = treeStructure.getChild(currentP.getLastPathComponent(), 0);
                        currentP = currentP.pathByAddingChild(child);
                    }
                    return currentP;
                } else {
                    List<Object> p = new ArrayList<>();
                    for (int i = 0; i <= index; i++) {
                        p.add(currentP.getPathComponent(i));
                    }

                    return new KTreePath(p);
                }
            }
        };
    }
}
