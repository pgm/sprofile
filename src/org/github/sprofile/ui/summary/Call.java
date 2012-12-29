package org.github.sprofile.ui.summary;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/26/12
 * Time: 9:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class Call {
    final StackTraceElement element;
    public final Map<CallKey, Call> children = new HashMap();
    public int selfCount;
    public int childCount;

    public Call(StackTraceElement element) {
        this.element = element;
    }

    public StackTraceElement getElement() {
        return element;
    }

}
