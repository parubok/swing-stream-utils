package org.swingk.utils.stream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class AbstractTreePathIterator implements Iterator<KTreePath> {
    /**
     * Singleton object to designate empty tree path.
     */
    protected static final KTreePath EMPTY_PATH = new KTreePath();

    protected KTreePath currentPath = EMPTY_PATH;
    protected KTreePath nextPath;
    protected boolean completed;

    protected static List<Object> getChildren(TreeStructure treeStructure, Object parent) {
        final int c = treeStructure.getChildCount(parent);
        assert c > 0;
        List<Object> children = new ArrayList<>(c);
        for (int i = 0; i < c; i++) {
            children.add(treeStructure.getChild(parent, i));
        }
        return children;
    }

    /**
     * @return The next tree path or {@link #EMPTY_PATH} to signal end of the iteration.
     */
    protected abstract KTreePath getNextPath();

    @Override
    public boolean hasNext() {
        if (completed) {
            return false;
        }
        if (nextPath == null) {
            nextPath = getNextPath();
        }
        return nextPath != EMPTY_PATH;
    }

    @Override
    public KTreePath next() {
        if (completed) {
            throw new NoSuchElementException();
        }
        currentPath = nextPath != null ? nextPath : getNextPath();
        nextPath = getNextPath(); // current path has changed - update nextPath
        completed = (nextPath == EMPTY_PATH);
        return currentPath;
    }
}
