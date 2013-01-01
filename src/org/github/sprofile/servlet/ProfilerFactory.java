package org.github.sprofile.servlet;

import org.github.sprofile.Profiler;
import org.github.sprofile.io.RollingFileWriter;
import org.github.sprofile.io.Writer;

import java.io.IOException;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/29/12
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProfilerFactory {
    public static final String CONFIG_PATH = "sprofile.properties";

    public static final String PROCESS_DESCRIPTION = "processDescription";
    public static final String LOG_FILE_PREFIX = "logPrefix";
    public static final String LOG_LENGTH_THRESHOLD = "logLengthThreshold";
    public static final String SLEEP_TIME = "sleepTime";

    static Profiler profiler;
    static int refCount;

    private static String safeGet(Properties properties, String key) {
        String value = (String) properties.get(key);
        if (value == null) {
            throw new RuntimeException("Could not find property key: " + key);
        }
        return value;
    }

    protected static Profiler createProfiler() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Properties props = new Properties();
        try {
            props.load(classLoader.getResourceAsStream(CONFIG_PATH));
        } catch (IOException ex) {
            throw new RuntimeException("Got exception trying to load " + CONFIG_PATH + " from class loader");
        }

        long sleepTime = Long.parseLong(safeGet(props, SLEEP_TIME));
        String processDescription = safeGet(props, PROCESS_DESCRIPTION);
        int fileLengthThreshold = Integer.parseInt(safeGet(props, LOG_LENGTH_THRESHOLD));
        String filenamePrefix = safeGet(props, LOG_FILE_PREFIX);

        Writer writer = new RollingFileWriter(processDescription, fileLengthThreshold, filenamePrefix);

        return new Profiler(sleepTime, writer);
    }

    public synchronized static Profiler getInstance() {
        if (profiler == null) {
            refCount = 0;
            profiler = createProfiler();
            profiler.start();
        }

        refCount++;

        return profiler;
    }

    public synchronized static void release(Profiler profiler) {
        if (profiler != ProfilerFactory.profiler) {
            throw new IllegalArgumentException("Tried to release an invalid instance of the profiler");
        }

        refCount--;

        if (refCount < 0) {
            throw new IllegalArgumentException("Profiler was released more times then it was obtained");
        } else if (refCount == 0) {
            profiler.stop();
            ProfilerFactory.profiler = null;
        }
    }
}
