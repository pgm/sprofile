package org.github.sprofile.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/26/12
 * Time: 8:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class DropElement implements Transform {
    final Pattern filter;

    public DropElement(Pattern filter) {
        this.filter = filter;
    }

    @Override
    public StackTraceElement[] transform(StackTraceElement[] trace) {
        List<StackTraceElement> result = new ArrayList();

        boolean foundCall = false;
        for (StackTraceElement element : trace) {
            String fullName = element.getClassName() + "." + element.getMethodName();
            if (!filter.matcher(fullName).matches()) {
                result.add(element);
            }
        }

        return result.toArray(new StackTraceElement[result.size()]);
    }
}
