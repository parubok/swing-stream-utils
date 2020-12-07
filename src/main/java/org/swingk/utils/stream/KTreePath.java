package org.swingk.utils.stream;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * {@link TreePath} with convenience methods.
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

    public KTreePath(Collection<?> path) {
        super(path.toArray());
    }

    public KTreePath(TreePath path) {
        super(path.getPath());
    }

    @Override
    public KTreePath pathByAddingChild(Object child) {
        TreePath treePath = super.pathByAddingChild(child);
        return new KTreePath(treePath);
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
     * @param <E> Type of the last path component.
     * @return The last path component casted to the specified type.
     * @throws ClassCastException If the component is not an instance of the specified type.
     */
    public <E> E getLastPathComponent(Class<E> componentClass) {
        Object component = getLastPathComponent();
        return componentClass.cast(component);
    }
}
