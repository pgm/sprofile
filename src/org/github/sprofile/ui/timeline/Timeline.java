package org.github.sprofile.ui.timeline;

import org.github.sprofile.Context;

import java.util.Arrays;

/**
 * A timeline for a single thread
 */
public class Timeline {
    final long[] timestamps;
    final StackTraceElement[][] traces;
    final Context[] contexts;

    final int maxContextDepth;
    final int maxTraceDepth;


    public Timeline(long[] timestamps, StackTraceElement[][] traces, Context[] contexts) {
        this.timestamps = timestamps;
        this.traces = traces;
        this.contexts = contexts;

        int maxTraceDepth = 0;
        int maxContextDepth = 0;
        for (int i = 0; i < getSampleCount(); i++) {
            maxTraceDepth = Math.max(getTraceDepth(i), maxTraceDepth);
            maxContextDepth = Math.max(getContextDepth(i), maxContextDepth);
        }
        this.maxTraceDepth = maxTraceDepth;
        this.maxContextDepth = maxContextDepth;
    }

    public int getIndexOf(long time) {
        int i = Arrays.binarySearch(timestamps, time);
        if (i < 0) {
            i = -(i + 1);
        }

        return i;
    }

    public int getSampleCount() {
        return timestamps.length;
    }

    public long getElapsedTime() {
        return timestamps[timestamps.length - 1] - timestamps[0];
    }

    public long getTime(int index) {
        return timestamps[index];
    }

    public int getMaxTraceDepth() {
        return maxTraceDepth;
    }

    public int getMaxContextDepth() {
        return maxContextDepth;
    }

    public Context getContext(int index) {
        return contexts[index];
    }

    public StackTraceElement[] getTrace(int index) {
        return traces[index];
    }

    public int getContextDepth(int index) {
        if (contexts[index] != null)
            return contexts[index].getDepth();
        return 0;
    }

    public int getTraceDepth(int index) {
        if (traces[index] != null) {
            return traces[index].length;
        }
        return 0;
    }

}
