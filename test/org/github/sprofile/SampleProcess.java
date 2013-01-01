package org.github.sprofile;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/31/12
 * Time: 10:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class SampleProcess {
    public static void main(String args[]) throws Exception {
        while (true) {
            Profiler2Test.runCpuIntensiveTask();
        }
    }
}
