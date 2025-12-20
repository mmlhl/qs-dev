package me.mm.qs.script.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记工具类为全局实例
 * 
 * 被此注解标记的类会在 BeanShell 脚本中自动实例化为全局变量
 * 变量名默认为类名首字母小写，也可以通过 value 参数自定义
 * 
 * 例如：
 * @GlobalInstance
 * public class SilkAudioDecoder { ... }
 * 
 * 会在 BeanShell 中生成：
 * silkAudioDecoder = new SilkAudioDecoder();
 * 
 * @GlobalInstance("audioDecoder")
 * public class SilkAudioDecoder { ... }
 * 
 * 会生成：
 * audioDecoder = new SilkAudioDecoder();
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalInstance {
    /**
     * 全局变量名称（可选）
     * 如果不指定，默认使用类名首字母小写
     */
    String value() default "";
}
