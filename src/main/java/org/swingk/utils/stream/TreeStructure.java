package org.swingk.utils.stream;

/**
 * Describes generic tree-like structure.
 *
 * @see javax.swing.tree.TreeModel
 */
public interface TreeStructure {
    Object getRoot();

    Object getChild(Object parent, int index);

    int getChildCount(Object parent);
}
