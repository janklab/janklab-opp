/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

// for inspecting themes
import javax.swing.UIManager;
import javax.swing.plaf.metal.*;
import java.lang.reflect.*;
import java.security.AccessControlException;

/**
 * A PLAFDetector provides a means to discover which versions of themes
 * are available on the host system.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class PLAFDetector {

    /**
     * Workaround method to get the metal theme, either "Steel" or "Ocean". This is because the
     * Metal look and feel's getName() property does not distinguish between versions correctly.
     * A bug has been submitted to the Sun Java bug database and will be reviewed. If fixed, we
     * can get rid of this ugly hack method.
     */
    public static String getMetalTheme() {
        try {
            MetalLookAndFeel metalLNF = (MetalLookAndFeel)UIManager.getLookAndFeel();
            Method getCurrentTheme = metalLNF.getClass().getMethod("getCurrentTheme", new Class[0]);
            MetalTheme currentTheme = (MetalTheme)getCurrentTheme.invoke(metalLNF, new Object[0]);
            return "Metal/" + currentTheme.getName();
        } catch(NoSuchMethodException e) {
            // must be Java 1.4 because getCurrentTheme() method does not exist
            // therefore the theme of interest is "Steel"
            return "Metal/Steel";
        } catch(Exception e) {
            e.printStackTrace();
            return "Metal/Steel";
        }
    }

    /**
     * Workaround method to get the Windows theme, either "Windows Classic" or
     * "Windows XP". The test for Windows XP is also an ugly hack because Swing's
     * pluggable look-and-feel provides no alternative means for determining if
     * the current theme is XP. For compatibility, the algorithm to determine if
     * the style is XP is derived from similar code in the XPStyle class.
     */
    public static String getWindowsTheme() {
        String classic = "Classic Windows";
        String xp = "Windows XP";

        // theme active property must be "Boolean.TRUE";
        String themeActiveKey = "win.xpstyle.themeActive";
        Boolean themeActive = (Boolean)java.awt.Toolkit.getDefaultToolkit().getDesktopProperty(themeActiveKey);
        if(themeActive == null) return classic;
        if(!themeActive.booleanValue()) return classic;

        // no "swing.noxp" system property
        String noXPProperty = "swing.noxp";
        try {
            if(System.getProperty(noXPProperty) != null) return classic;
        } catch (AccessControlException e) {
            // in WebStart, it is possible to receive this exception when
            // querying for the swing.noxp property - in which case we don't
            // want to acknowledge the security violation
        }

        // l&f class must not be "WindowsClassicLookAndFeel"
        String classicLnF = "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel";
        if(UIManager.getLookAndFeel().getClass().getName().equals(classicLnF)) return "Classic Windows";

        // must be XP
        return xp;
    }
}