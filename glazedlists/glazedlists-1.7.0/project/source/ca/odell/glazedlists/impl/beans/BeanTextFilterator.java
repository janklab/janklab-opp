/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.beans;

// To work with Lists
import java.util.*;
// To implement the interface
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.Filterator;

/**
 * TextFilterator implementation that uses reflection to be used for any
 * JavaBean-like Object with getProperty() and setProperty() style API.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class BeanTextFilterator<D,E> implements TextFilterator<E>, Filterator<D,E> {

    /** Java Beans property names */
    private String[] propertyNames;

    /** methods for extracting field values */
    private BeanProperty[] beanProperties = null;

    /**
     * Create a BeanTextFilterator that uses the specified property names.
     */
    public BeanTextFilterator(String[] propertyNames) {
        this.propertyNames = propertyNames;
    }

    /** {@inheritDoc} */
    public void getFilterStrings(List<String> baseList, E element) {
        if(element == null) return;

        // load the property descriptors on first request
        if(beanProperties == null) loadPropertyDescriptors(element);

        // get the filter strings
        for(int p = 0; p < beanProperties.length; p++) {
            Object propertyValue = beanProperties[p].get(element);
            if(propertyValue == null) continue;
            baseList.add(propertyValue.toString());
        }
    }

    /** {@inheritDoc} */
    public void getFilterValues(List<D> baseList, E element) {
        if(element == null) return;

        // load the property descriptors on first request
        if(beanProperties == null) loadPropertyDescriptors(element);

        // get the filter strings
        for(int p = 0; p < beanProperties.length; p++) {
            Object propertyValue = beanProperties[p].get(element);
            if(propertyValue == null) continue;
            baseList.add((D)propertyValue);
        }
    }

    /**
     * Loads the property descriptors which are used to invoke property
     * access methods using the property names.
     */
    private void loadPropertyDescriptors(E beanObject) {
        Class<E> beanClass = (Class<E>) beanObject.getClass();
        beanProperties = new BeanProperty[propertyNames.length];
        for(int p = 0; p < propertyNames.length; p++) {
            beanProperties[p] = new BeanProperty<E>(beanClass, propertyNames[p], true, false);
        }
    }
}