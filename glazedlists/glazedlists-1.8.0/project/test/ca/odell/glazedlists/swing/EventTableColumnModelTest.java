/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * The general testing strategy in this class is to put a DefaultTableColumnModel
 * and an EventTableColumnModel through their paces and ensure that their
 * states match at each stage and that they fire identical sets of
 * TableModelEvents in identical orders.
 */
public class EventTableColumnModelTest extends SwingTestCase {

    // record the TableColumnModelEvents from each registered model
    // so they can be compared for consistency at a later time
    private TableColumnEventWatcher watcher;

    public void guiSetUp() {
        watcher = new TableColumnEventWatcher();
    }

    public void guiTestConstructor() {
        // test empty models
        EventTableColumnModel eventModel = watcher.createEventModel(new BasicEventList<TableColumn>());
        DefaultTableColumnModel defaultModel = watcher.createDefaultModel(Collections.EMPTY_LIST);
        assertEquals(eventModel, defaultModel);
        watcher.assertFiredEventsAgreeAndClear();

        // test models with data
        EventList<TableColumn> columns = new BasicEventList<TableColumn>();
        columns.add(createColumn("name"));
        columns.add(createColumn("age"));
        defaultModel = watcher.createDefaultModel(columns);
        eventModel = watcher.createEventModel(columns);
        assertEquals(eventModel, defaultModel);
        watcher.assertFiredEventsAgreeAndClear();

        // test model with null TableColumn
        try {
            new EventTableColumnModel(GlazedLists.eventListOf(new TableColumn[] {null}));
            fail("failed to throw an exception for a null TableColumn");
        } catch (IllegalStateException ise) {
            // expected
        }
    }

    public void guiTestAddColumn() {
        // create 3 models with the same TableColumns
        EventList<TableColumn> columns = new BasicEventList<TableColumn>();
        columns.add(createColumn("name"));
        columns.add(createColumn("age"));

        DefaultTableColumnModel defaultModel = watcher.createDefaultModel(columns);
        EventTableColumnModel eventModel1 = watcher.createEventModel(columns);
        EventTableColumnModel eventModel2 = watcher.createEventModel(GlazedLists.eventList(columns));

        assertEquals(eventModel1, defaultModel);
        assertEquals(eventModel2, defaultModel);
        assertEquals(eventModel1, eventModel2);

        defaultModel.getSelectionModel().addSelectionInterval(0, 0);
        eventModel1.getSelectionModel().addSelectionInterval(0, 0);
        eventModel2.getSelectionModel().addSelectionInterval(0, 0);

        // add a new TableColumn to each of the models
        final TableColumn newColumn = createColumn("new");

        assertEquals(0, newColumn.getPropertyChangeListeners().length);
        columns.add(newColumn);
        defaultModel.addColumn(newColumn);
        eventModel2.addColumn(newColumn);
        assertEquals(3, newColumn.getPropertyChangeListeners().length);

        assertEquals(eventModel1, defaultModel);
        assertEquals(eventModel2, defaultModel);
        assertEquals(eventModel1, eventModel2);
        watcher.assertFiredEventsAgreeAndClear();
    }

    public void guiTestRemoveColumn() {
        final TableColumn nameColumn = createColumn("name");
        EventList<TableColumn> columns = new BasicEventList<TableColumn>();
        columns.add(nameColumn);
        columns.add(createColumn("age"));
        DefaultTableColumnModel defaultModel = watcher.createDefaultModel(columns);
        EventTableColumnModel eventModel1 = watcher.createEventModel(columns);
        EventTableColumnModel eventModel2 = watcher.createEventModel(GlazedLists.eventList(columns));
        assertEquals(eventModel1, defaultModel);
        assertEquals(eventModel2, defaultModel);
        assertEquals(eventModel1, eventModel2);
        assertEquals(3, nameColumn.getPropertyChangeListeners().length);

        columns.remove(0);
        defaultModel.removeColumn(nameColumn);
        eventModel2.removeColumn(nameColumn);
        assertEquals(eventModel1, defaultModel);
        assertEquals(eventModel2, defaultModel);
        assertEquals(eventModel1, eventModel2);
        assertEquals(0, nameColumn.getPropertyChangeListeners().length);
        watcher.assertFiredEventsAgreeAndClear();
    }

