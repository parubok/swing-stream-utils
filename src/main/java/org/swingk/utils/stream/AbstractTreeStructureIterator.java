package org.swingk.utils.stream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

abstract class AbstractTreeStructureIterator implements Iterator<KTreePath> {

    /**
     * Singleton object to designate empty tree path.
     */
    static final KTreePath EMPTY_PATH = new KTreePath();

    final TreeStructure treeStructure;
    KTreePath currentPath = EMPTY_PATH;
    KTreePath nextPath;
    boolean completed;

    AbstractTreeStructureIterator(TreeStructure treeStructure) {
        this.treeStructure = Objects.requireNonNull(treeStructure);
    }

    static List<Object> getChildren(TreeStructure treeStructure, Object parent) {
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
    abstract KTreePath getNextPath();

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
