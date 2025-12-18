package me.mm.qs.script.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Script metadata annotation.
 * Used to define script information that will be written to info.prop and desc.txt
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ScriptInfo {
    /** Script name (required) */
    String name();
    
    /** Script author (required) */
    String author();
    
    /** Script version (auto-updated from git or timestamp) */
    String version() default "1.0";
    
    /** Script description (written to desc.txt) */
    String description() default "";
    
    /** Script tags (comma separated) */
    String tags() default "功能扩展";
    
    /** Script ID (auto-generated and fixed on first run, stored in .script-id file) */
    String id() default "";
}
