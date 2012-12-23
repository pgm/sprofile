package org.github.sprofile.io;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/22/12
 * Time: 6:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class RollingFileWriter {
    final int fileLengthThreshold;
    final String filenamePrefix;
    final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");

    Writer writer;
    DataOutputStream out;

    public RollingFileWriter(int fileLengthThreshold, String filenamePrefix) {
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
            writer = new Writer(out);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
