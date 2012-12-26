package org.github.sprofile;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/23/12
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class AgentMain {
    public static void agentmain(String agentArgs) {
        String args[] = agentArgs.split(",");
        String path = args[1];
//        System.setProperty(WatchAndAttach.SPROFILER_PATH, path);
//        Profiler p = new Profiler(Integer.parseInt(args[0]), path);
//        p.start();
    }
}
