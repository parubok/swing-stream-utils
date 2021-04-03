package org.swingk.utils.stream;

import java.util.Iterator;

/**
 * Types of tree traversal. See https://en.wikipedia.org/wiki/Tree_traversal
 */
public enum TreeTraversalType {
    /**
     * Depth-first traversal, pre-order.
     */
    PRE_ORDER {
        @Override
        public Iterator<KTreePath> createIterator(TreeStructure treeStructure) {
            return new TreeStructureIteratorPreOrder(treeStructure);
        }
    },
    /**
     * Depth-first traversal, post-order.
     */
    POST_ORDER {
        @Override
        public Iterator<KTreePath> createIterator(TreeStructure treeStructure) {
            return new TreeStructureIteratorPostOrder(treeStructure);
        }
    };

    public abstract Iterator<KTreePath> createIterator(TreeStructure treeStructure);
}
