package me.mm.qs.myscript.utils;

import me.mm.qs.script.QScriptBase;
import me.mm.qs.myscript.enums.ChatType;

/**
 * General helper utilities.
 * Methods in this class will be extracted to the module file.
 */
public class Helper extends QScriptBase {

    /**
     * Format QQ number with display
     */
    public String formatQQ(String qq) {
        return "[QQ:" + qq + "]";
    }

    /**
     * Check if group is enabled for this feature
     */
    public boolean isGroupEnabled(String groupUin, String feature) {
        return false; // Will be replaced with: getBoolean(feature, groupUin, false) in BeanShell
    }

    /**
     * Toggle group feature
     */
    public void toggleGroupFeature(String groupUin, String feature) {
        // Will be replaced with actual implementation in BeanShell
    }
}
