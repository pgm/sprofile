package org.github.sprofile.ui.profile;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/26/12
 * Time: 11:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class StackTreeNode {
    final StackTraceElement element;
    final float samplePercentage;
    final int samples;
    final List<StackTreeNode> children;

    public StackTreeNode(StackTraceElement element, int samples, float samplePercentage, List<StackTreeNode> children) {
        this.element = element;
        this.samplePercentage = samplePercentage;
        this.samples = samples;
        this.children = children;
    }

    public float getSamplePercentage() {
        return samplePercentage;
    }

    public int getSamples() {
        return samples;
    }

    public List<StackTreeNode> getChildren() {
        return children;
    }

    public StackTraceElement getElement() {
        return element;
    }

    public String toString() {
        if (element == null)
            return "null";
        return element.getMethodName();
    }
}
