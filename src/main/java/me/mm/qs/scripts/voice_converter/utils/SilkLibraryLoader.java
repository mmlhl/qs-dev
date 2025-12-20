package me.mm.qs.scripts.voice_converter.utils;

import me.mm.qs.script.QScriptBase;

import java.lang.reflect.Method;

import static me.mm.qs.script.Globals.context;
import static me.mm.qs.script.Globals.loader;

/**
 * Silk 库加载工具类
 * 主动加载 Silk 编解码库
 */
public class SilkLibraryLoader extends QScriptBase {

    /**
     * 主动加载 Silk 编解码库
     * 调用 com.tencent.mobileqq.qqaudio.silk.a 的 b 方法
     * 
     * @return 是否加载成功
     */
    public boolean loadSilkLibrary() {
        try {
            // 获取目标类
            Class silkLoaderClass = loader.loadClass("com.tencent.mobileqq.qqaudio.silk.a");
            
            // 获取 b 方法，参数为 Context 和 String
            Method loadMethod = silkLoaderClass.getMethod("b",
                loader.loadClass("android.content.Context"), 
                String.class);
            
            // 调用 b 方法，传入 context 和 "codecsilk"
            Object result = loadMethod.invoke(null, context, "codecsilk");
            
            toast("Silk 库加载完成");
            return true;
            
        } catch (Exception e) {
            error(e);
            toast("Silk 库加载失败: " + e.toString());
            return false;
        }
    }
}
