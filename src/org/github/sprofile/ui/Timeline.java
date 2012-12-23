package org.github.sprofile.ui;

import org.github.sprofile.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A timeline for a single thread
 */
public class Timeline {
    final long[] timestamps;
    final StackTraceElement[][] traces;
    final Context[] contexts;

    final int maxContextDepth;
    final int maxTraceDepth;

    int selectionStart;
    int selectionStop;

    List<SelectionListener> selectionListenerList = new ArrayList();

    public Timeline(long[] timestamps, StackTraceElement[][] traces, Context[] contexts) {
        this.timestamps = timestamps;
        this.traces = traces;
        this.contexts = contexts;

        int maxTraceDepth = 0;
        int maxContextDepth = 0;
        for (int i = 0; i < getSampleCount(); i++) {
            maxTraceDepth = Math.max(this.traces[i].length, maxTraceDepth);
            maxContextDepth = Math.max(this.contexts[i].getDepth(), maxContextDepth);
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

    public int getContextDepth(int index) {
        return contexts[index].getDepth();
    }

    public int getTraceDepth(int index) {
        return traces[index].length;
    }

    public int getSelectionStart() {
        return this.selectionStart;
    }

    public int getSelectionStop() {
        return selectionStop;
    }

    public void setSelection(int selectionStart, int selectionStop) {
        this.selectionStart = selectionStart;
        this.selectionStop = selectionStop;
        fireSelectionChanged();
    }

    protected void fireSelectionChanged() {
        for (SelectionListener l : selectionListenerList) {
            l.selectionChanged();
        }
    }

    public void addSelectionListener(SelectionListener listener) {
        this.selectionListenerList.add(listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        this.selectionListenerList.remove(listener);
    }
}
