package org.github.sprofile.ui;

import org.github.sprofile.Context;
import org.github.sprofile.Details;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;


public class TimelinePane extends JPanel {
    Timeline model;

    public TimelinePane(Timeline model) {
        super();
        this.model = model;
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

//            @Override
//            public void mouseMoved(MouseEvent mouseEvent) {
//            }
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
        model.setSelection(Math.min(up, down), Math.max(up, down) + 1);
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

        int selectionStart = model.getSelectionStart();
        int selectionStop = model.getSelectionStop();

        // scale = elapsed / width
        int lastX = 0;
        for (int i = 0; i < model.getSampleCount(); i++) {
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

            lastX = x;
        }
    }

    private void paintBar(Graphics g, Color fg, Color bg, int x, int y, int w, int h, int full_h) {
        g.setColor(fg);
        g.fillRect(x, y, w, h);
        g.setColor(bg);
        g.fillRect(x, y + h, w, full_h - h);
    }

    public static void main(String[] args) {

        int steps = 100;
        Context[] contexts = new Context[steps];
        long timestamps[] = new long[steps];
        StackTraceElement[][] traces = new StackTraceElement[steps][];

        StackTraceElement element = new StackTraceElement("org.foo.Foo", "bar", "Foo.java", 100);
        Context context = new Context(new Details("alpha", "beta"), null);
        for (int i = 0; i < steps; i++) {
            timestamps[i] = i * 10000;
            contexts[i] = context;

            int traceDepth = i % 4 + 1;
            traces[i] = new StackTraceElement[traceDepth];
            for (int j = 0; j < traceDepth; j++) {
                traces[i][j] = element;
            }
        }

        Timeline timeline = new Timeline(timestamps, traces, contexts);
        timeline.setSelection(10, 14);

        JFrame frame = new JFrame("test");
        frame.setSize(600, 400);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new TimelinePane(timeline), BorderLayout.CENTER);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
