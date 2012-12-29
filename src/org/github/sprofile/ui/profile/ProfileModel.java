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
                return "Package";
            case 2:
                return "Samples";
            case 3:
                return "Percentage";
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(Object o, int column) {
        StackTreeNode e = (StackTreeNode) o;
        switch (column) {
            case 0:
                if (e.getElement() == null)
                    return null;
                StackTraceElement ste = e.getElement();
                return ste;
            case 1:
                if (e.getElement() == null)
                    return null;
                StackTraceElement ste2 = e.getElement();
                return ste2.getClassName();
            case 2:
                return e.getSamples();
            case 3:
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
