package org.github.sprofile;

public class ThreadInfo {
    final String name;
    final Thread.State state;

    public ThreadInfo(String name, Thread.State state) {
        this.name = name;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public Thread.State getState() {
        return state;
    }
}
