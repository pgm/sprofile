package org.github.sprofile;

import org.github.sprofile.ui.profile.StackTreeNode;
import org.github.sprofile.ui.summary.Call;
import org.github.sprofile.ui.summary.Controller;
import org.github.sprofile.ui.summary.SummaryTableRow;
import org.github.sprofile.ui.timeline.Timeline;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/26/12
 * Time: 12:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class ControllerTest {
    @Test
    public void testAggregate() {
        StackTraceElement call1 = new StackTraceElement("class", "method1", "file", 100);
        StackTraceElement call2 = new StackTraceElement("class", "method2", "file", 100);

        StackTraceElement[] s1 = new StackTraceElement[]{call1};
        StackTraceElement[] s2 = new StackTraceElement[]{call2};
        StackTraceElement[] s3 = new StackTraceElement[]{call1, call2};

        Timeline timeline = new Timeline(new long[]{1, 2, 3}, new StackTraceElement[][]{s1, s2, s3}, new Context[]{null, null, null});
        List<SummaryTableRow> rows = Arrays.asList(new SummaryTableRow("proc", 1, "thread", 2, new Date(), new Date(), timeline));
        Call builder = Controller.aggregate(rows);
        StackTreeNode tree = Controller.makeTree(builder, 100);

        Assert.assertNull(tree.getElement());
        Assert.assertEquals(2, tree.getChildren().size());

        StackTreeNode child1 = tree.getChildren().get(0);
        StackTreeNode child2 = tree.getChildren().get(1);

        Assert.assertEquals(call1, child1.getElement());
        Assert.assertEquals(1, child1.getChildren().size());

        Assert.assertEquals(call2, child2.getElement());
    }

}
