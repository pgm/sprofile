package org.github.sprofile.io;

import org.github.sprofile.Context;
import org.github.sprofile.Details;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.github.sprofile.io.Constants.*;

public class ProfileParser {
    DataInputStream din;

    ProfileVisitor visitor;

    public ProfileParser(String filename, ProfileVisitor visitor)
            throws IOException {
        FileInputStream in = new FileInputStream(filename);
        BufferedInputStream bin = new BufferedInputStream(in);
        din = new DataInputStream(bin);
        this.visitor = visitor;
    }

    public void read() throws IOException {
        Map<Integer, StackTraceElement> traceElements = new HashMap();
        Map<Integer, StackTraceElement[]> traces = new HashMap();

        Map<Integer, String> atoms = new HashMap();
        Map<Integer, Context> contexts = new HashMap();

        atoms.put(0, null);
        contexts.put(0, null);

        try {
            while (true) {
                int tk = din.readByte();
                if (tk == TIMESTAMP) {
                    long timestamp = din.readLong();
                    visitor.handleTimestamp(timestamp);
                } else if (tk == THREAD_NAME) {
                    long threadId = din.readLong();
                    String name = readAtom(din, atoms);
                    visitor.handleThreadName(threadId, name);
                } else if (tk == NEW_TRACE) {
                    int len = din.readShort();
                    StackTraceElement[] elements = new StackTraceElement[len];
                    for (int i = 0; i < len; i++) {
                        elements[i] = traceElements.get(din.readInt());
                    }
                    int index = traces.size();
                    traces.put(index, elements);
                    visitor.handleTrace(index, elements);
                } else if (tk == NEW_TRACE_ELEMENT) {
                    String className = readAtom(din, atoms);
                    String filename
                            = readAtom(din, atoms);
                    String methodName = readAtom(din, atoms);
                    int lineNumber = din.readInt();
                    int index = traceElements.size();
                    traceElements.put(index,
                            new StackTraceElement(className, methodName,
                                    filename, lineNumber));
                    visitor.handleTraceElement(index, className, filename, methodName, lineNumber);
                } else if (tk == OBSERVED_TRACE) {
                    long threadId = din.readLong();
                    int state = din.readByte();
                    int traceId = din.readInt();
                    int contextId = din.readInt();
                    visitor.handleSampledTrace(threadId, Thread.State.values()[state], traces.get(traceId), contexts.get(contextId));
                } else if (tk == NEW_ATOM) {
                    String value = din.readUTF();
                    int index = atoms.size();
                    atoms.put(index, value);
                    visitor.handleAtom(index, value);
                } else if (tk == COLLECTION_TIME) {
                    visitor.handleCollectionTime(din.readInt());
                } else if (tk == NEW_PROCESS) {
                    String value = din.readUTF();
                    visitor.handleProcessInfo(value);
                } else if (tk == NEW_CONTEXT) {
                    int instance = din.readInt();
                    int prevContextId = din.readInt();
                    int pairCount = din.readInt();
                    String[] keyValues = new String[pairCount * 2];
                    for (int i = 0; i < pairCount * 2; i++) {
                        keyValues[i] = din.readUTF();
                    }
                    int contextId = contexts.size();
                    Context context = new Context(instance, new Details(keyValues), contexts.get(prevContextId));
                    contexts.put(contextId, context);
                    visitor.handleContext(contextId, context);
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
