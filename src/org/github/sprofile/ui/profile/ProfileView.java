package org.github.sprofile.ui.profile;

import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/26/12
 * Time: 9:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProfileView {

    public static JFrame createProfileWindow(StackTreeNode root) {
        ProfileModel treeModel = new ProfileModel(root);

        JXTreeTable tree = new JXTreeTable();
        tree.setShowsRootHandles(false);
        tree.setTreeTableModel(treeModel);
        tree.expandAll();

        DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
        TableColumn c1 = new TableColumn(0, 100, tree.getDefaultRenderer(String.class), tree.getDefaultEditor(String.class));
        c1.setHeaderValue(treeModel.getColumnName(0));
        TableColumn c2 = new TableColumn(1, 100, tree.getDefaultRenderer(Integer.class), tree.getDefaultEditor(Integer.class));
        c2.setHeaderValue(treeModel.getColumnName(1));
        TableColumn c3 = new TableColumn(2, 100, new BarComponent(), tree.getDefaultEditor(Float.class));
        c3.setHeaderValue(treeModel.getColumnName(2));

        columnModel.addColumn(c1);
        columnModel.addColumn(c2);
        columnModel.addColumn(c3);
        tree.setColumnModel(columnModel);

        //tree.set

        JFrame frame = new JFrame("test");
        frame.setSize(600, 400);
        frame.getContentPane().add(new JScrollPane(tree));
//        frame.pack();

        return frame;
    }

    public static void main(String[] args) throws Exception {

        StackTreeNode child1 = new StackTreeNode(new StackTraceElement("child1", "method", "file", 100), 30, 0.5f, Collections.EMPTY_LIST);
        StackTreeNode child3 = new StackTreeNode(new StackTraceElement("child3", "method", "file", 100), 30, 0.5f, Collections.EMPTY_LIST);
        StackTreeNode child2 = new StackTreeNode(new StackTraceElement("child2", "method", "file", 100), 30, 0.5f, Arrays.asList(child3));

        StackTreeNode root = new StackTreeNode(null, 100, 0.5f, Arrays.asList(child1, child2));

        JFrame frame = createProfileWindow(root);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
