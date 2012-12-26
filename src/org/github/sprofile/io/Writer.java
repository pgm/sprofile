package org.github.sprofile.io;

import org.github.sprofile.Context;

import java.io.IOException;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/23/12
 * Time: 3:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Writer {
    public void flush();

    public void close();

    public void writeCollectionFinished(int time);

    public void write(long timestamp, Map<Thread, StackTraceElement[]> dump, Map<Thread, Context> contexts) throws IOException;

}
