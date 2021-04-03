package org.swingk.utils.stream;

import java.util.Iterator;

public enum TreeTraversalType {
    PRE_ORDER {
        @Override
        public Iterator<KTreePath> createIterator(TreeStructure treeStructure) {
            return new TreeStructureIteratorPreOrder(treeStructure);
        }
    },
    POST_ORDER {
        @Override
        public Iterator<KTreePath> createIterator(TreeStructure treeStructure) {
            return new TreeStructureIteratorPostOrder(treeStructure);
        }
    };

    public abstract Iterator<KTreePath> createIterator(TreeStructure treeStructure);
}
