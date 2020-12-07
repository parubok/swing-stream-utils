package org.swingk.utils.stream;

import javax.swing.tree.TreeModel;

/**
 * Describes generic tree-like structure.
 * Has only a subset of methods comparing to {@link javax.swing.tree.TreeModel}.
 *
 * @see javax.swing.tree.TreeModel
 */
public interface TreeStructure {
    /**
     * @see TreeModel#getRoot()
     */
    Object getRoot();

    /**
     * @see TreeModel#getChild(Object, int)
     */
    Object getChild(Object parent, int index);

    /**
     * @see TreeModel#getChildCount(Object)
     */
    int getChildCount(Object parent);
}
