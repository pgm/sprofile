package org.github.sprofile;

import org.github.sprofile.io.ObservationListener;
import org.github.sprofile.io.ProfileParser;
import org.github.sprofile.io.RollingFileWriter;

import java.io.File;
import java.io.FileFilter;
import java.lang.Thread.State;
import java.util.Arrays;

public class ProfilerTest {
    public static final class PrintObservations implements ObservationListener {
        long collectionMills = 0;
        int collectionCount = 0;

        public void observe(long timestamp, long threadId,
                            String threadName, State threadState,
                            StackTraceElement[] trace, Context context) {
            System.out.println("" + timestamp + " " + threadId + " " + " " + threadName + " " + threadState + " " + Arrays.toString(trace));
        }

        public void collectionTime(int milliseconds) {
            collectionMills += milliseconds;
            collectionCount++;
        }

        public void printCollectionSummary() {
            double x = ((double) collectionMills) / collectionCount;
            System.out.println("collection " + x + " ms (" + collectionCount + ")");
        }
    }

    static void call1(int count) throws Exception {
        Thread.sleep(100);
        call2(count);
        Thread.sleep(50);
        call3();
        Thread.sleep(100);
    }

    static void call2(int count) throws Exception {
        Thread.sleep(100);
        for (int i = 0; i < count; i++) {
            call4();
        }
        Thread.sleep(100);

    }

    static void call3() throws Exception {
        Thread.sleep(100);
    }

    static void call4() throws Exception {
        Thread.sleep(30);
    }

    static public void main(String args[]) throws Exception {
        System.out.println(new File(".").getAbsolutePath());
        RollingFileWriter writer = new RollingFileWriter(5000000, "samples-");
        Profiler p = new Profiler(50, writer);
        p.start();

        for (int i = 0; i < 100; i++) {
            System.out.println("" + i);
            call1(3);
        }

        p.stop();

        PrintObservations listener = new PrintObservations();
        File[] filenames = new File(".").listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.getName().startsWith("samples-"));
            }
        });

        for (File f : filenames) {
            ProfileParser pp = new ProfileParser(f.getAbsolutePath(),
                    listener);
            pp.read();
        }
        listener.printCollectionSummary();
    }
}
