package me.mm.qs.script.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class whose methods should be extracted and placed at the root level
 * in the BeanShell script (outside any class).
 * This allows writing methods directly without wrapping them in a class.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ScriptMethods {
}
