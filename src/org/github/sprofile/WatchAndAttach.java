package org.github.sprofile;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class WatchAndAttach {
	private static final String SPROFILE_IGNORE = "sprofile.ignore";

	static final String SPROFILER_PATH = "sprofiler.path";

	static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");

	public static ServerSocket listeningPort;

	public static void main(String[] args) throws Exception {
		Set<VirtualMachineDescriptor> prevDescriptors = new HashSet();

		System.setProperty(SPROFILE_IGNORE, "true");
		List<VirtualMachineDescriptor> descs = VirtualMachine.list();

		int pollInterval = Integer.parseInt(args[0]);
		String logPath = args[1];

		// this is a bit of a hack, but I wanted a way to prevent multiple
		// instances running. A port is
		// listened to, and if it cannot get the port, an exception will be
		// thrown. The listener is never
		// used, it's just a mechanism to prevent multiple instances from
		// running concurrently.
		if (args.length > 2) {
			int port = Integer.parseInt(args[2]);
			listeningPort = new ServerSocket(port);
		}

		String jarPath = findJar();
		String hostName = InetAddress.getLocalHost().getHostName();

		while (true) {
			for (VirtualMachineDescriptor desc : descs) {
				// don't bother checking a VM that we saw on the previous pass
				if (prevDescriptors.contains(desc)) {
					continue;
				}

				System.out.println("New vm found " + desc);
				VirtualMachine vm = VirtualMachine.attach(desc);

				Properties props = vm.getSystemProperties();
				if (props.containsKey(SPROFILE_IGNORE)) {
					System.out
							.println("skipping process because flagged as one to ignore");
					continue;
				}

				if (props.containsKey(SPROFILER_PATH)) {
					System.out
							.println("skipping process because already instrumented and log written to "
									+ props.get(SPROFILER_PATH));
					continue;
				}

				System.out.println("New vm found " + desc);
				String path = File.createTempFile("samples", ".dat",
						getTodaysLogPath(hostName, logPath)).getAbsolutePath();

				System.out.println("Attaching to " + desc
						+ " and writing output to " + path);
				vm.loadAgent(jarPath, pollInterval + "," + path);
				vm.detach();
			}

			prevDescriptors = new HashSet(descs);
			Thread.sleep(1000);
		}
	}

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

	private static File getTodaysLogPath(String hostName, String logPath) {
		Date now = new Date();
		String dateStr = dateFormatter.format(now);
		File dir = new File(logPath + "/" + dateStr + "/" + hostName);
		dir.mkdirs();
		return dir;
	}
}
