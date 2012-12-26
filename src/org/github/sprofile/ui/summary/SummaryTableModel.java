package org.github.sprofile.ui.summary;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/25/12
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class SummaryTableModel extends AbstractTableModel {
    final List<SummaryTableRow> rows;

    public SummaryTableModel(List<SummaryTableRow> rows) {
        this.rows = rows;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int col) {
        SummaryTableRow row = rows.get(rowIndex);
        switch (col) {
            case 0:
                return row.process;
            case 1:
                return row.thread;
            case 2:
                return row.start;
            case 3:
                return row.end;
            case 4:
                return row.samples;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case 0:
                return "Process";
            case 1:
                return "Thread";
            case 2:
                return "start";
            case 3:
                return "end";
            case 4:
                return "Samples";
        }
        throw new UnsupportedOperationException();
    }
}
