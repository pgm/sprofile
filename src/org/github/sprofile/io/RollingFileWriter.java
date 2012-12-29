package org.github.sprofile.io;

import org.github.sprofile.Context;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class RollingFileWriter implements Writer {
    final int fileLengthThreshold;
    final String filenamePrefix;
    final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
    final String processDescription;

    SnapshotStreamWriter writer;
    DataOutputStream out;

    public RollingFileWriter(String processDescription, int fileLengthThreshold, String filenamePrefix) {
        this.processDescription = processDescription;
        this.fileLengthThreshold = fileLengthThreshold;
        this.filenamePrefix = filenamePrefix;
        openNewWriter(new Date());
    }

    public void checkLength() {
        if (out.size() > fileLengthThreshold) {
            writer.close();
        }
        openNewWriter(new Date());
    }

    public void openNewWriter(Date date) {
        String filename = filenamePrefix + dateFormatter.format(date);
        try {
            FileOutputStream outStream = new FileOutputStream(filename, false);
            out = new DataOutputStream(new BufferedOutputStream(outStream));
            writer = new SnapshotStreamWriter(processDescription, out);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void flush() {
        writer.flush();
    }

    @Override
    public void close() {
        writer.close();
    }

    @Override
    public void writeCollectionFinished(int time) {
        writer.writeCollectionFinished(time);
    }

    @Override
    public void write(long timestamp, Map<Thread, StackTraceElement[]> dump, Map<Thread, Context> contexts) throws IOException {
        writer.write(timestamp, dump, contexts);
    }
}
