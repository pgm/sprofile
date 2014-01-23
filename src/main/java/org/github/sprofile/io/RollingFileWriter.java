package org.github.sprofile.io;

import org.github.sprofile.Context;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RollingFileWriter implements Writer {
    final int fileLengthThreshold;
    final int maxFiles;
    final String filenamePrefix;
    final Pattern datePattern = Pattern.compile("\\d\\d\\d\\d\\d\\d\\d\\d-\\d\\d\\d\\d\\d\\d\\.\\d\\d\\d");
    final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
    final String processDescription;
    final File logDir;

    SnapshotStreamWriter writer;
    DataOutputStream out;

    public RollingFileWriter(String processDescription, int fileLengthThreshold, int maxFiles, String filenamePrefix) {
        File filenamePrefixFile = new File(filenamePrefix);
        if(filenamePrefixFile.getName().equals(""))
            throw new RuntimeException("Filename prefix cannot end with a slash");
        logDir = filenamePrefixFile.getParentFile();

        this.processDescription = processDescription;
        this.fileLengthThreshold = fileLengthThreshold;
        this.filenamePrefix = filenamePrefixFile.getName();
        this.maxFiles = maxFiles;
        openNewWriter(new Date());
    }

    public void checkLength() {
        if (out.size() > fileLengthThreshold) {
            writer.close();
        }
        openNewWriter(new Date());
    }

    protected void cleanUpStaleFiles() {
//        System.out.println("maxFiles = "+maxFiles);
        if(maxFiles <=0)
            return;

        java.io.File[] files = logDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                if(name.startsWith(filenamePrefix)) {
//                    System.out.println("hadPrefix: "+file);
                    Matcher m = datePattern.matcher(name.substring(filenamePrefix.length()));
                    if(m.matches()) {
//                        System.out.println("hadSuffix: "+file);
                        return true;
                    }
                }
                return false;
            }
        });

        if(files.length >= maxFiles) {
            Arrays.sort(files);
            // delete until we have at most maxFiles-1
            int nToDelete = files.length - (maxFiles - 1);
//            System.out.println("toDelete "+nToDelete);
            for(int i=0;i<nToDelete;i++) {
//                System.out.println("Deleting stale log file: "+files[i]);
                files[i].delete();
            }
        }
    }

    public void openNewWriter(Date date) {
        cleanUpStaleFiles();

        File file = new File(logDir, filenamePrefix + dateFormatter.format(date));
        try {
            FileOutputStream outStream = new FileOutputStream(file, false);
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
        checkLength();
    }

    @Override
    public void write(long timestamp, Map<Thread, StackTraceElement[]> dump, Map<Thread, Context> contexts) throws IOException {
        writer.write(timestamp, dump, contexts);
    }
}
