
package org.clc.android.app.redbox.util.string;

public class RedBoxStringUtil {
    static public boolean isNullOrEmpty(final String string) {
        if (string == null || "".equals(string)) {
            return true;
        }
        return false;
    }
}
