package me.mm.qs.script;

import android.content.Context;

/**
 * Global variables available in BeanShell runtime.
 * These are static in Java for compilation, but become global variables in BeanShell.
 */
public class Globals {
    
    /** Current user's QQ number */
    public static String myUin;
    
    /** QQ global context object */
    public static Context context;
    
    /** Script runtime directory */
    public static String appPath;
    
    /** QQ's class loader */
    public static ClassLoader loader;
    
    /** Current script ID */
    public static String pluginID;
    
    // Prevent instantiation
    private Globals() {}
}
