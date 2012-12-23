package org.github.sprofile.io;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/22/12
 * Time: 5:44 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ObservationListener {
    public void observe(long timestamp, long threadId, String threadName,
                        Thread.State threadState, StackTraceElement[] trace);

    public void collectionTime(int milliseconds);
}
