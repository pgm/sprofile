package org.github.sprofile.io;

import org.github.sprofile.Context;
import org.github.sprofile.Details;
import org.github.sprofile.ThreadInfo;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.github.sprofile.io.Constants.*;

public class ProfileParser {
    DataInputStream din;

    ObservationListener listener;

    public ProfileParser(String filename, ObservationListener listener)
            throws IOException {
        FileInputStream in = new FileInputStream(filename);
        BufferedInputStream bin = new BufferedInputStream(in);
        din = new DataInputStream(bin);
        this.listener = listener;
    }

    public void read() throws IOException {
        long timestamp = 0;
        Map<Long, ThreadInfo> threads = new HashMap();
        Map<Integer, StackTraceElement> traceElements = new HashMap();
        Map<Integer, StackTraceElement[]> traces = new HashMap();

        Map<Integer, String> atoms = new HashMap();
        atoms.put(0, null);

        Map<Integer, Context> contexts = new HashMap();
        contexts.put(0, null);

        try {
            while (true) {
                int tk = din.readByte();
                if (tk == TIMESTAMP) {
                    timestamp = din.readLong();
                } else if (tk == NEW_THREAD_INFO) {
                    long threadId = din.readLong();
                    String name = readAtom(din, atoms);
                    byte state = din.readByte();
                    ThreadInfo ti = new ThreadInfo(name, Thread.State.NEW);
                    threads.put(threadId, ti);
                } else if (tk == NEW_TRACE) {
                    int len = din.readShort();
                    StackTraceElement[] elements = new StackTraceElement[len];
                    for (int i = 0; i < len; i++) {
                        elements[i] = traceElements.get(din.readInt());
                    }
                    traces.put(traces.size(), elements);
                } else if (tk == NEW_TRACE_ELEMENT) {
                    String className = readAtom(din, atoms);
                    String fileName = readAtom(din, atoms);
                    String methodName = readAtom(din, atoms);
                    int lineNumber = din.readInt();
                    traceElements.put(traceElements.size(),
                            new StackTraceElement(className, methodName,
                                    fileName, lineNumber));
                } else if (tk == OBSERVED_TRACE) {
                    long threadId = din.readLong();
                    int traceId = din.readInt();
                    int contextId = din.readInt();

                    ThreadInfo ti = threads.get(threadId);
                    StackTraceElement[] trace = traces.get(traceId);
                    Context context = contexts.get(contextId);
                    listener.observe(timestamp, threadId, ti.getName(), ti.getState(),
                            trace, context);
                } else if (tk == NEW_ATOM) {
                    String value = din.readUTF();
                    atoms.put(atoms.size(), value);
                } else if (tk == COLLECTION_TIME) {
                    listener.collectionTime(din.readInt());
                } else if (tk == NEW_PROCESS) {
                    String value = din.readUTF();
                } else if (tk == NEW_CONTEXT) {
                    int prevContextId = din.readInt();
                    int pairCount = din.readInt();
                    String[] keyValues = new String[pairCount * 2];
                    for (int i = 0; i < pairCount * 2; i++) {
                        keyValues[i] = din.readUTF();
                    }
                    int contextId = contexts.size();
                    contexts.put(contextId, new Context(new Details(keyValues), contexts.get(prevContextId)));
                } else {
                    throw new RuntimeException("unknown: " + tk);
                }
            }
        } catch (IOException ex) {
            // end of file reached
        }
    }

    String readAtom(DataInputStream din, Map<Integer, String> atoms)
            throws IOException {
        return atoms.get(din.readInt());
    }
}
