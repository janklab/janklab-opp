/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Ensures that ListEventAssembler.forwardEvent() works.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ForwardEventTest extends TestCase {

    /** the origin of all events */
    private EventList<String> source;
    
    /** gossipy list that forwards everything it hears */
    private ForwardingList<String> forwarding;
    
    /** listens to anything the forwarding list will say, and validates it */
    private ListConsistencyListener test;

    /**
     * Prepare for the test.
     */
    public void setUp() {
        source = new BasicEventList<String>();
        forwarding = new ForwardingList<String>(source);
        test = ListConsistencyListener.install(forwarding);
        test.setPreviousElementTracked(false);
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        source = null;
        forwarding = null;
        test = null;
    }
    
    /**
     * Tests that forwardEvent works.
     */
    public void testForwarding() {
        source.add("Pepsi");
        source.add("Coke");
        source.add("RC");
        test.assertConsistent();
        
        source.addAll(Arrays.asList(new String[] { "7-up", "Dr. Pepper", "Sprite" }));
        source.retainAll(Arrays.asList(new String[] { "Pepsi", "7-up", "RC" }));
        test.assertConsistent();
    }

    /**
     * Tests that forwardEvent works.
     */
    public void testNestedForwarding() {
        forwarding.beginEvent();
        source.add("Pepsi");
        source.add("Coke");
        source.add("RC");
        forwarding.commitEvent();
        test.assertConsistent();
        
        forwarding.beginEvent();
        source.addAll(Arrays.asList(new String[] { "7-up", "Dr. Pepper", "Sprite" }));
        source.retainAll(Arrays.asList(new String[] { "Pepsi", "7-up", "RC" }));
        forwarding.commitEvent();
        test.assertConsistent();
    }

    public void testBadListEventHandler_FixMe() {
        assertIllegalStateExceptionIsThrown(source, new GetTypeListener());
        assertIllegalStateExceptionIsThrown(source, new GetBlockStartIndexListener());
        assertIllegalStateExceptionIsThrown(source, new GetBlockEndIndexListener());
        assertIllegalStateExceptionIsThrown(source, new GetIndexListener());
        assertIllegalStateExceptionIsThrown(source, new GetTypeTooFarListener());
        assertIllegalStateExceptionIsThrown(source, new GetBlockStartIndexTooFarListener());
        assertIllegalStateExceptionIsThrown(source, new GetBlockEndIndexTooFarListener());
        assertIllegalStateExceptionIsThrown(source, new GetIndexTooFarListener());
    }
    private class GetTypeListener extends DoNotStartIteratingListEventListener {
        protected void breakListEvent(ListEvent l) { l.getType(); }
    }
    private class GetBlockStartIndexListener extends DoNotStartIteratingListEventListener {
        protected void breakListEvent(ListEvent l) { l.getBlockStartIndex(); }
    }
    private class GetBlockEndIndexListener extends DoNotStartIteratingListEventListener {
        protected void breakListEvent(ListEvent l) { l.getBlockEndIndex(); }
    }
    private class GetIndexListener extends DoNotStartIteratingListEventListener {
        protected void breakListEvent(ListEvent l) { l.getIndex(); }
    }
    private class GetTypeTooFarListener extends IterateTooFarListEventListener {
        protected void breakListEvent(ListEvent l) { l.getType(); }
    }
    private class GetBlockStartIndexTooFarListener extends IterateTooFarListEventListener {
        protected void breakListEvent(ListEvent l) { l.getBlockStartIndex(); }
    }
    private class GetBlockEndIndexTooFarListener extends IterateTooFarListEventListener {
        protected void breakListEvent(ListEvent l) { l.getBlockEndIndex(); }
    }
    private class GetIndexTooFarListener extends IterateTooFarListEventListener {
        protected void breakListEvent(ListEvent l) { l.getIndex(); }
    }

    private static void assertIllegalStateExceptionIsThrown(EventList list, ListEventListener listener) {
        list.addListEventListener(listener);
        try {
            list.add("this should throw an IllegalStateException");
            fail("failed to throw an expected IllegalStateException for a bad ListEventListener implementation");
        } catch (IllegalStateException ise) {
            // expected, do nothing
        }
        list.removeListEventListener(listener);
    }

    static class DoNotStartIteratingListEventListener implements ListEventListener {
        public void listChanged(ListEvent listChanges) {
            breakListEvent(listChanges);
        }

        // override me to do bad things
        protected void breakListEvent(ListEvent l) { }
    }

    static class IterateTooFarListEventListener implements ListEventListener {
        public void listChanged(ListEvent listChanges) {
            while (listChanges.next());

            // now try breaking the ListEvent
            breakListEvent(listChanges);
        }

        // override me to do bad things
        protected void breakListEvent(ListEvent l) { }
    }

    /**
     * Simple TransformationList that forwards events.
     */
    static class ForwardingList<E> extends TransformedList<E, E> {
        public ForwardingList(EventList<E> source) {
            super(source);
            source.addListEventListener(this);
        }
        protected boolean isWritable() {
            return false;
        }
        public void listChanged(ListEvent<E> e) {
            updates.forwardEvent(e);
        }
        public void beginEvent() {
            updates.beginEvent(true);
        }
        public void commitEvent() {
            updates.commitEvent();
        }
    }
}