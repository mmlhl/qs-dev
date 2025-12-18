package me.mm.qs.script.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class that should be converted to Object in BeanShell script.
 * Used for types that exist in QStory runtime but need IDE hints during development.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface BeanShellType {
    /**
     * The target type name in BeanShell (default is "Object")
     */
    String value() default "Object";
}
