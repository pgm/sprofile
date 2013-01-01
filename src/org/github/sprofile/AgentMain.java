package org.github.sprofile;

import org.github.sprofile.io.SnapshotStreamWriter;

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
        String path = args[2];
        String processDesc = System.getProperty("sun.java.command");

        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), port);
            OutputStreamWriter socketWriter = new OutputStreamWriter(socket.getOutputStream());
            socketWriter.write("Starting sampling writing to " + path);
            socketWriter.flush();

            SnapshotStreamWriter writer = new SnapshotStreamWriter(processDesc, new FileOutputStream(path));
            Profiler profiler = new Profiler(samplingPeriod, writer);
            profiler.start();

            // block until we get notification that this stream closed
            int eof = socket.getInputStream().read();

            // The attach process has stopped.  Stop profiling
            profiler.stop();
            writer.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }


        System.setProperty(AttachToJvm.SPROFILER_PATH, path);
//        Profiler p = new Profiler(Integer.parseInt(args[0]), path);
//        p.start();
    }
}
