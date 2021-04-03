package org.swingk.utils.stream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

        return new AbstractTreePathIterator() {
            @Override
            protected KTreePath getNextPath() {
                assert !completed;
                if (currentPath == EMPTY_PATH) {
                    return KTreePath.of(root); // start iteration with the root path
                }
                // try to go down first:
                Object currentNode = currentPath.getLastPathComponent();
                if (treeStructure.getChildCount(currentNode) > 0) {
                    Object child = treeStructure.getChild(currentNode, 0);
                    return currentPath.pathByAddingChild(child);
                }
                // try to go to the right:
                if (currentPath.getPathCount() > 1) {
                    int indexInPath = currentPath.getPathCount() - 2;
                    while (indexInPath > -1) {
                        Object parent = currentPath.getPathComponent(indexInPath);
                        List<Object> children = getChildren(treeStructure, parent);
                        int childIndex = children.indexOf(currentPath.getPathComponent(indexInPath + 1));
                        assert childIndex > -1;
                        if (childIndex < (children.size() - 1)) {
                            // take next child:
                            List<Object> p = new ArrayList<>();
                            for (int i = 0; i < (indexInPath + 1); i++) {
                                p.add(currentPath.getPathComponent(i));
                            }
                            p.add(children.get(childIndex + 1));
                            return new KTreePath(p);
                        }
                        indexInPath--; // go 1 level up
                    }
                }
                return EMPTY_PATH; // unable to find next path - end of iteration
            }
        };
    }
}
