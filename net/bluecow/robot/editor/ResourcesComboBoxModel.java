/*
 * Created on Sep 28, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class ResourcesComboBoxModel implements ComboBoxModel {

    private final List<String> items;
    
    private String selectedItem;
    
    ResourcesComboBoxModel(Project proj, FilenameFilter filter) {
        this.items = new ArrayList<String>();
        items.add(null);
        for (String item : proj.getAllResourceNames()) {
            if (filter == null || filter.accept(null, item)) {
                items.add(item);
            }
        }
        this.selectedItem = null;
    }
    
    public Object getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(Object anItem) {
        selectedItem = (String) anItem;
        fireSelectionChanged();
    }


    public Object getElementAt(int index) {
        return items.get(index);
    }

    public int getSize() {
        return items.size();
    }

    public List<ListDataListener> listeners = new ArrayList<ListDataListener>();
    
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }
    
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

    private void fireSelectionChanged() {
        // not sure why we use a list data event, but this is what DefaultComboBoxModel does.
        ListDataEvent evt = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1);
        for (int i = listeners.size() - 1; i >= 0; i--) {
            listeners.get(i).contentsChanged(evt);
        }
    }

}
