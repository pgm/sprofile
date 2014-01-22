package org.github.sprofile.ui.timeline;

import org.github.sprofile.ui.summary.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/26/12
 * Time: 5:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimelineBrowser {
    private JButton unzoomButton;
    private JButton showAggregatedProfileButton;
    private JPanel rootPanel;
    private JLabel startTimeLabel;
    private JLabel endTimeLabel;
    private JLabel samplesLabel;
    private TimelinePane timelinePane1;
    private JButton zoomButton;
    private JScrollPane timelineContainer;
    private Timeline timeline;
    private SelectionModel selectionModel;
    Controller controller;

    final float ZOOM_SCALE = 1.50f;

    public TimelineBrowser(Controller controller, Timeline timeline) {
        // todo: split timeline zoom and selection model out of timeline
        this.timeline = timeline;
        this.controller = controller;

        showAggregatedProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Controller controller = TimelineBrowser.this.controller;
                Timeline timeline = TimelineBrowser.this.timeline;

                controller.showAggregate(timeline, selectionModel.getSelectionStart(), selectionModel.getSelectionStop());
            }
        });

        selectionModel.addSelectionListener(new SelectionListener() {
            @Override
            public void selectionChanged() {
                Timeline timeline = TimelineBrowser.this.timeline;
                startTimeLabel.setText(new Date(timeline.getTime(selectionModel.getSelectionStart())).toString());
                endTimeLabel.setText(new Date(timeline.getTime(selectionModel.getSelectionStop())).toString());
                samplesLabel.setText(Integer.toString(selectionModel.getSelectionStop() - selectionModel.getSelectionStart()));
            }
        });
        unzoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                zoom(1.0f / ZOOM_SCALE);
            }
        });
        zoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                zoom(ZOOM_SCALE);
            }
        });
    }


    private void zoom(float scale) {
        int newWidth = Math.max((int) (timelinePane1.getWidth() * scale), timelineContainer.getWidth());
        timelinePane1.setPreferredSize(new Dimension(newWidth, timelineContainer.getHeight()));
        timelinePane1.revalidate();
    }

//    public static void main(String[] args) {
//        JFrame frame = new JFrame("TimelineBrowser");
//        frame.setContentPane(new TimelineBrowser().rootPanel);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//    }

    private void createUIComponents() {
        this.selectionModel = new SelectionModel();
        timelinePane1 = new TimelinePane(selectionModel, timeline);
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }
}
