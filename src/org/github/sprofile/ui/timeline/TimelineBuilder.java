package org.github.sprofile.ui.timeline;

import org.github.sprofile.Context;
import org.github.sprofile.io.ObservationListener;
import org.github.sprofile.ui.summary.SummaryTableRow;

import java.util.*;

public class TimelineBuilder implements ObservationListener {
    static class PointInTime {
        final long timestamp;
        final StackTraceElement[] trace;
        final Context context;

        PointInTime(long timestamp, StackTraceElement[] trace, Context context) {
            this.timestamp = timestamp;
            this.trace = trace;
            this.context = context;
        }
    }

    static class ThreadDetails {
        String process;
        String threadName;
        long threadId;
        Date start;
        long lastTimestamp;
        int samples;
        List<PointInTime> points = new ArrayList();
    }

    Map<Long, ThreadDetails> threadDetailsMap = new HashMap();

    @Override
    public void observe(long timestamp, long threadId, String threadName, Thread.State threadState, StackTraceElement[] trace, Context context) {
        ThreadDetails threadDetails = threadDetailsMap.get(threadId);

        if (threadDetails == null) {
            threadDetails = new ThreadDetails();
            threadDetails.threadName = threadName;
            threadDetails.threadId = threadId;
            threadDetails.process = "unknown";
            threadDetails.start = new Date(timestamp);
            threadDetailsMap.put(threadId, threadDetails);
        }
        threadDetails.lastTimestamp = timestamp;
        threadDetails.samples++;

        threadDetails.points.add(new PointInTime(timestamp, trace, context));
    }

    @Override
    public void collectionTime(int milliseconds) {
    }

    public List<SummaryTableRow> getThreads() {
        List<SummaryTableRow> threads = new ArrayList();
        for (ThreadDetails details : threadDetailsMap.values()) {
            threads.add(new SummaryTableRow(details.process, details.threadId, details.threadName, details.samples, details.start, new Date(details.lastTimestamp), makeTimeline(details.points)));
        }
        return threads;
    }

    protected Timeline makeTimeline(List<PointInTime> pit) {

        long[] timestamps = new long[pit.size()];
        StackTraceElement[][] traces = new StackTraceElement[pit.size()][];
        Context[] contexts = new Context[pit.size()];

        for (int i = 0; i < pit.size(); i++) {
            PointInTime p = pit.get(i);
            timestamps[i] = p.timestamp;
            traces[i] = p.trace;
            contexts[i] = p.context;
        }

        return new Timeline(timestamps, traces, contexts);
    }
}
