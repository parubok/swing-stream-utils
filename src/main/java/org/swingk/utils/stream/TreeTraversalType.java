package org.swingk.utils.stream;

public enum TreeTraversalType {
    PRE_ORDER {
        @Override
        public TreeStructureIterable createIterable(TreeStructure treeStructure) {
            return new TreeStructureIterablePreOrder(treeStructure);
        }
    },
    POST_ORDER {
        @Override
        public TreeStructureIterable createIterable(TreeStructure treeStructure) {
            return new TreeStructureIterablePostOrder(treeStructure);
        }
    };

    public abstract TreeStructureIterable createIterable(TreeStructure treeStructure);
}
