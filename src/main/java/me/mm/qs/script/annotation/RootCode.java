package me.mm.qs.script.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks methods or classes whose bodies should be extracted and placed directly
 * at the root level of the script, removing the class and method wrapper.
 * 
 * Used to write code that executes immediately when the script loads,
 * without needing to wrap it in classes or methods.
 * 
 * When applied to a CLASS: extracts all methods' bodies to root level
 *   Example: @RootCode class Init { public void init() { ... } }
 *   Result: statements are placed directly at root level
 *
 * When applied to a METHOD: extracts only that method's body to root level
 *   Example: @RootCode void init() { ... }
 *   Result: statements inside init() are placed directly at root level
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RootCode {
}
