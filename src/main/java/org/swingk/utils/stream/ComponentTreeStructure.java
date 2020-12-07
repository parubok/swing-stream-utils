package org.swingk.utils.stream;

import java.awt.Component;
import java.awt.Container;
import java.util.Objects;

/**
 * Presents component tree as {@link TreeStructure}.
 */
final class ComponentTreeStructure implements TreeStructure {
    private final Component root;

    ComponentTreeStructure(Component root) {
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
}
