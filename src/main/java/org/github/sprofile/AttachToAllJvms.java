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

public class AttachToAllJvms {
    public static List<VirtualMachineDescriptor> pollVms(Set<String> watchedIds) {
        // get the list of virtual machines accessible
        List<VirtualMachineDescriptor> newVms = new ArrayList<VirtualMachineDescriptor>();
        Set<String> latestIds = new HashSet<String>();

        List<VirtualMachineDescriptor> descs = VirtualMachine.list();
        for (VirtualMachineDescriptor desc : descs) {
            latestIds.add(desc.id());

            if (!watchedIds.contains(desc.id())) {
                newVms.add(desc);
            }
        }

        // only remember Ids which we've seen recently
        watchedIds.clear();
        watchedIds.addAll(latestIds);

        return newVms;
    }

    public static void startVmPollThread(final String jarPath, final int samplePeriod, final int listeningPort,
                                         final String logFilePath, final int maxLen, final int maxFiles) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Set<String> watchedIds = new HashSet<String>();

                while(true) {
                    System.out.println("Searching for VMs");
                    List<VirtualMachineDescriptor> newVMs = pollVms(watchedIds);

                    for (VirtualMachineDescriptor vmDesc : newVMs) {
                        System.out.println("Attaching to vm " + vmDesc.displayName() + " (" + vmDesc.id() + ")");
                        watchedIds.add(vmDesc.id());

                        try {
                            VirtualMachine vm = VirtualMachine.attach(vmDesc);
                            Properties properties = vm.getSystemProperties();
                            boolean loadAgent = true;
                            if(properties.getProperty(AttachToJvm.SPROFILER_PATH) != null) {
                                System.out.println("VM is already being profiled");
                                loadAgent = false;
                            }

                            if(properties.getProperty(AttachToJvm.SPROFILE_IGNORE) != null) {
                                System.out.println("VM is monitor, so skipping");
                                loadAgent = false;
                            }

                            if(loadAgent)
                                vm.loadAgent(jarPath, samplePeriod + "," + listeningPort + "," + logFilePath+"-"+vm.id()+"-,"+maxLen+","+maxFiles);
                            vm.detach();
                        } catch (Exception ex) {
                            System.err.println("Could not monitor "+vmDesc.id());
                            ex.printStackTrace();
                        }
                    }

                    try {
                    Thread.sleep(60000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        t.setDaemon(true);
        t.setName("VM poll");
        t.start();
    }

    // args are: pid path-to-jar samplePeriod logfile
    public static void main(String[] args) throws Exception {
        System.setProperty(AttachToJvm.SPROFILE_IGNORE, "true");

        if (args.length < 4) {
            System.out.println("Invalid number of arguments.  Expected path-to-jar, samplePeriodInMs, logFilePath, maxLen, maxFiles, jvmId1, jvmId2, etc.");

            List<VirtualMachineDescriptor> descs = VirtualMachine.list();
            System.out.println("Accessible VMs:");
            for(VirtualMachineDescriptor desc : descs) {
                System.out.println("\t"+desc.id()+": "+desc.displayName());
            }

            System.exit(-1);
        }

        String jarPath = new File(args[0]).getAbsolutePath();
        int samplePeriod = Integer.parseInt(args[1]);
        String logFilePath = new File(args[2]).getAbsolutePath();
        int maxLen = Integer.parseInt(args[3]);
        int maxFiles = Integer.parseInt(args[4]);


        // start a socket which is used to signal that we're still listening
        ServerSocket listeningPort = new ServerSocket(0);

        startVmPollThread(jarPath, samplePeriod, listeningPort.getLocalPort(),
                logFilePath, maxLen, maxFiles);

        System.out.println("Waiting...  (Kill this process to stop sampling)");
        while (true) {
            final Socket socket = listeningPort.accept();
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
}
