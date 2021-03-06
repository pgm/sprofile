package org.github.sprofile.io;

import org.github.sprofile.Context;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static org.github.sprofile.io.Constants.*;

public class SnapshotStreamWriter implements Writer {
    final DataOutputStream out;

    static class HashableElement {
        String className;
        String methodName;
        String filename;
        int lineNumber;

        HashableElement(StackTraceElement element) {
            this.className = element.getClassName();
            this.methodName = element.getMethodName();
            this.filename = element.getFileName();
            this.lineNumber = element.getLineNumber();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            HashableElement that = (HashableElement) o;

            if (lineNumber != that.lineNumber) return false;
            if (className != null ? !className.equals(that.className) : that.className != null) return false;
            if (filename != null ? !filename.equals(that.filename) : that.filename != null) return false;
            if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = className != null ? className.hashCode() : 0;
            result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
            result = 31 * result + (filename != null ? filename.hashCode() : 0);
            result = 31 * result + lineNumber;
            return result;
        }
    }

    private Map<HashableElement, Integer> stackTraceElements = new HashMap();
    private Map<IdList, Integer> traces = new HashMap();
    private Map<Long, String> threadInfo = new HashMap();
    private Map<String, Integer> atoms = new HashMap();

    {
        atoms.put(null, 0);
    }

    // use a weak hashmap for contexts because we have the potential to accumulate a large number of these as parameter
    // values are not from a small possible set of values.  (Unlike file and method names)
    private Map<Context, Integer> contextIds = new WeakHashMap<Context, Integer>();

    {
        contextIds.put(null, 0);
    }

    private List<String> unwrittenAtoms = new ArrayList();
    private List<StackTraceElement> unwrittenStackTraceElements = new ArrayList();
    private List<IdList> unwrittenTraces = new ArrayList();
    private List<Context> unwrittenContexts = new ArrayList();

    public SnapshotStreamWriter(String processDescription, OutputStream os) {
        out = new DataOutputStream(os);
        try {
            out.writeByte(NEW_PROCESS);
            out.writeUTF(processDescription);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void flush() {
        try {
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {

        try {
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeCollectionFinished(int time) {
        try {
            out.writeByte(COLLECTION_TIME);
            out.writeInt(time);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void write(long timestamp, Map<Thread, StackTraceElement[]> dump, Map<Thread, Context> contexts) throws IOException {
        out.writeByte(TIMESTAMP);
        out.writeLong(timestamp);

        for (Thread thread : dump.keySet()) {
            StackTraceElement trace[] = dump.get(thread);
            Context context = contexts.get(thread);

            // write everything to a temporary buffer so we can collect all the
            // unwritten atoms first
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            writeThreadState(new DataOutputStream(buffer), thread, trace, context);

            for (Context c : unwrittenContexts) {
                int prevContextId = getContextId(c.getPrevious());
                out.writeByte(NEW_CONTEXT);
                out.writeInt(c.getInstance());
                out.writeInt(prevContextId);
                String[] keyValues = c.getKeyValues();
                out.writeInt(keyValues.length / 2);
                for (int i = 0; i < keyValues.length / 2; i++) {
                    String key = keyValues[i * 2];
                    String value = keyValues[i * 2];
                    out.writeInt(getAtomId(key));
                    out.writeInt(getAtomId(value));
                }
            }
            unwrittenContexts.clear();

            for (String atom : unwrittenAtoms) {
                out.writeByte(NEW_ATOM);
                out.writeUTF(atom);
            }
            unwrittenAtoms.clear();

            out.write(buffer.toByteArray());
        }
    }

    private void writeThreadState(DataOutputStream out, Thread thread2,
                                  StackTraceElement[] trace, Context context) throws IOException {
        String name = threadInfo.get(thread2.getId());
        if (name == null || !name.equals(thread2.getName())) {
            out.writeByte(THREAD_NAME);
            out.writeLong(thread2.getId());
            out.writeInt(getAtomId(thread2.getName()));
        }

        int traceId = getTraceId(trace);
        int contextId = getContextId(context);

        writeSample(out, thread2.getId(), thread2.getState(), traceId, contextId);
    }

    private int getAtomId(String str) {
        Integer id = atoms.get(str);
        if (id == null) {
            id = atoms.size();
            atoms.put(str, id);
            unwrittenAtoms.add(str);
        }
        return id;
    }

    private void writeSample(DataOutputStream out, long threadId, Thread.State state, int traceId, int contextId)
            throws IOException {

        for (StackTraceElement e : unwrittenStackTraceElements) {
            out.writeByte(NEW_TRACE_ELEMENT);
            out.writeInt(getAtomId(e.getClassName()));
            out.writeInt(getAtomId(e.getFileName()));
            out.writeInt(getAtomId(e.getMethodName()));
            out.writeInt(e.getLineNumber());
        }
        unwrittenStackTraceElements.clear();

        for (IdList ids : unwrittenTraces) {
            out.writeByte(NEW_TRACE);
            out.writeShort(ids.ids.length);
            for (int id : ids.ids) {
                out.writeInt(id);
            }
        }
        unwrittenTraces.clear();

        out.writeByte(OBSERVED_TRACE);
        out.writeLong(threadId);
        out.writeByte(state.ordinal());
        out.writeInt(traceId);
        out.writeInt(contextId);
    }

    protected int getContextId(Context context) {
        Integer id = contextIds.get(context);
        if (id == null) {
            id = contextIds.size();
            contextIds.put(context, id);
            unwrittenContexts.add(context);
        }
        return id;
    }


    private int getTraceId(StackTraceElement[] trace) {
        int[] ids = new int[trace.length];
        for (int i = 0; i < trace.length; i++) {
            ids[i] = getTraceElementId(trace[i]);
        }
        IdList idList = new IdList(ids);

        Integer traceId = traces.get(idList);
        if (traceId == null) {
            traceId = traces.size();
            traces.put(idList, traceId);
            unwrittenTraces.add(idList);
        }

        return traceId;
    }

    private int getTraceElementId(StackTraceElement stackTraceElement) {
        HashableElement e = new HashableElement(stackTraceElement);
        Integer id = stackTraceElements.get(e);
        if (id == null) {
            id = stackTraceElements.size();
            stackTraceElements.put(e, id);
            unwrittenStackTraceElements.add(stackTraceElement);
        }
        return id;
    }

}
