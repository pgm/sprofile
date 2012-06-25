package org.github.sprofile;

import java.lang.Thread.State;
import java.util.Arrays;

import org.github.sprofile.ProfileParser.ObservationListener;

public class ProfilerTest {
	public static final class PrintObservations implements ObservationListener {
		long collectionMills = 0;
		int collectionCount = 0;
		
		public void observe(long timestamp, long threadId,
				String threadName, State threadState,
				StackTraceElement[] trace) {
			System.out.println(""+timestamp+" "+threadId+" "+" "+threadName+" "+threadState+" "+Arrays.toString(trace));
		}

		public void collectionTime(int milliseconds) {
			collectionMills += milliseconds;
			collectionCount ++;
		}
		
		public void printCollectionSummary() {
			double x = ((double)collectionMills)/collectionCount;
			System.out.println("collection "+x+" ms ("+collectionCount+")");
		}
	}

	static public void main(String args[]) throws Exception {
		Profiler p = new Profiler(50);
		p.start("samples.data");

		for (int i = 0; i < 100; i++) {
			Thread.sleep(100);
		}

		p.stop();
		
		PrintObservations listener = new PrintObservations();
		ProfileParser pp = new ProfileParser("samples.data",
				listener);
		pp.read();
		listener.printCollectionSummary();
	}
}
