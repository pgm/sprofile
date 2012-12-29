package org.github.sprofile;

import org.github.sprofile.io.ProfileParser;
import org.github.sprofile.io.ProfileVisitor;
import org.github.sprofile.ui.timeline.TimelineBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/23/12
 * Time: 9:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProfileAsText {


    public static void main(String[] args) throws Exception {
        TimelineBuilder listener = new TimelineBuilder();
        ProfileParser pp = new ProfileParser("samples-20121228-110436.610",
                new ProfileVisitor() {
                    @Override
                    public void handleTimestamp(long timestamp) {
                        System.out.println("timestamp: " + timestamp);
                    }

                    @Override
                    public void handleTraceElement(int id, String className, String filename, String methodName, int lineNumber) {
                        System.out.println("trace element: " + id + " " + className + " " + filename + " " + methodName + " " + lineNumber);
                    }

                    @Override
                    public void handleTrace(int id, StackTraceElement[] elements) {
                        System.out.println("trace: " + id + " (" + System.identityHashCode(elements) + ")");
                        for (StackTraceElement element : elements) {
                            System.out.println("  " + element);
                        }
                    }

                    @Override
                    public void handleSampledTrace(long threadId, Thread.State state, StackTraceElement[] trace, Context context) {
                        System.out.println("sampledTrace: " + System.identityHashCode(trace) + " " + System.identityHashCode(context));
                    }

                    @Override
                    public void handleThreadName(long threadId, String threadName) {
                        System.out.println("threadName: " + threadId + " " + threadName);
                    }

                    @Override
                    public void handleAtom(int id, String value) {
                        System.out.println("atom: " + id + " " + value);
                    }

                    @Override
                    public void handleCollectionTime(int elapsed) {
                        System.out.println("collectionTime: " + elapsed);
                    }

                    @Override
                    public void handleProcessInfo(String name) {
                        System.out.println("processInfo: " + name);
                    }

                    @Override
                    public void handleContext(int id, Context context) {
                        System.out.println("context: " + id + " " + context);
                    }
                });
        pp.read();

    }

}
