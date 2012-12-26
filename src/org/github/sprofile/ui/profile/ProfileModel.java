package org.github.sprofile.ui.profile;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/26/12
 * Time: 9:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProfileModel extends AbstractTreeTableModel {
    public ProfileModel(Object root) {
        super(root);
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Method";
            case 1:
                return "Samples";
            case 2:
                return "Percentage";
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(Object o, int column) {
        StackTreeNode e = (StackTreeNode) o;
        switch (column) {
            case 0:
                if (e.getElement() == null)
                    return null;
                return e.getElement().toString();
            case 1:
                return e.getSamples();
            case 2:
                return e.getSamplePercentage();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getChild(Object o, int index) {
        StackTreeNode e = (StackTreeNode) o;
        return e.getChildren().get(index);
    }

    @Override
    public int getChildCount(Object o) {
        StackTreeNode e = (StackTreeNode) o;
        return e.getChildren().size();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        StackTreeNode e = (StackTreeNode) parent;
        return e.getChildren().indexOf(child);
    }
}
