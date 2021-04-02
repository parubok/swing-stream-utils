package org.swingk.utils.stream;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Subclass of {@link TreePath} which adds some convenience methods.
 */
public class KTreePath extends TreePath {

    protected KTreePath() {
        super();
    }

    /**
     * Static factory method.
     */
    public static KTreePath of(Object root, Object... components) {
        List<Object> list = new ArrayList<>(1 + components.length);
        list.add(root);
        Collections.addAll(list, components);
        return new KTreePath(list);
    }

    /**
     * Constructor.
     *
     * @param path Non-empty collection with the path components.
     */
    public KTreePath(Collection<?> path) {
        super(path.toArray());
    }

    /**
     * Constructor.
     *
     * @param path Source path. Not null.
     */
    public KTreePath(TreePath path) {
        super(path.getPath());
    }

    @Override
    public KTreePath pathByAddingChild(Object child) {
        TreePath treePath = super.pathByAddingChild(child);
        return new KTreePath(treePath);
    }

    @Override
    public KTreePath getParentPath() {
        TreePath parentPath = super.getParentPath();
        return parentPath != null ? new KTreePath(parentPath) : null;
    }

    /**
     * @param component Object to check if it is a component of the path.
     * @return true if the specified object is a component of the path.
     * @see #indexOf(Object)
     */
    public boolean hasComponent(Object component) {
        return indexOf(component) > -1;
    }

    /**
     * @return Index of the specified component in the path (0 for root, etc.) or -1 if the path doesn't contain this
     * component.
     * @see #hasComponent(Object)
     */
    public int indexOf(Object component) {
        int index = -1;
        final int count = getPathCount();
        for (int i = 0; i < count; i++) {
            if (Objects.equals(getPathComponent(i), component)) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * @return List with components of this path.
     * @param <E> Type of the path components.
     * @throws ClassCastException If there is a path component that is not an instance of the specified type.
     */
    public <E> List<E> asList(Class<E> componentClass) {
        Object[] p = getPath();
        List<E> list = new ArrayList<>(p.length);
        for (Object component : p) {
            list.add(componentClass.cast(component));
        }
        return list;
    }

    /**
     * @return List with components of this path.
     */
    public List<?> asList() {
        return asList(Object.class);
    }

    /**
     * Overloaded version of {@link TreePath#getLastPathComponent()} which allows to specify type of the component.
     *
     * @param <E> Type of the last path component.
     * @return The last path component casted to the specified type.
     * @throws ClassCastException If the component is not an instance of the specified type.
     */
    public <E> E getLastPathComponent(Class<E> componentClass) {
        Object lastComponent = getLastPathComponent();
        return componentClass.cast(lastComponent);
    }

    /**
     * @return True is the specified object is the last component of this path, false otherwise.
     */
    public boolean isLastComponent(Object component) {
        return Objects.equals(getLastPathComponent(), component);
    }
}
