package org.github.sprofile.io;

import org.github.sprofile.Context;

public interface SampleListener {
    public void processDescription(String name);

    public void threadName(long threadId, String name);

    public void sample(long timestamp, long threadId,
                       Thread.State threadState, StackTraceElement[] trace, Context context);

    public void collectionTime(int milliseconds);
}
