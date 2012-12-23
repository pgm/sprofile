package org.github.sprofile;

public class Context {
    final Context prev;
    final Details details;

    public Context(Details details, Context prev) {
        this.prev = prev;
        this.details = details;
    }

    public Context getPrevious() {
        return prev;
    }

    public String[] getKeyValues() {
        throw new RuntimeException("unimplemented");
    }

    public int getDepth() {
        int depth = 0;
        Context c = this;
        while (c != null) {
            depth++;
            c = c.prev;
        }
        return depth;
    }
}
