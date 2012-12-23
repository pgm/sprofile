package org.github.sprofile;

import org.github.sprofile.io.Writer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class Profiler implements Runnable {
    long sleepTime;

    public Profiler(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    static final int MAX_TIME_BETWEEN_FLUSHES = 60 * 1000;

    boolean running = false;
    Thread samplerThread;
    Writer writer;

    Map<Thread, Context> contexts = new WeakHashMap();

    long lastFlush = System.currentTimeMillis();

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

        if (System.currentTimeMillis() - lastFlush > MAX_TIME_BETWEEN_FLUSHES) {
            writer.flush();
        }

        long finalTimestamp = System.currentTimeMillis();
        writer.writeCollectionFinished((int) ((finalTimestamp - timestamp)));
    }

    /**
     * Record an event occurred.   This will always be recorded independent of sampling frequency,
     * so care should be taken to not include too much information or include this too frequently.
     *
     * @param eventName
     * @param parameters
     */
    public void signal(String eventName, Details parameters) {
        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Call the following method, providing some context which will be record (only if a sample happens to be taken while
     * within that call)
     *
     * @param context
     * @param callee
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

    public void run() {
        synchronized (this) {
//            try {
//                out.writeByte(NEW_PROCESS);
//                out.writeUTF(ManagementFactory.getRuntimeMXBean().getName());
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }

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

//        try {
//            out.close();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
    }

    public void start(String filename) throws IOException {

        Thread shutdownThread = new Thread(new Runnable() {
            public void run() {
                stop();
            }
        });
        shutdownThread.setName("sprofiler shutdown hook");
        Runtime.getRuntime().addShutdownHook(shutdownThread);

        Thread mainThread = new Thread(this);
        mainThread.setName("sprofiler");
        mainThread.setDaemon(true);
        mainThread.start();
    }

    public static void agentmain(String agentArgs) {
        String args[] = agentArgs.split(",");
        String path = args[1];
        System.setProperty(WatchAndAttach.SPROFILER_PATH, path);
        Profiler p = new Profiler(Integer.parseInt(args[0]));
        try {
            p.start(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
