package org.github.sprofile;

import org.github.sprofile.io.ObservationListener;
import org.github.sprofile.io.ProfileParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.State;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SummarizeProfiles {
    static class Counts {
        String name;
        int self;
        int children;
        Map<String, Counts> childCounts;
    }

    public static final class SummarizeObservations implements
            ObservationListener {

        final static Comparator<Counts> COMPARE_BY_CHILD_COUNT = new Comparator<Counts>() {
            public int compare(Counts o1, Counts o2) {
                return -(o1.children - o2.children);
            }
        };

        Pattern filter;

        public SummarizeObservations(Pattern filter) {
            this.filter = filter;
            this.callTree.childCounts = new HashMap();
            this.callTree.name = "[root]";
        }

        Map<String, Counts> counts = new HashMap();
        Counts callTree = new Counts();

        public void observe(long timestamp, long threadId, String threadName,
                            State threadState, StackTraceElement[] trace, Context context) {

            if (filter != null) {
                boolean foundMatch = false;
                for (StackTraceElement element : trace) {
                    Matcher m = filter.matcher(element.getClassName());
                    if (m.matches()) {
                        foundMatch = true;
                        break;
                    }
                }

                if (!foundMatch)
                    return;
            }

            Counts tree = callTree;
            for (StackTraceElement element : trace) {
                get(element).children++;

                String methodName = element.getClassName() + "."
                        + element.getMethodName();
                Counts node = tree.childCounts.get(methodName);
                if (node == null) {
                    node = new Counts();
                    node.name = methodName;
                    node.childCounts = new HashMap();
                    tree.childCounts.put(methodName, node);
                }
                node.children++;
                tree = node;
            }
            tree.self++;

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
            Collections.sort(sorted, COMPARE_BY_CHILD_COUNT);

            for (Counts c : sorted) {
                System.out.println("" + c.children + "\t" + c.self + "\t"
                        + c.name);
            }

            System.out.println("\nCall tree:");
            printTree(System.out, 0, callTree);
        }

        public void printTree(PrintStream out, int indentLevel, Counts tree) {
            out.print("" + tree.children + "\t" + tree.self + "\t");
            for (int i = 0; i < indentLevel; i++) {
                out.print('\t');
            }
            out.println(tree.name);

            List<Counts> children = new ArrayList(tree.childCounts.values());
            Collections.sort(children, COMPARE_BY_CHILD_COUNT);
            for (Counts child : children) {
                // prune any nodes that only have a single sample
                if (child.children > 1)
                    printTree(out, indentLevel + 1, child);
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        Pattern filter = null;
        if (args.length > 1) {
            filter = Pattern.compile(args[1]);
        }
        SummarizeObservations listener = new SummarizeObservations(filter);
        recursivelyParse(new File(args[0]), listener);
        listener.printCollectionSummary();
    }

    private static void recursivelyParse(File path,
                                         SummarizeObservations listener) throws IOException {

        for (File child : path.listFiles()) {
            if (child.isDirectory()) {
                recursivelyParse(child, listener);

            } else if (child.getName().startsWith("samples")
                    && child.getName().endsWith(".dat")) {
                System.out.println("reading " + child.getPath());

                ProfileParser pp = new ProfileParser(child.getAbsolutePath(),
                        listener);
                pp.read();
            }
        }

    }
}
