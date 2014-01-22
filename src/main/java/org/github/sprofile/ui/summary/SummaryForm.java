package org.github.sprofile.ui.summary;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class SummaryForm {
    private JTextField elementFilterTextField;
    private JButton viewTimelineButton;
    private JTable table;
    private JButton viewAggregateTimesButton;
    private JTextField traceFilterTextField;
    public JPanel rootPanel;

    private final List<SummaryTableRow> rows;
    private final Controller controller;

    public SummaryForm(Controller controller, List<SummaryTableRow> rows) {
        this.controller = controller;
        this.rows = rows;
        viewAggregateTimesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SummaryForm.this.controller.showAggregate(getSelectedRows(), elementFilterTextField.getText(), traceFilterTextField.getText());
            }
        });
        viewTimelineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SummaryForm.this.controller.showTimeline(SummaryForm.this.rows.get(table.getSelectedRow()), elementFilterTextField.getText(), traceFilterTextField.getText());
            }
        });
        updateButtonState();
    }

    private List<SummaryTableRow> getSelectedRows() {
        List<SummaryTableRow> rows = new ArrayList();

        for (int rowIndex : table.getSelectedRows()) {
            rows.add(this.rows.get(rowIndex));
        }

        return rows;
    }

    protected void updateButtonState() {
        int rowCount = table.getSelectedRows().length;

        viewTimelineButton.setEnabled(rowCount == 1);
        viewAggregateTimesButton.setEnabled(rowCount >= 1);
    }

    private void createUIComponents() {
        SummaryTableModel model = new SummaryTableModel(rows);
        table = new JTable();
        table.setModel(model);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                updateButtonState();
            }
        });
    }

}
