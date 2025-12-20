package me.mm.qs.script.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method or class whose body should be extracted and placed directly
 * at the root level (not wrapped in a method).
 * 
 * When applied to a METHOD: extracts only that method's body to root level
 * When applied to a CLASS: extracts all methods' bodies in the class to root level
 * 
 * Used for initialization code that runs immediately when the script loads.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RootCode {
}
