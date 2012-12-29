package org.github.sprofile.ui.summary;

import org.github.sprofile.transform.DropCaller;
import org.github.sprofile.transform.DropElement;
import org.github.sprofile.transform.Transform;
import org.github.sprofile.ui.profile.ProfileView;
import org.github.sprofile.ui.profile.StackTreeNode;
import org.github.sprofile.ui.timeline.Timeline;
import org.github.sprofile.ui.timeline.TimelineBrowser;
import org.github.sprofile.ui.timeline.TimelineBuilder;

import javax.swing.*;
import java.util.*;
import java.util.regex.Pattern;

public class Controller {
    public void showAggregate(Timeline timeline, int startIndex, int stopIndex) {
        Call tree = new Call(null);
        addCallsToRoot(tree, timeline, startIndex, stopIndex);
        populateChildCounts(tree);

        StackTreeNode root = makeTree(tree, tree.childCount + tree.selfCount);

        JFrame frame = ProfileView.createProfileWindow(root);

        frame.setVisible(true);
    }

    protected List<Transform> createTransforms(String elementFilter, String callFilter) {
        List<Transform> transforms = new ArrayList();

        for (String expr : elementFilter.split(" ")) {
            if (expr.length() > 0)
                transforms.add(new DropElement(Pattern.compile(expr)));
        }

        for (String expr : callFilter.split(" ")) {
            if (expr.length() > 0)
                transforms.add(new DropCaller(Pattern.compile(expr)));
        }

        return transforms;
    }

    public void showAggregate(List<SummaryTableRow> rows, String elementFilter, String callFilter) {
        List<Transform> transforms = createTransforms(elementFilter, callFilter);

        rows = transform(rows, transforms);

        Call tree = aggregate(rows);

        StackTreeNode root = makeTree(tree, tree.childCount + tree.selfCount);

        JFrame frame = ProfileView.createProfileWindow(root);

        frame.setVisible(true);
    }

    public void showTimeline(SummaryTableRow row, String elementFilter, String callFilter) {
        List<Transform> transforms = createTransforms(elementFilter, callFilter);

        Timeline timeline = transform(Arrays.asList(row), transforms).get(0).timeline;

        JFrame frame = new JFrame("Timeline Browser");
        TimelineBrowser browser = new TimelineBrowser(this, timeline);
        frame.setContentPane(browser.getRootPanel());
        frame.pack();
        frame.setVisible(true);
    }

    protected static void populateChildCounts(Call builder) {
        int a = 0;
        for (Call child : builder.children.values()) {
            a += child.childCount + child.selfCount;
        }
        builder.childCount = a;
    }

    public static StackTreeNode makeTree(Call builder, int total) {
        StackTreeNode[] children = new StackTreeNode[builder.children.size()];
        List keys = new ArrayList(builder.children.keySet());
        for (int i = 0; i < children.length; i++) {
            children[i] = makeTree(builder.children.get(keys.get(i)), total);
        }
        Arrays.sort(children, new Comparator<StackTreeNode>() {
            @Override
            public int compare(StackTreeNode stackTreeNode, StackTreeNode stackTreeNode2) {
                return (int) Math.signum(stackTreeNode2.getSamplePercentage() - stackTreeNode.getSamplePercentage());
            }
        });

        return new StackTreeNode(builder.getElement(), builder.selfCount, ((float) (builder.selfCount + builder.childCount)) / total, Arrays.asList(children));
    }

    public static void addCallsToRoot(Call root, Timeline timeline, int startIndex, int stopIndex) {
        for (int i = startIndex; i < stopIndex; i++) {
            StackTraceElement[] trace = timeline.getTrace(i);

            Call node = root;
            for (int traceIndex = 0; traceIndex < trace.length; traceIndex++) {
                StackTraceElement element = trace[trace.length - traceIndex - 1];

                CallKey key = new CallKey(element.getClassName(), element.getMethodName());
                Call child = node.children.get(key);
                if (child == null) {
                    child = new Call(element);
                    node.children.put(key, child);
                }
                child.selfCount++;
                node = child;
            }
        }
    }


    public static Call aggregate(List<SummaryTableRow> rows) {
        Call root = new Call(null);
        for (SummaryTableRow row : rows) {
            Timeline timeline = row.timeline;
            addCallsToRoot(root, timeline, 0, timeline.getSampleCount());
        }

        populateChildCounts(root);

        return root;
    }

    protected List<SummaryTableRow> transform(List<SummaryTableRow> rows, List<Transform> transforms) {
        Map<StackTraceElement[], StackTraceElement[]> cached = new IdentityHashMap<StackTraceElement[], StackTraceElement[]>();

        TimelineBuilder builder = new TimelineBuilder();
        for (SummaryTableRow row : rows) {
            long threadId = row.threadId;
            String threadName = row.threadName;
            Timeline timeline = row.timeline;

            builder.threadName(row.threadId, row.threadName);
            for (int i = 0; i < timeline.getSampleCount(); i++) {
                StackTraceElement[] trace = timeline.getTrace(i);
                if (cached.containsKey(trace)) {
                    trace = cached.get(trace);
                } else {
                    for (Transform t : transforms) {
                        trace = t.transform(trace);
                    }
                }

                builder.sample(timeline.getTime(i), threadId, null, trace, timeline.getContext(i));
            }

        }

        return builder.getThreads();
    }
}
