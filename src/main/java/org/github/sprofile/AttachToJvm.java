package org.github.sprofile;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class AttachToJvm {
    private static final String SPROFILE_IGNORE = "sprofile.ignore";

    static final String SPROFILER_PATH = "sprofiler.path";

    static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");

    // args are: pid path-to-jar samplePeriod logfile
    public static void main(String[] args) throws Exception {
        System.setProperty(SPROFILE_IGNORE, "true");

        if (args.length < 4) {
            System.out.println("Invalid number of arguments.  Expected path-to-jar, samplePeriodInMs, logFilePath, maxLen, maxFiles, jvmId1, jvmId2, etc.");
            System.exit(-1);
        }

        String jarPath = new File(args[0]).getAbsolutePath();
        int samplePeriod = Integer.parseInt(args[1]);
        String logFilePath = new File(args[2]).getAbsolutePath();
        int maxLen = Integer.parseInt(args[3]);
        int maxFiles = Integer.parseInt(args[4]);
        Collection<String> jvmIds = new HashSet(Arrays.asList(args).subList(5, args.length));

        // get the list of virtual machines accessible
        List<VirtualMachineDescriptor> descs = VirtualMachine.list();
        Collection<VirtualMachineDescriptor> foundDescs = new ArrayList();
        for (VirtualMachineDescriptor desc : descs) {
            if (jvmIds.contains(desc.id())) {
                foundDescs.add(desc);
                jvmIds.remove(desc.id());
            }
        }


        if (jvmIds.size() > 0) {
            System.out.println("Could not find jvms: " + jvmIds + ".  Use Jps to list java vms and their Ids");
            System.exit(-1);
        }

        // start a socket which is used to signal that we're still listening
        ServerSocket listeningPort = new ServerSocket(0);

        String hostName = InetAddress.getLocalHost().getHostName();

        for (VirtualMachineDescriptor foundDesc : foundDescs) {
            System.out.println("Attaching to vm " + foundDesc.displayName() + " (" + foundDesc.id() + ")");
            VirtualMachine vm = VirtualMachine.attach(foundDesc);
            vm.loadAgent(jarPath, samplePeriod + "," + listeningPort.getLocalPort() + "," + logFilePath+","+maxLen+","+maxFiles);
            vm.detach();
        }

        System.out.println("Sampling...  (Kill this process to stop sampling)");
        while (true) {
            Socket socket = listeningPort.accept();
            printTextFrom(socket);
        }
    }

    protected static void printTextFrom(final Socket socket) {
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            String line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            System.out.println(line);
                        } catch (IOException ex) {
                            System.out.println("Exception reading -- closing socket");
                            break;
                        }
                    }
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        // don't worry about it
                    }
                }
            });
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    /*
    private static String findJar() {
		String classpath[] = System.getProperty("java.class.path").split(
				File.pathSeparator);
		for (String cp : classpath) {
			if (cp.endsWith("sprofile.jar")) {
				return cp;
			}
		}
		throw new RuntimeException("could not find sprofile.jar in classpath");
	}
	*/
}
