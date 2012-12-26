package org.github.sprofile.ui.summary;

import org.github.sprofile.ui.timeline.Timeline;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/25/12
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class SummaryTableRow {
    final String process;
    final long threadId;
    final String threadName;
    final String thread;
    final int samples;
    final Date start;
    final Date end;
    final Timeline timeline;

    public SummaryTableRow(String process, long threadId, String threadName, int samples, Date start, Date end, Timeline timeline) {
        this.process = process;
        this.thread = threadName + " (" + threadId + ")";
        this.threadId = threadId;
        this.threadName = threadName;
        this.samples = samples;
        this.start = start;
        this.end = end;
        this.timeline = timeline;
    }
}
