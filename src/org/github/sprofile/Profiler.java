package org.github.sprofile;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Profiler implements Runnable {
	long sleepTime;

	public Profiler(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	static class IdList {
		final int ids[];

		public IdList(int ids[]) {
			this.ids = ids;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(ids);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IdList other = (IdList) obj;
			if (!Arrays.equals(ids, other.ids))
				return false;
			return true;
		}
	}

	static class ThreadInfo {
		String name;
		State state;
	}

	public static final int NEW_TRACE_ELEMENT = 1;
	public static final int NEW_TRACE = 2;
	public static final int OBSERVED_TRACE = 3;
	public static final int TIMESTAMP = 4;
	public static final int NEW_THREAD_INFO = 5;
	public static final int NEW_ATOM = 6;
	public static final int COLLECTION_TIME = 7;
	public static final int NEW_PROCESS = 8;

	static final int MAX_TIME_BETWEEN_FLUSHES = 60 * 1000;

	private Map<StackTraceElement, Integer> stackTraceElements = new HashMap();
	private Map<IdList, Integer> traces = new HashMap();
	private Map<Long, ThreadInfo> threadInfo = new HashMap();
	private Map<String, Integer> atoms = new HashMap();
	{
		atoms.put(null, 0);
	}

	private List<String> unwrittenAtoms = new ArrayList();
	private List<StackTraceElement> unwrittenStackTraceElements = new ArrayList();
	private List<IdList> unwrittenTraces = new ArrayList();

	boolean running = false;
	Thread samplerThread;

	DataOutputStream out;

	long lastFlush = System.currentTimeMillis();

	public void sample() throws IOException {
		long timestamp = System.currentTimeMillis();
		out.writeByte(TIMESTAMP);
		out.writeLong(timestamp);

		Map<Thread, StackTraceElement[]> dump = Thread.getAllStackTraces();
		for (Entry<Thread, StackTraceElement[]> entry : dump.entrySet()) {
			Thread thread = entry.getKey();
			StackTraceElement trace[] = entry.getValue();

			// write everything to a temporary buffer so we can collect all the
			// unwritten atoms first
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			writeThreadState(new DataOutputStream(buffer), thread, trace);

			for (String atom : unwrittenAtoms) {
				out.writeByte(NEW_ATOM);
				out.writeUTF(atom);
			}
			unwrittenAtoms.clear();
			out.write(buffer.toByteArray());
		}

		if (System.currentTimeMillis() - lastFlush > MAX_TIME_BETWEEN_FLUSHES) {
			out.flush();
		}

		long finalTimestamp = System.currentTimeMillis();
		out.writeByte(COLLECTION_TIME);
		out.writeInt((int) (finalTimestamp - timestamp));
	}

	private void writeThreadState(DataOutputStream out, Thread thread2,
			StackTraceElement[] trace) throws IOException {
		ThreadInfo writtenThreadInfo = threadInfo.get(thread2.getId());
		if (writtenThreadInfo == null
				|| !writtenThreadInfo.name.equals(thread2.getName())
				|| writtenThreadInfo.state != thread2.getState()) {
			if (writtenThreadInfo == null) {
				writtenThreadInfo = new ThreadInfo();
			}
			writtenThreadInfo.name = thread2.getName();
			writtenThreadInfo.state = thread2.getState();

			out.writeByte(NEW_THREAD_INFO);
			out.writeLong(thread2.getId());
			out.writeInt(getAtomId(writtenThreadInfo.name));
			out.writeByte(writtenThreadInfo.state.ordinal());
		}

		int traceId = getTraceId(trace);

		write(out, thread2.getId(), traceId);
	}

	private int getAtomId(String str) {
		Integer id = atoms.get(str);
		if (id == null) {
			id = atoms.size();
			atoms.put(str, id);
			unwrittenAtoms.add(str);
		}
		return id;
	}

	private void write(DataOutputStream out, long threadId, int traceId)
			throws IOException {
		for (StackTraceElement e : unwrittenStackTraceElements) {
			out.writeByte(NEW_TRACE_ELEMENT);
			out.writeInt(getAtomId(e.getClassName()));
			out.writeInt(getAtomId(e.getFileName()));
			out.writeInt(getAtomId(e.getMethodName()));
			out.writeInt(e.getLineNumber());
		}
		unwrittenStackTraceElements.clear();

		for (IdList ids : unwrittenTraces) {
			out.writeByte(NEW_TRACE);
			out.writeShort(ids.ids.length);
			for (int id : ids.ids) {
				out.writeInt(id);
			}
		}
		unwrittenTraces.clear();

		out.writeByte(OBSERVED_TRACE);
		out.writeLong(threadId);
		out.writeInt(traceId);
	}

	private int getTraceId(StackTraceElement[] trace) {
		int[] ids = new int[trace.length];
		for (int i = 0; i < trace.length; i++) {
			ids[i] = getTraceElementId(trace[i]);
			unwrittenStackTraceElements.add(trace[i]);
		}
		IdList idList = new IdList(ids);

		Integer traceId = traces.get(idList);
		if (traceId == null) {
			traceId = traces.size();
			traces.put(idList, traceId);
			unwrittenTraces.add(idList);
		}

		return traceId;
	}

	private int getTraceElementId(StackTraceElement stackTraceElement) {
		Integer id = stackTraceElements.get(stackTraceElement);
		if (id == null) {
			id = stackTraceElements.size();
			stackTraceElements.put(stackTraceElement, id);
		}
		return id;
	}

	public void stop() {
		synchronized (this) {
			if (running) {
				running = false;
				this.notifyAll();
			}
		}

		// wait for thread to exit before resuming
		try {
			if (samplerThread != null)
				samplerThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		synchronized (this) {
			try {
				out.writeByte(NEW_PROCESS);
				out.writeUTF(ManagementFactory.getRuntimeMXBean().getName());
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}

			running = true;
			samplerThread = Thread.currentThread();
			while (running) {
				try {
					sample();
				} catch (IOException ex) {
					ex.printStackTrace();
					break;
				}
				try {
					this.wait(sleepTime);
				} catch (InterruptedException ex) {
					// continue. Wake up may have occurred because process was
					// shutting down
				}
			}
		}

		try {
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void start(String filename) throws IOException {
		FileOutputStream outStream = new FileOutputStream(filename, false);
		BufferedOutputStream buffered = new BufferedOutputStream(outStream);
		out = new DataOutputStream(buffered);

		Thread shutdownThread = new Thread(new Runnable() {
			public void run() {
				stop();
			}
		});
		shutdownThread.setName("sprofiler shutdown hook");
		Runtime.getRuntime().addShutdownHook(shutdownThread);

		Thread mainThread = new Thread(this);
		mainThread.setName("sprofiler");
		mainThread.setDaemon(true);
		mainThread.start();
	}

	public static void agentmain(String agentArgs) {
		String args[] = agentArgs.split(",");
		String path = args[1];
		System.setProperty(WatchAndAttach.SPROFILER_PATH, path);
		Profiler p = new Profiler(Integer.parseInt(args[0]));
		try {
			p.start(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
