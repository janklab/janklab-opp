/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
import javax.swing.event.*;
// this class uses tables for displaying message lists
import java.util.*;

/**
 * An EventListModel adapts a EventList to the ListModel interface making it
 * appropriate for use with a {@link JList}. Each element of the list
 * corresponds to an element in the {@link ListModel}.
 *
 * <p>The EventListModel class is <strong>not thread-safe</strong>. Unless otherwise
 * noted, all methods are only safe to be called from the event dispatch thread.
 * To do this programmatically, use {@link SwingUtilities#invokeAndWait(Runnable)}.
 * 
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=14">Bug 14</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=146">Bug 146</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=177">Bug 177</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=228">Bug 228</a>
 * @see SwingUtilities#invokeAndWait(Runnable)
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventListModel<E> implements ListEventListener<E>, ListModel {

    /** the proxy moves events to the Swing Event Dispatch thread */
    private TransformedList<E, E> swingSource = null;

    /** whom to notify of data changes */
    private List<ListDataListener> listeners = new ArrayList<ListDataListener>();

    /** whenever a list change covers greater than this many rows, redraw the whole thing */
    public int changeSizeRepaintAllThreshhold = Integer.MAX_VALUE;
    
    /** recycle the list data event to prevent unnecessary object creation */
    protected MutableListDataEvent listDataEvent = new MutableListDataEvent(this);

    /**
     * Creates a new widget that renders the specified list.
     */
    public EventListModel(EventList<E> source) {
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventListModel
        source.getReadWriteLock().readLock().lock();
        try {
            swingSource = GlazedListsSwing.swingThreadProxyList(source);

            // prepare listeners
            swingSource.addListEventListener(this);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }
    
    /**
     * For implementing the ListEventListener interface. This sends changes
     * to the table which can repaint the table cells. Because this class uses
     * a EventThreadProxy, it is guaranteed that all natural
     * calls to this method use the Swing thread.
     *
     * <p>This always sends discrete changes for the complete size of the list.
     * It may be more efficient to implement a threshhold where a large list
     * of changes are grouped together as a single change. This is how the
     * ListTable accepts large change events.
     */
    public void listChanged(ListEvent<E> listChanges) {
        // when all events have already been processed by clearing the event queue
        if(!listChanges.hasNext()) return;

        // for all changes, one block at a time
        while(listChanges.nextBlock()) {
            // get the current change info
            int startIndex = listChanges.getBlockStartIndex();
            int endIndex = listChanges.getBlockEndIndex();
            int changeType = listChanges.getType();

            // create a table model event for this block
            listDataEvent.setRange(startIndex, endIndex);
            if(changeType == ListEvent.INSERT) listDataEvent.setType(ListDataEvent.INTERVAL_ADDED);
            else if(changeType == ListEvent.DELETE) listDataEvent.setType(ListDataEvent.INTERVAL_REMOVED);
            else if(changeType == ListEvent.UPDATE) listDataEvent.setType(ListDataEvent.CONTENTS_CHANGED);

            // fire an event to notify all listeners
            fireListDataEvent(listDataEvent);
        }
    }

    /**
     * Retrieves the value at the specified location from the table.
     * 
     * Before every get, we need to validate the row because there may be an
     * update waiting in the event queue. For example, it is possible that
     * the source list has been updated by a database thread. Such a change
     * may have been sent as notification, but after this request in the
     * event queue. In the case where a row is no longer available, null is
     * returned. The value returned is insignificant in this case because the
     * Event queue will very shortly be repainting (or removing) the row
     * anyway.
     *
     * @see ca.odell.glazedlists.swing.EventTableModel#getValueAt(int,int) ListTable
     */
    public Object getElementAt(int index) {
        swingSource.getReadWriteLock().readLock().lock();
        try {
            return swingSource.get(index);
        } finally {
            swingSource.getReadWriteLock().readLock().unlock();
        }
    }
    
    /**
     * Gets the size of the list.
     */
    public int getSize() {
        swingSource.getReadWriteLock().readLock().lock();
        try {
            return swingSource.size();
        } finally {
            swingSource.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Registers the specified ListDataListener to receive updates whenever
     * this list changes.
     *
     * <p>The specified ListDataListener must <strong>not</strong> save a
     * reference to the ListDataEvent beyond the end of the notification
     * method. This is because the ListDataEvent is re-used to increase
     * the performance of this implementation.
     */
    public void addListDataListener(ListDataListener listDataListener) {
        listeners.add(listDataListener);
    }
    /**
     * Deregisters the specified ListDataListener from receiving updates
     * whenever this list changes.
     */
    public void removeListDataListener(ListDataListener listDataListener) {
        listeners.remove(listDataListener);
    }

    /**
     * Notifies all ListDataListeners about one block of changes in the list.
     */
    protected void fireListDataEvent(ListDataEvent listDataEvent) {
        // notify all listeners about the event
        for(int i = 0, n = listeners.size(); i < n; i++) {
            ListDataListener listDataListener = listeners.get(i);
            if(listDataEvent.getType() == ListDataEvent.CONTENTS_CHANGED) {
                listDataListener.contentsChanged(listDataEvent);
            } else if(listDataEvent.getType() == ListDataEvent.INTERVAL_ADDED) {
                listDataListener.intervalAdded(listDataEvent);
            } else if(listDataEvent.getType() == ListDataEvent.INTERVAL_REMOVED) {
                listDataListener.intervalRemoved(listDataEvent);
            }
        }
    }
    
    /**
     * Releases the resources consumed by this {@link EventListModel} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link EventListModel} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link EventListModel}
     * to be garbage collected before its source {@link EventList}. This is 
     * necessary for situations where an {@link EventListModel} is short-lived but
     * its source {@link EventList} is long-lived.
     * 
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link EventListModel} after it has been disposed.
     */
    public void dispose() {
        swingSource.removeListEventListener(this);
        swingSource.dispose();
    }
}