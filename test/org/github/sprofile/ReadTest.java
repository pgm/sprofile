package org.github.sprofile;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.github.sprofile.ProfileParser.ObservationListener;
import org.github.sprofile.ProfilerTest.PrintObservations;

public class ReadTest {
	static class Counts {
		String name;
		int self;
		int children;
	}

	public static final class SummarizeObservations implements
			ObservationListener {
		Map<String, Counts> counts = new HashMap();

		public void observe(long timestamp, long threadId, String threadName,
				State threadState, StackTraceElement[] trace) {

			for (StackTraceElement element : trace) {
				get(element).children++;
			}

			if (trace.length > 0) {
				StackTraceElement element = trace[trace.length - 1];
				get(element).self++;
			}
		}

		private Counts get(StackTraceElement element) {
			String methodName = element.getClassName() + "."
					+ element.getMethodName();
			Counts c = counts.get(methodName);
			if (c == null) {
				c = new Counts();
				c.name = methodName;
				counts.put(methodName, c);
			}
			return c;
		}

		public void collectionTime(int milliseconds) {
		}

		public void printCollectionSummary() {
			List<Counts> sorted = new ArrayList(counts.values());
			Collections.sort(sorted, new Comparator<Counts>() {
				public int compare(Counts o1, Counts o2) {
					return o1.children - o2.children;
				}
			});

			for (Counts c : sorted) {
				System.out.println("" + c.children + "\t" + c.self + "\t"
						+ c.name);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		// 
		SummarizeObservations listener = new SummarizeObservations();
		ProfileParser pp = new ProfileParser(
				"/Users/pgm/work/workspace/profiler/./samples1214039878200522974.dat",
				listener);
		pp.read();
		listener.printCollectionSummary();

	}

}
