package org.github.sprofile.ui;

import org.github.sprofile.io.SamplesParser;
import org.github.sprofile.ui.summary.Controller;
import org.github.sprofile.ui.summary.SummaryForm;
import org.github.sprofile.ui.timeline.TimelineBuilder;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: pmontgom
 * Date: 1/22/14
 * Time: 9:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        TimelineBuilder listener = new TimelineBuilder();

        for(String filename : args) {
            SamplesParser pp = new SamplesParser(filename,
                    listener);

            pp.read();
        }

        JFrame frame = new JFrame("test");
        frame.setSize(600, 400);
//        frame.getContentPane().setLayout(new BorderLayout());
//        frame.getContentPane().add(new TimelinePane(listener.getTimeline(1)), BorderLayout.CENTER);
        SummaryForm form = new SummaryForm(new Controller(), listener.getThreads());
        frame.getContentPane().add(form.rootPanel);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

}
