package me.mm.qs.scripts.voice_converter.utils;

import me.mm.qs.script.QScriptBase;

import java.lang.reflect.Method;

import static me.mm.qs.script.Globals.context;
import static me.mm.qs.script.Globals.loader;

/**
 * Silk 库加载工具类
 * 通过方法签名动态判断版本，兼容多版本 QQ
 */
public class SilkLibraryLoader extends QScriptBase {

    /**
     * 主动加载 Silk 编解码库
     * 动态检测方法签名，兼容不同版本：
     * - 版本1: b(Context, String)
     * - 版本2: c(Context, String)
     * 
     * @return 是否加载成功
     */
    public boolean loadSilkLibrary() {
        try {
            Class silkLoaderClass = loader.loadClass("com.tencent.mobileqq.qqaudio.silk.a");
            Class contextClass = loader.loadClass("android.content.Context");
            
            // 尝试不同版本的方法签名
            String[] methodNames = {"b", "c"};
            Method loadMethod = null;
            
            for (String methodName : methodNames) {
                try {
                    loadMethod = silkLoaderClass.getMethod(methodName, contextClass, String.class);
                    break;
                } catch (NoSuchMethodException e) {
                    // 继续尝试下一个方法名
                }
            }
            
            if (loadMethod == null) {
                toast("找不到 Silk 加载方法");
                return false;
            }
            
            loadMethod.invoke(null, context, "codecsilk");
            return true;
            
        } catch (Exception e) {
            error(e);
            toast("Silk 库加载失败: " + e.toString());
            return false;
        }
    }
}
