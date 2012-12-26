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

        int w = (int) ((getWidth() - insets.right - insets.left) * fraction);

        graphics.setColor(this.getForeground());
        graphics.fillRect(insets.left, insets.top, w, getHeight() - insets.bottom - insets.top);
    }

    public void setFraction(double fraction) {
        this.fraction = fraction;
    }

    public BarComponent() {
        this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    @Override
    public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b2, int i, int i2) {
        this.fraction = ((Number) o).floatValue();
        return this;
    }

}
