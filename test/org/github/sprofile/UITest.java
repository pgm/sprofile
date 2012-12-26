package org.github.sprofile;

import org.github.sprofile.io.ProfileParser;
import org.github.sprofile.ui.summary.Controller;
import org.github.sprofile.ui.summary.SummaryForm;
import org.github.sprofile.ui.timeline.TimelineBuilder;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/23/12
 * Time: 9:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class UITest {


    public static void main(String[] args) throws Exception {
        TimelineBuilder listener = new TimelineBuilder();
        ProfileParser pp = new ProfileParser("samples-20121223-210002.788",
                listener);
        pp.read();

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