    /**
     * At the moment, EventTableColumnModel does not broadcast move events.
     * In their place, listeners receive a columnRemoved() callback followed
     * immediately by a columnAdded() callback. This is because Glazed Lists
     * does not yet have a move event defined. The belief is that in practice
     * this will produce the same net results anyway.
     */
    public void guiTestMoveColumn() {
        final TableColumn nameColumn = createColumn("name");
        EventList<TableColumn> columns = new BasicEventList<TableColumn>();
        columns.add(nameColumn);
        columns.add(createColumn("age"));
        DefaultTableColumnModel defaultModel = watcher.createDefaultModel(columns);
        EventTableColumnModel eventModel1 = watcher.createEventModel(columns);
        EventTableColumnModel eventModel2 = watcher.createEventModel(GlazedLists.eventList(columns));
        assertEquals(eventModel1, defaultModel);
        assertEquals(eventModel2, defaultModel);
        assertEquals(eventModel1, eventModel2);
        assertEquals(3, nameColumn.getPropertyChangeListeners().length);

        // move the column at index 0 to index 1
        columns.remove(0);
        columns.add(nameColumn);
        defaultModel.removeColumn(nameColumn);
        defaultModel.addColumn(nameColumn);
        eventModel2.moveColumn(0, 1);
        assertEquals(eventModel1, defaultModel);
        assertEquals(eventModel2, defaultModel);
        assertEquals(eventModel1, eventModel2);
        assertEquals(3, nameColumn.getPropertyChangeListeners().length);

        // move the column at index 0 to index 0 (this simulates dragging a column to reorder it)
        columns.set(0, columns.get(0));
        defaultModel.moveColumn(0, 0);
        eventModel2.moveColumn(0, 0);

        watcher.assertFiredEventsAgreeAndClear();
    }
    
    public void guiTestMoveSelectedColumn() {
        final TableColumn nameColumn = createColumn("name");
        EventList<TableColumn> columns = new BasicEventList<TableColumn>();
        columns.add(nameColumn);
        columns.add(createColumn("age"));

        DefaultTableColumnModel defaultModel = watcher.createDefaultModel(columns);
        EventTableColumnModel eventModel = watcher.createEventModel(columns);
        assertEquals(eventModel, defaultModel);

        // move the unselected column at index 0 to index 1
        eventModel.moveColumn(0, 1);
        defaultModel.moveColumn(0, 1);
        assertEquals(eventModel, defaultModel);

        // select column 0
        eventModel.getSelectionModel().addSelectionInterval(0, 0);
        defaultModel.getSelectionModel().addSelectionInterval(0, 0);
        assertEquals(eventModel, defaultModel);

        // move the *selected* column at index 0 to index 1
        eventModel.moveColumn(0, 1);
        defaultModel.moveColumn(0, 1);
        assertEquals(eventModel, defaultModel);
    }

