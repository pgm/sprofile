package org.github.sprofile.ui.timeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/26/12
 * Time: 9:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelectionModel {
    int selectionStart;
    int selectionStop;

    List<SelectionListener> selectionListenerList = new ArrayList();

    public int getSelectionStart() {
        return this.selectionStart;
    }

    public int getSelectionStop() {
        return selectionStop;
    }

    public void setSelection(int selectionStart, int selectionStop) {
        this.selectionStart = selectionStart;
        this.selectionStop = selectionStop;
        fireSelectionChanged();
    }

    protected void fireSelectionChanged() {
        for (SelectionListener l : selectionListenerList) {
            l.selectionChanged();
        }
    }

    public void addSelectionListener(SelectionListener listener) {
        this.selectionListenerList.add(listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        this.selectionListenerList.remove(listener);
    }

}
