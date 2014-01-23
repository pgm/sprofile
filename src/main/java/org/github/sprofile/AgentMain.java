package org.github.sprofile;

import org.github.sprofile.io.RollingFileWriter;
import org.github.sprofile.io.SnapshotStreamWriter;
import org.github.sprofile.io.Writer;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/23/12
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class AgentMain {
    public static void agentmain(String agentArgs) {
        String args[] = agentArgs.split(",");

        int samplingPeriod = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);
        final String path = args[2];
        int maxLen = Integer.parseInt(args[3]);
        int maxFiles = Integer.parseInt(args[4]);
        String processDesc = System.getProperty("sun.java.command");
        System.out.println("Starting sampling writing to "+path);

        try {
            final Socket socket = new Socket(InetAddress.getLocalHost(), port);
            final OutputStreamWriter socketWriter = new OutputStreamWriter(socket.getOutputStream());
            socketWriter.write("Starting sampling writing to " + path);
            socketWriter.flush();

            final Writer writer = new RollingFileWriter(processDesc, maxLen, maxFiles, path);
            final Profiler profiler = new Profiler(samplingPeriod, writer);
            profiler.start();

            // kick off another thread to block on the stream, because the jvm still thinks the agent hasn't initialized until
            // this method returns.

            Thread thread = new Thread(new Runnable() {
                public void run() {
                    System.setProperty(AttachToJvm.SPROFILER_PATH, path);

                    // block until we get notification that this stream closed
                    try {
                        int eof = socket.getInputStream().read();

                        // The attach process has stopped.  Stop profiling
                        profiler.stop();
                        writer.close();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }

                    System.getProperties().remove(AttachToJvm.SPROFILER_PATH);

                }
            });

            thread.setDaemon(true);
            thread.setName("Sprofile logging");
            thread.start();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }


    }
}
