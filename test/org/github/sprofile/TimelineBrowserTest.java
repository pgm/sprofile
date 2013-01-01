package org.github.sprofile;

import org.github.sprofile.ui.timeline.SelectionModel;
import org.github.sprofile.ui.timeline.Timeline;
import org.github.sprofile.ui.timeline.TimelinePane;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/26/12
 * Time: 8:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimelineBrowserTest {
    public static void main(String[] args) {

        int steps = 100;
        Context[] contexts = new Context[steps];
        long timestamps[] = new long[steps];
        StackTraceElement[][] traces = new StackTraceElement[steps][];

        StackTraceElement element = new StackTraceElement("org.foo.Foo", "bar", "Foo.java", 100);
        Context context = new Context(1, new Details("alpha", "beta"), null);
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

        JFrame frame = new JFrame("test");
        frame.setSize(600, 400);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new TimelinePane(new SelectionModel(), timeline), BorderLayout.CENTER);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

}
