package org.github.sprofile.io;

import org.github.sprofile.Context;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/27/12
 * Time: 11:43 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ProfileVisitor {
    public void handleTimestamp(long timestamp);

    public void handleTraceElement(int id, String className, String filename, String methodName, int lineNumber);

    public void handleTrace(int id, StackTraceElement[] elements);

    public void handleSampledTrace(long threadId, Thread.State state, StackTraceElement[] trace, Context context);

    public void handleThreadName(long threadId, String threadName);

    public void handleAtom(int id, String value);

    public void handleCollectionTime(int elapsed);

    public void handleProcessInfo(String name);

    public void handleContext(int id, Context context);

}
