package org.github.sprofile.ui.summary;

public class CallKey {
    final String className;
    final String methodName;

    public CallKey(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallKey callKey = (CallKey) o;

        if (className != null ? !className.equals(callKey.className) : callKey.className != null) return false;
        if (methodName != null ? !methodName.equals(callKey.methodName) : callKey.methodName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        return result;
    }
}
