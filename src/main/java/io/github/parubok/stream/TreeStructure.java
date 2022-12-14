package io.github.parubok.stream;

import javax.swing.tree.TreeModel;

/**
 * Allows to access generic tree-like structure in read-only mode.
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

    /**
     * Called once before the iteration starts.
     */
    default void startListeningForChanges() {
    }

    /**
     * Called once after the iteration ends.
     */
    default void stopListeningForChanges() {
    }

    /**
     * If the structure has been changed during iteration.
     */
    default boolean isChangeDetected() {
        return false;
    }
}
