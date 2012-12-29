package org.github.sprofile.ui.profile;

import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeCellRenderer;
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

        DefaultTreeCellRenderer treeCellRenderer = new DefaultTreeCellRenderer();
        treeCellRenderer.setLeafIcon(null);
        treeCellRenderer.setOpenIcon(null);
        treeCellRenderer.setClosedIcon(null);

        JXTreeTable tree = new JXTreeTable();
        tree.setShowsRootHandles(true);
        tree.setTreeCellRenderer(treeCellRenderer);
        tree.setTreeTableModel(treeModel);

        DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
        TableColumn methodColumn = new TableColumn(0, 300, tree.getDefaultRenderer(String.class), tree.getDefaultEditor(String.class));
        methodColumn.setHeaderValue(treeModel.getColumnName(0));

        TableColumn packageColumn = new TableColumn(1, 50, tree.getDefaultRenderer(String.class), tree.getDefaultEditor(String.class));
        packageColumn.setHeaderValue(treeModel.getColumnName(1));

        TableColumn samplesColumn = new TableColumn(2, 50, tree.getDefaultRenderer(Integer.class), tree.getDefaultEditor(Integer.class));
        samplesColumn.setHeaderValue(treeModel.getColumnName(2));

        TableColumn percentColumn = new TableColumn(3, 100, new BarComponent(), tree.getDefaultEditor(Float.class));
        percentColumn.setHeaderValue(treeModel.getColumnName(3));

        columnModel.addColumn(methodColumn);
        columnModel.addColumn(packageColumn);
        columnModel.addColumn(samplesColumn);
        columnModel.addColumn(percentColumn);
        tree.setColumnModel(columnModel);

        //tree.set

        JFrame frame = new JFrame("Aggregated Profile");
        frame.setSize(600, 400);
        frame.getContentPane().add(new JScrollPane(tree));
//        frame.pack();

        return frame;
    }

    public static void main(String[] args) throws Exception {

        StackTreeNode child1 = new StackTreeNode(new StackTraceElement("class", "method1", "file", 100), 30, 0.5f, Collections.EMPTY_LIST);
        StackTreeNode child3 = new StackTreeNode(new StackTraceElement("class", "method2", "file", 100), 30, 0.2f, Collections.EMPTY_LIST);
        StackTreeNode child2 = new StackTreeNode(new StackTraceElement("class", "method3", "file", 100), 30, 0.5f, Arrays.asList(child3));

        StackTreeNode root = new StackTreeNode(null, 100, 0.5f, Arrays.asList(child1, child2));

        JFrame frame = createProfileWindow(root);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
