package org.swingk.utils.stream;

import java.util.List;

/**
 * Implementation of {@link TreeTraversalType#POST_ORDER} iteration.
 */
final class TreeStructureIteratorPostOrder extends AbstractTreePathIterator {

    TreeStructureIteratorPostOrder(TreeStructure treeStructure) {
        super(treeStructure);
    }

    private KTreePath getLeafPath(KTreePath path) {
        while (treeStructure.getChildCount(path.getLastPathComponent()) > 0) {
            Object child = treeStructure.getChild(path.getLastPathComponent(), 0);
            path = path.pathByAddingChild(child);
        }
        return path;
    }

    @Override
    KTreePath getNextPath() {
        assert !completed;

        if (currentPath == EMPTY_PATH) {
            // start of iteration - return initial path:
            return getLeafPath(KTreePath.of(treeStructure.getRoot()));
        }

        if (currentPath.getPathCount() == 1) {
            return EMPTY_PATH; // root-only path - end of iteration
        }

        KTreePath parentPath = currentPath.getParentPath();
        Object parentNode = parentPath.getLastPathComponent();

        if (treeStructure.getChildCount(parentNode) == 1) {
            return parentPath;
        }

        // try to go right and down, if not possible - return parent path:
        List<Object> children = getChildren(treeStructure, parentNode);
        int childIndex = children.indexOf(currentPath.getLastPathComponent());
        assert childIndex > -1;
        if (childIndex < (children.size() - 1)) {
            return getLeafPath(parentPath.pathByAddingChild(children.get(childIndex + 1)));
        } else {
            return parentPath;
        }
    }
}
