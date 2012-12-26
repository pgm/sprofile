package org.github.sprofile;

import org.github.sprofile.io.Writer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class Profiler {
    final long sleepTime;

    boolean running = false;
    Thread samplerThread;

    Writer writer;

    Map<Thread, Context> contexts = new WeakHashMap();

    long lastFlush = System.currentTimeMillis();
    long maxTimeBetweenFlushes = 60 * 1000;

    public Profiler(long sleepTime, Writer writer) {
        this.writer = writer;
        this.sleepTime = sleepTime;
    }

    /**
     * Called when we want to write a snapshot of the threads to the log
     *
     * @throws IOException
     */
    public void sample() throws IOException {
        long timestamp = System.currentTimeMillis();

        // synchronize to ensure that the stack trace is consistent with the state of the contexts objects
        Map<Thread, Context> contextMapSnapshot;
        Map<Thread, StackTraceElement[]> dump;
        synchronized (this) {
            contextMapSnapshot = new HashMap(contexts);
            dump = Thread.getAllStackTraces();
        }
        writer.write(timestamp, dump, contextMapSnapshot);

        // check to see if it's been a while since we last flushed
        if ((System.currentTimeMillis() - lastFlush) > maxTimeBetweenFlushes) {
            writer.flush();
        }

        long finalTimestamp = System.currentTimeMillis();
        writer.writeCollectionFinished((int) ((finalTimestamp - timestamp)));
    }

    /**
     * Call the following method, providing some context which will be record (only if a sample happens to be taken while
     * within that call)
     * <p/>
     * For example, if instrumenting a JDBC call, one could provide the query string in the context so that when inspecting
     * the log, we'll be able to see which query was taking a long time.
     * <p/>
     * As long as the callee has not returned (or thrown an exception) the context provided will be recorded as the current
     * context as part of snapshots.
     *
     * @param context keys and values that provide general contextual information about the call being handled.
     * @param callee  The method to invoke
     */
    public void callWithContext(Details context, Runnable callee) {
        Thread thread = Thread.currentThread();

        // save this context associated with the current thread
        Context prevContext;
        synchronized (this) {
            prevContext = contexts.get(thread);
            contexts.put(thread, new Context(context, prevContext));
        }

        try {
            callee.run();
        } finally {
            // restore the previous context
            synchronized (this) {
                contexts.put(thread, prevContext);
            }
        }
    }

    /**
     * Invoked from background thread to take snapshots
     */
    protected void pollSnapshotsUntilStopped() {
        synchronized (this) {
            running = true;
            samplerThread = Thread.currentThread();
            while (running) {
                try {
                    sample();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    break;
                }
                try {
                    this.wait(sleepTime);
                } catch (InterruptedException ex) {
                    // continue. Wake up may have occurred because process was
                    // shutting down
                }
            }
        }
    }

    protected void registerShutdownHook() {
        // request we get notified when the vm is shutting down.  Not strictly necessary
        // but nice to gracefully cleanup and exit.  This runs the risk that we might
        // prevent the JVM from exiting.  If we have any trouble, we can always pull this code.
        Thread shutdownThread = new Thread(new Runnable() {
            public void run() {
                stop();
            }
        });
        shutdownThread.setName("sprofiler shutdown hook");
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    /**
     * Start a thread running in the background which will periodically take snapshots and write
     * then to the provided log directory.
     *
     * @throws IOException
     */
    public void start() {
        registerShutdownHook();

        Thread mainThread = new Thread(new Runnable() {
            public void run() {
                pollSnapshotsUntilStopped();
            }
        });
        mainThread.setName("sprofiler");
        mainThread.setDaemon(true);
        mainThread.start();
    }

    /**
     * Stop the daemon thread running
     */
    public void stop() {
        synchronized (this) {
            if (running) {
                running = false;
                this.notifyAll();
            }
        }

        // wait for thread to exit before resuming
        try {
            if (samplerThread != null)
                samplerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