    public void guitTestGetColumnIndex() {
        EventList<TableColumn> columns = new BasicEventList<TableColumn>();
        columns.add(createColumn("name"));
        columns.add(createColumn("age"));
        DefaultTableColumnModel defaultModel = watcher.createDefaultModel(columns);
        EventTableColumnModel eventModel = watcher.createEventModel(columns);

        assertEquals(0, defaultModel.getColumnIndex("name"));
        assertEquals(0, eventModel.getColumnIndex("name"));

        assertEquals(1, defaultModel.getColumnIndex("age"));
        assertEquals(1, eventModel.getColumnIndex("age"));

        try {
            defaultModel.getColumnIndex("this is absent");
            fail("failed to throw IllegalArgumentException for an unknown TableColumn identifier");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            eventModel.getColumnIndex("this is absent");
            fail("failed to throw IllegalArgumentException for an unknown TableColumn identifier");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void guitTestGetColumn() {
        final TableColumn nameColumn = createColumn("name");
        final TableColumn ageColumn = createColumn("age");

        EventList<TableColumn> columns = GlazedLists.eventListOf(new TableColumn[] {nameColumn, ageColumn});
        DefaultTableColumnModel defaultModel = watcher.createDefaultModel(columns);
        EventTableColumnModel eventModel = watcher.createEventModel(columns);

        assertEquals(nameColumn, defaultModel.getColumn(0));
        assertEquals(nameColumn, eventModel.getColumn(0));

        assertEquals(ageColumn, defaultModel.getColumn(1));
        assertEquals(ageColumn, eventModel.getColumn(1));

        try {
            defaultModel.getColumn(2);
            fail("failed to throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            eventModel.getColumn(2);
            fail("failed to throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void guitTestGetColumnIndexAtX() {
        final TableColumn nameColumn = createColumn("name");
        final TableColumn ageColumn = createColumn("age");

        EventList<TableColumn> columns = GlazedLists.eventListOf(new TableColumn[] {nameColumn, ageColumn});
        DefaultTableColumnModel defaultModel = watcher.createDefaultModel(columns);
        EventTableColumnModel eventModel = watcher.createEventModel(columns);

        assertEquals(0, defaultModel.getColumnIndexAtX(1));
        assertEquals(0, eventModel.getColumnIndexAtX(1));

        assertEquals(1, defaultModel.getColumnIndexAtX(76));
        assertEquals(1, eventModel.getColumnIndexAtX(76));

        assertEquals(-1, defaultModel.getColumnIndexAtX(151));
        assertEquals(-1, eventModel.getColumnIndexAtX(151));
    }

    private static TableColumn createColumn(Object identifier) {
        TableColumn column = new TableColumn();
        column.setIdentifier(identifier);
        return column;
    }

    private void assertEquals(TableColumnModel model1, TableColumnModel model2) {
        assertTrue(Arrays.equals(model1.getSelectedColumns(), model2.getSelectedColumns()));
        assertEquals(model1.getTotalColumnWidth(), model2.getTotalColumnWidth());
        assertEquals(model1.getSelectedColumnCount(), model2.getSelectedColumnCount());
        assertEquals(model1.getColumnSelectionAllowed(), model2.getColumnSelectionAllowed());
        assertEquals(model1.getColumnMargin(), model2.getColumnMargin());
        assertEquals(model1.getColumnCount(), model2.getColumnCount());

        // verify that the Enumeration of columns matches
        Enumeration<TableColumn> columnEnum1 = model1.getColumns();
        Enumeration<TableColumn> columnEnum2 = model2.getColumns();
        while (columnEnum1.hasMoreElements() && columnEnum2.hasMoreElements()) {
            assertSame(columnEnum1.nextElement(), columnEnum2.nextElement());
        }
        assertFalse(columnEnum1.hasMoreElements());
        assertFalse(columnEnum2.hasMoreElements());

        for (int i = 0; i < model1.getColumnCount(); i++) {
            TableColumn column1 = model1.getColumn(i);
            TableColumn column2 = model2.getColumn(i);
            assertSame(column1, column2);
            final List<PropertyChangeListener> propertyChangeListeners = Arrays.asList(column1.getPropertyChangeListeners());
            assertTrue(propertyChangeListeners.contains(model1));
            assertTrue(propertyChangeListeners.contains(model2));

            final Object identifier = column1.getIdentifier();
            if (identifier != null) {
                assertEquals(i, model1.getColumnIndex(identifier));
                assertEquals(i, model2.getColumnIndex(identifier));
            }
        }
    }

    /**
     * This class watches the events broadcasted by a group of TableColumnModels
     * registered with it and stores then in Lists. The events include both
     * TableColumnModelEvent objects and ListSelectionEvent objects for the
     * underlying ListSelectionModel.
     *
     * After executing a variety of methods on the TableColumnModels which
     * broadcast events, the {@link #assertFiredEventsAgreeAndClear()} method
     * can be called to ensure that all watched TableColumnModels broadcasted
     * identical events in identical orders.
     */
    private static class TableColumnEventWatcher implements TableColumnModelListener {
        private Map<TableColumnModel, List<EventObject>> eventMap = new HashMap<TableColumnModel, List<EventObject>>();
        private Map<Object, TableColumnModel> objectToTableColumnModelMap = new HashMap<Object, TableColumnModel>();

        private EventTableColumnModel createEventModel(EventList<TableColumn> columns) {
            EventTableColumnModel model = new EventTableColumnModel(columns);
            model.setColumnSelectionAllowed(true);
            addModel(model);
            return model;
        }

        private DefaultTableColumnModel createDefaultModel(Collection<TableColumn> columns) {
            DefaultTableColumnModel model = new DefaultTableColumnModel();
            for (Iterator<TableColumn> i = columns.iterator(); i.hasNext();)
                model.addColumn(i.next());

            model.setColumnSelectionAllowed(true);
            addModel(model);
            return model;
        }


        private void addModel(TableColumnModel model) {
            model.addColumnModelListener(this);
            objectToTableColumnModelMap.put(model.getSelectionModel(), model);
        }

        private TableColumnModel getTableColumnModel(Object o) {
            if (o instanceof TableColumnModel)
                return (TableColumnModel) o;

            final TableColumnModel model = objectToTableColumnModelMap.get(o);
            if (model == null)
                throw new IllegalArgumentException("unable to locate TableColumnModel for key object: " + o);

            return model;
        }

        public void columnAdded(TableColumnModelEvent e) { addEvent(e); }
        public void columnRemoved(TableColumnModelEvent e) { addEvent(e); }
        public void columnMoved(TableColumnModelEvent e) { addEvent(e); }
        public void columnMarginChanged(ChangeEvent e) { addEvent(e); }
        public void columnSelectionChanged(ListSelectionEvent e) { addEvent(e); }

        /**
         * Check over every piece of state in the TableColumnModel and its
         * selection model that we can. All TableColumnModels being watched
         * must agree or an assertion will fail.
         */
        public void assertFiredEventsAgreeAndClear() {
            Map.Entry<TableColumnModel, List<EventObject>> previousEntry = null;
            for (Iterator<Map.Entry<TableColumnModel, List<EventObject>>> i = eventMap.entrySet().iterator(); i.hasNext();) {
                Map.Entry<TableColumnModel, List<EventObject>> currentEntry = i.next();

                if (previousEntry != null) {
                    List<EventObject> previousEventObjects = previousEntry.getValue();
                    List<EventObject> currentEventObjects = currentEntry.getValue();

                    assertEquals(previousEventObjects.size(), currentEventObjects.size());

                    for (int j = 0, n = previousEventObjects.size(); j < n; j++) {
                        EventObject previousEvent = previousEventObjects.get(j);
                        EventObject currentEvent = currentEventObjects.get(j);
                        
                        if (previousEvent instanceof TableColumnModelEvent) {
                            TableColumnModelEvent previous = (TableColumnModelEvent) previousEvent;
                            TableColumnModelEvent current = (TableColumnModelEvent) currentEvent;

                            assertEquals(previous.getFromIndex(), current.getFromIndex());
                            assertEquals(previous.getToIndex(), current.getToIndex());

                        } else if (previousEvent instanceof ChangeEvent) {
                            assertEquals(ChangeEvent.class, previousEvent.getClass());
                            assertEquals(ChangeEvent.class, currentEvent.getClass());

                        } else if (previousEvent instanceof ListSelectionEvent) {
                            ListSelectionEvent previous = (ListSelectionEvent) previousEvent;
                            ListSelectionEvent current = (ListSelectionEvent) currentEvent;

                            assertEquals(previous.getFirstIndex(), current.getFirstIndex());
                            assertEquals(previous.getLastIndex(), current.getLastIndex());
                            assertEquals(previous.getValueIsAdjusting(), current.getValueIsAdjusting());
                        }
                    }
                }

                previousEntry = currentEntry;
            }

            eventMap.clear();
        }

        private void addEvent(EventObject e) {
            final TableColumnModel model = getTableColumnModel(e.getSource());

            List<EventObject> eventList = eventMap.get(model);
            if (eventList == null) {
                eventList = new ArrayList<EventObject>();
                eventMap.put(model, eventList);
            }
        }
    }
}