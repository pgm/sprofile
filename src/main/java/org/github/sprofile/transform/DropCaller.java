package org.github.sprofile.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/26/12
 * Time: 8:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class DropCaller implements Transform {
    final Pattern callName;

    public DropCaller(Pattern callName) {
        this.callName = callName;
    }

    @Override
    public StackTraceElement[] transform(StackTraceElement[] trace) {
        List<StackTraceElement> result = new ArrayList();

        boolean foundCall = false;
        for (int i = 0; i < trace.length; i++) {
            StackTraceElement element = trace[trace.length - i - 1];
            if (!foundCall) {
                String fullName = element.getClassName() + "." + element.getMethodName();
                if (callName.matcher(fullName).matches()) {
                    foundCall = true;
                }
            }
            if (foundCall) {
                result.add(element);
            }
        }

        Collections.reverse(result);
        return result.toArray(new StackTraceElement[result.size()]);
    }
}
