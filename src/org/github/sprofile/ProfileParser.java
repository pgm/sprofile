package org.github.sprofile;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.HashMap;
import java.util.Map;

import org.github.sprofile.Profiler.ThreadInfo;

public class ProfileParser {
	DataInputStream din;

	static interface ObservationListener {
		public void observe(long timestamp, long threadId, String threadName,
				State threadState, StackTraceElement[] trace);
		
		public void collectionTime(int milliseconds);
	}

	ObservationListener listener;

	public ProfileParser(String filename, ObservationListener listener)
			throws IOException {
		FileInputStream in = new FileInputStream(filename);
		BufferedInputStream bin = new BufferedInputStream(in);
		din = new DataInputStream(bin);
		this.listener = listener;
	}

	public void read() throws IOException {
		long timestamp = 0;
		Map<Long, ThreadInfo> threads = new HashMap();
		Map<Integer, StackTraceElement> traceElements = new HashMap();
		Map<Integer, StackTraceElement[]> traces = new HashMap();
		Map<Integer, String> atoms = new HashMap();
		atoms.put(0, null);
		try {
			while (true) {
				int tk = din.readByte();
				if (tk == Profiler.TIMESTAMP) {
					timestamp = din.readLong();
				} else if (tk == Profiler.NEW_THREAD_INFO) {
					long threadId = din.readLong();
					String name = readAtom(din, atoms);
					byte state = din.readByte();
					ThreadInfo ti = new ThreadInfo();
					ti.name = name;
					threads.put(threadId, ti);
				} else if (tk == Profiler.NEW_TRACE) {
					int len = din.readShort();
					StackTraceElement[] elements = new StackTraceElement[len];
					for (int i = 0; i < len; i++) {
						elements[i] = traceElements.get(din.readInt());
					}
					traces.put(traces.size(), elements);
				} else if (tk == Profiler.NEW_TRACE_ELEMENT) {
					String className = readAtom(din, atoms);
					String fileName = readAtom(din, atoms);
					String methodName = readAtom(din, atoms);
					int lineNumber = din.readInt();
					traceElements.put(traceElements.size(),
							new StackTraceElement(className, methodName,
									fileName, lineNumber));
				} else if (tk == Profiler.OBSERVED_TRACE) {
					long threadId = din.readLong();
					int traceId = din.readInt();

					ThreadInfo ti = threads.get(threadId);
					StackTraceElement[] trace = traces.get(traceId);
					listener.observe(timestamp, threadId, ti.name, ti.state,
							trace);
				} else if (tk == Profiler.NEW_ATOM) {
					String value = din.readUTF();
					atoms.put(atoms.size(), value);
				} else if (tk == Profiler.COLLECTION_TIME) {
					listener.collectionTime(din.readInt());
				} else if (tk == Profiler.NEW_PROCESS) {
					String value = din.readUTF();
				} else {
					throw new RuntimeException("unknown: " + tk);
				}
			}
		} catch (IOException ex) {
			// end of file reached
		}
	}

	String readAtom(DataInputStream din, Map<Integer, String> atoms)
			throws IOException {
		return atoms.get(din.readInt());
	}
}
