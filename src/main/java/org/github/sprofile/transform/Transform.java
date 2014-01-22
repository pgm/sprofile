package org.github.sprofile.transform;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/26/12
 * Time: 8:29 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Transform {
    public StackTraceElement[] transform(StackTraceElement[] trace);
}
