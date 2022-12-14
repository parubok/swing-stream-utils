package io.github.parubok.stream;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import java.util.Objects;

/**
 * {@link TreeStructure} that wraps {@link TreeModel}.
 */
public class TreeModelTreeStructure implements TreeStructure {

    private final TreeModel treeModel;
    private final boolean detectChanges;
    private boolean changed;

    /**
     * Listens to the tree model changes during iteration.
     */
    private final TreeModelListener listener = new TreeModelListener() {
        @Override
        public void treeNodesChanged(TreeModelEvent e) {
            // do nothing
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            onChange();
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
            onChange();
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            onChange();
        }

        private void onChange() {
            changed = true;
            treeModel.removeTreeModelListener(this); // change detected - no need to continue listening
        }
    };

    public TreeModelTreeStructure(TreeModel treeModel, boolean detectChanges) {
        this.treeModel = Objects.requireNonNull(treeModel);
        this.detectChanges = detectChanges;
    }

    @Override
    public Object getRoot() {
        return treeModel.getRoot();
    }

    @Override
    public Object getChild(Object parent, int index) {
        return treeModel.getChild(parent, index);
    }

    @Override
    public int getChildCount(Object parent) {
        return treeModel.getChildCount(parent);
    }

    @Override
    public void startListeningForChanges() {
        if (detectChanges) {
            treeModel.addTreeModelListener(listener);
        }
    }

    @Override
    public void stopListeningForChanges() {
        if (detectChanges) {
            treeModel.removeTreeModelListener(listener);
        }
    }

    @Override
    public boolean isChangeDetected() {
        return changed;
    }
}
