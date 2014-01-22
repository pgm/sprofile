package org.github.sprofile.io;

import org.github.sprofile.Context;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/27/12
 * Time: 11:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SamplesParser extends ProfileParser {

    static class Adapter implements ProfileVisitor {
        final SampleListener listener;
        long timestamp;

        Adapter(SampleListener listener) {
            this.listener = listener;
        }

        @Override
        public void handleTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public void handleTraceElement(int id, String className, String filename, String methodName, int lineNumber) {
        }

        @Override
        public void handleTrace(int id, StackTraceElement[] elements) {
        }

        @Override
        public void handleSampledTrace(long threadId, Thread.State state, StackTraceElement[] trace, Context context) {
            listener.sample(timestamp, threadId, state,
                    trace, context);
        }

        @Override
        public void handleThreadName(long threadId, String threadName) {
            listener.threadName(threadId, threadName);
        }

        @Override
        public void handleAtom(int id, String value) {
        }

        @Override
        public void handleCollectionTime(int elapsed) {
        }

        @Override
        public void handleProcessInfo(String name) {
            listener.processDescription(name);
        }

        @Override
        public void handleContext(int prevContextId, Context context) {
        }
    }

    public SamplesParser(String filename, SampleListener listener) throws IOException {
        super(filename, new Adapter(listener));
    }
}
