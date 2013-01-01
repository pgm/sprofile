package org.github.sprofile;

public class Context {
    final int instance;
    final Context prev;
    final Details details;

    public Context(int instance, Details details, Context prev) {
        this.instance = instance;
        this.prev = prev;
        this.details = details;
    }

    public int getInstance() {
        return instance;
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
