package org.github.sprofile.ui.timeline;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;


public class TimelinePane extends JComponent {
    Timeline model;
    SelectionModel selectionModel;

    public TimelinePane(SelectionModel selectionModel, Timeline model) {
        super();
        this.model = model;
        this.selectionModel = selectionModel;

        MouseInputAdapter listener = new MouseInputAdapter() {
            int mouseDownIndex;
            int mouseIndex;

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                mouseDownIndex = mapClientXToIndex(mouseEvent.getX());
                mouseIndex = mapClientXToIndex(mouseEvent.getX());
                updateSelection(mouseDownIndex, mouseIndex);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                mouseIndex = mapClientXToIndex(mouseEvent.getX());
                updateSelection(mouseDownIndex, mouseIndex);
            }

            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                mouseIndex = mapClientXToIndex(mouseEvent.getX());
                updateSelection(mouseDownIndex, mouseIndex);
            }

        };
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);
    }

    protected int mapClientXToIndex(int x) {
        long elapsed = model.getElapsedTime();
        long start = model.getTime(0);

        long time = start + x * elapsed / getWidth();
        return model.getIndexOf(time);
    }

    protected void updateSelection(int down, int up) {
        selectionModel.setSelection(Math.min(up, down), Math.max(up, down) + 1);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        int height = getHeight();
        int width = getWidth();

        graphics.setColor(Color.BLACK);
        graphics.drawLine(0, height / 2, width, height / 2);

        long elapsed = model.getElapsedTime();
        long start = model.getTime(0);

        int maxTraceDepth = model.getMaxTraceDepth() + 1;
        int maxContextDepth = model.getMaxContextDepth() + 1;

        int selectionStart = selectionModel.getSelectionStart();
        int selectionStop = selectionModel.getSelectionStop();

        // scale = elapsed / width
        Rectangle clip = graphics.getClipBounds();
        int lastX = clip.x - 1;
        long startingTime = (lastX * elapsed / width) + start;
        int firstIndex = model.getIndexOf(startingTime);
        for (int i = firstIndex; i < model.getSampleCount(); i++) {
            long time = model.getTime(i);
            int x = (int) ((time - start) * width / elapsed);
            // don't bother with overdraw.  If we've already draw values for this column of pixels, move on
            if (x <= lastX)
                continue;

            Color bg = Color.WHITE;
            Color fg = Color.BLACK;

            if (i >= selectionStart && i < selectionStop) {
                fg = Color.BLUE;
                bg = Color.YELLOW;
            }

            int traceDepth = model.getTraceDepth(i);
            paintBar(graphics, fg, bg, lastX, 0, x - lastX, traceDepth * height / 2 / maxTraceDepth, height / 2);

            int contextDepth = model.getContextDepth(i);
            paintBar(graphics, fg, bg, lastX, height / 2, x - lastX, contextDepth * height / 2 / maxContextDepth, height / 2);

            // don't bother drawing past the edge of the clip region
            if (x > clip.x + clip.width)
                break;

            lastX = x;
        }
    }

    private void paintBar(Graphics g, Color fg, Color bg, int x, int y, int w, int h, int full_h) {
        g.setColor(fg);
        g.fillRect(x, y, w, h);
        g.setColor(bg);
        g.fillRect(x, y + h, w, full_h - h);
    }

    public Timeline getModel() {
        return model;
    }
}
