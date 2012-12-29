package org.github.sprofile.ui.profile;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/26/12
 * Time: 11:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class BarComponent extends JComponent implements TableCellRenderer {
    double fraction;

    @Override
    protected void paintComponent(Graphics graphics) {
        Insets insets = this.getInsets();

        graphics.setColor(this.getBackground());
        graphics.fillRect(0, 0, getWidth(), getHeight());

        int w = (int) ((getWidth() - insets.right - insets.left) * fraction);

        graphics.setColor(this.getForeground());
        graphics.fillRect(insets.left, insets.top, w, getHeight() - insets.bottom - insets.top);
    }

    public void setFraction(double fraction) {
        this.fraction = fraction;
    }

    public BarComponent() {
        this.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    @Override
    public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        if (isSelected) {
            this.setForeground(jTable.getSelectionForeground());
            this.setBackground(jTable.getSelectionBackground());
        } else {
            this.setForeground(jTable.getForeground());
            this.setBackground(jTable.getBackground());
        }
        this.fraction = ((Number) value).floatValue();
        return this;
    }

}
