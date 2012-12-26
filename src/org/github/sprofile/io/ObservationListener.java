package org.github.sprofile.io;

import org.github.sprofile.Context;

public interface ObservationListener {
    public void observe(long timestamp, long threadId, String threadName,
                        Thread.State threadState, StackTraceElement[] trace, Context context);

    public void collectionTime(int milliseconds);
}
