/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.beans;

import java.util.*;
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.*;

/**
 * TableFormat implementation that uses reflection to be used for any
 * JavaBean-like Object with getProperty() and setProperty() style API.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author <a href="mailto:andrea.aime@aliceposta.it">Andrea Aime</a>
 */
public class BeanTableFormat<E> implements WritableTableFormat<E>, AdvancedTableFormat<E> {

    /** methods for extracting field values */
    protected BeanProperty<E>[] beanProperties = null;

    /** Java Beans property names */
    protected String[] propertyNames;

    /** column labels are pretty-print column header labels */
    protected String[] columnLabels;

    /** whether all columns can be edited */
    private boolean[] editable;

    /** column comparators */
    protected Comparator[] comparators;
    
    /** column classes */
    protected Class[] classes;
    
    /** primitive class to object class conversion map */
    protected static Map<Class,Class> primitiveToObjectMap;
    static {
        primitiveToObjectMap = new HashMap<Class,Class>();
        primitiveToObjectMap.put(boolean.class, Boolean.class);
        primitiveToObjectMap.put(char.class, Character.class);
        primitiveToObjectMap.put(byte.class, Byte.class);
        primitiveToObjectMap.put(short.class, Short.class);
        primitiveToObjectMap.put(int.class, Integer.class);
        primitiveToObjectMap.put(long.class, Long.class);
        primitiveToObjectMap.put(float.class, Float.class);
        primitiveToObjectMap.put(double.class, Double.class);
    }
    

    /**
     * Create a BeanTableFormat that uses the specified column names
     * and the specified field names while offering editable columns.
     */
    public BeanTableFormat(Class<E> beanClass, String[] propertyNames, String[] columnLabels, boolean[] editable) {
        this.propertyNames = propertyNames;
        this.columnLabels = columnLabels;
        this.editable = editable;
        
        // set up the AdvancedTableFormat properties
        comparators = new Comparator[propertyNames.length];
        classes = new Class[propertyNames.length];

        // use default properties if no class is specified
        if(beanClass == null) {
            for(int c = 0; c < classes.length; c++) {
                classes[c] = Object.class;
                comparators[c] = GlazedLists.comparableComparator();
            }

        // use detected properties if class is specified
        } else {
            loadPropertyDescriptors(beanClass);
            for(int c = 0; c < classes.length; c++) {
                // class
                Class rawClass = beanProperties[c].getValueClass();
                if(primitiveToObjectMap.containsKey(rawClass)) {
                    classes[c] = primitiveToObjectMap.get(rawClass);
                } else {
                    classes[c] = rawClass;
                }
                // comparator
                if(Comparable.class.isAssignableFrom(classes[c])) comparators[c] = GlazedLists.comparableComparator();
                else comparators[c] = null;
            }
        }
    }
    public BeanTableFormat(Class<E> beanClass, String[] propertyNames, String[] columnLabels) {
        this(beanClass, propertyNames, columnLabels, new boolean[propertyNames.length]);
    }

    /**
     * Loads the property descriptors which are used to invoke property
     * access methods using the property names.
     */
    protected void loadPropertyDescriptors(Class<E> beanClass) {
        beanProperties = new BeanProperty[propertyNames.length];
        for(int p = 0; p < propertyNames.length; p++) {
            beanProperties[p] = new BeanProperty<E>(beanClass, propertyNames[p], true, editable[p]);
        }
    }

    // TableFormat // // // // // // // // // // // // // // // // // // // //

    /**
     * The number of columns to display.
     */
    public int getColumnCount() {
        return columnLabels.length;
    }

    /**
     * Gets the title of the specified column.
     */
    public String getColumnName(int column) {
        return columnLabels[column];
    }

    /**
     * Gets the value of the specified field for the specified object. This
     * is the value that will be passed to the editor and renderer for the
     * column. If you have defined a custom renderer, you may choose to return
     * simply the baseObject.
     */
    public Object getColumnValue(E baseObject, int column) {
        if(baseObject == null) return null;

        // load the property descriptors on first request
        if(beanProperties == null) loadPropertyDescriptors((Class<E>) baseObject.getClass());

        // get the property
        return beanProperties[column].get(baseObject);
    }

    
    // WritableTableFormat // // // // // // // // // // // // // // // // // //

    /**
     * For editing fields. This returns true if the specified Object in the
     * specified column can be edited by the user.
     *
     * @param baseObject the Object to test as editable or not. This will be
     *      an element from the source list.
     * @param column the column to test.
     * @return true if the object and column are editable, false otherwise.
     * @since 2004-August-27, as a replacement for isColumnEditable(int).
     */
    public boolean isEditable(E baseObject, int column) {
        return editable[column];
    }

    /**
     * Sets the specified field of the base object to the edited value. When
     * a column of a table is edited, this method is called so that the user
     * can specify how to modify the base object for each column.
     *
     * @param baseObject the Object to be edited. This will be the original
     *      Object from the source list.
     * @param editedValue the newly constructed value for the edited column
     * @param column the column which has been edited
     * @return the revised Object, or null if the revision shall be discarded.
     *      If not null, the EventTableModel will set() this revised value in
     *      the list and overwrite the previous value.
     */
    public E setColumnValue(E baseObject, Object editedValue, int column) {
        if(baseObject == null) return null;

        // load the property descriptors on first request
        if(beanProperties == null) loadPropertyDescriptors((Class<E>) baseObject.getClass());

        // set the property
        beanProperties[column].set(baseObject, editedValue);

        // return the modified result
        return baseObject;
    }

    
    // AdvancedTableFormat // // // // // // // // // // // // // // // // // //

    /**
     * Get the class of the specified column.
     */
    public Class getColumnClass(int column) {
        return classes[column];
    }

    /**
     * Get the comparator for the specified column.
     */
    public Comparator getColumnComparator(int column) {
        return comparators[column];
    }
}