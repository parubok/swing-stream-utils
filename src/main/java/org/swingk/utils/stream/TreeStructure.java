package org.swingk.utils.stream;

/**
 * Describes generic tree-like structure.
 * Has only a subset of methods comparing to {@link javax.swing.tree.TreeModel}.
 *
 * @see javax.swing.tree.TreeModel
 */
public interface TreeStructure {
    Object getRoot();

    Object getChild(Object parent, int index);

    int getChildCount(Object parent);
}
