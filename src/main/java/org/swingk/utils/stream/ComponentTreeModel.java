package org.swingk.utils.stream;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Presents component tree as {@link TreeModel}.
 */
final class ComponentTreeModel implements TreeModel {
    private final Component root;

    ComponentTreeModel(Component root) {
        this.root = Objects.requireNonNull(root);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((Container) parent).getComponent(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return (parent instanceof Container) ? ((Container) parent).getComponentCount() : 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        // do nothing
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        Container container = (Container) parent;
        List<Component> c = Arrays.asList(container.getComponents());
        return c.indexOf(child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        // do nothing
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        // do nothing
    }
}
