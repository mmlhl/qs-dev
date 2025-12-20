package me.mm.qs.scripts.voice_converter.utils;

import me.mm.qs.script.QScriptBase;
import me.mm.qs.scripts.voice_converter.utils.SilkLibraryLoader;
import me.mm.qs.scripts.voice_converter.utils.AudioDecoderState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

import static me.mm.qs.script.Globals.*;

/**
 * Silk 音频解码工具类
 * 改回用 InputStream，看看崩溃会不会提前
 */
public class SilkAudioDecoder extends QScriptBase {


    public String decodeVoiceMessage(String localPath) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        Object decoder = null;
        
        try {

            File inputFile = new File(localPath);
            if (!inputFile.exists()) {
                toast("文件不存在");
                return null;
            }

            Class silkWrapperClass = loader.loadClass("com.tencent.mobileqq.utils.SilkCodecWrapper");
            Class audioUtilsClass = loader.loadClass("com.tencent.mobileqq.qqaudio.QQAudioUtils");
            

            Method checkLoadedMethod = silkWrapperClass.getMethod("e");
            Boolean isLoaded = (Boolean) checkLoadedMethod.invoke(null);
            if (!isLoaded.booleanValue()) {
                SilkLibraryLoader silkLoader = new SilkLibraryLoader();
                silkLoader.loadSilkLibrary();
            }

            fis = new FileInputStream(inputFile);

            Method readHeaderMethod = audioUtilsClass.getMethod("d", loader.loadClass("java.io.InputStream"));
            Byte sampleRateIndex = (Byte) readHeaderMethod.invoke(null, fis);
            
            if (sampleRateIndex.byteValue() == -1) {
                toast("无效的Silk文件");
                return null;
            }

            int[] sampleRates = (int[]) audioUtilsClass.getField("a").get(null);
            int sampleRate = sampleRates[sampleRateIndex.byteValue()];
            AudioDecoderState.lastSampleRate = sampleRate; // 保存采样率

            
            // 输出路径
            String fileName = localPath.substring(localPath.lastIndexOf("/") + 1);
            if (fileName.endsWith(".slk") || fileName.endsWith(".silk")) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            fileName = fileName + ".pcm";
            
            String pcmPath = appPath + "/" + fileName;


            decoder = silkWrapperClass.getConstructor(
                loader.loadClass("android.content.Context"), 
                boolean.class
            ).newInstance(context, Boolean.FALSE);

            Method initMethod = silkWrapperClass.getMethod("a", int.class, int.class, int.class);
            initMethod.invoke(decoder, Integer.valueOf(sampleRate), 0,1);

            fos = new FileOutputStream(pcmPath);
            
            // 解码
            Method decodeMethod = silkWrapperClass.getMethod("c", int.class, int.class, byte[].class, byte[].class);
            byte[] silkBuffer = new byte[4096];
            byte[] pcmBuffer = new byte[4096];
            
            int frameCount = 0;
            
            while (true) {
                // 读取数据块长度
                int b1 = fis.read();
                int b2 = fis.read();
                if (b1 == -1 || b2 == -1) {
                    break;
                }
                
                int dataLength = (b1 & 0xFF) | ((b2 & 0xFF) << 8);
                if (dataLength <= 0 || dataLength > 4096) {
                    break;
                }
                
                // 读取数据块
                int bytesRead = fis.read(silkBuffer, 0, dataLength);
                if (bytesRead != dataLength) {
                    break;
                }
                
                // 解码
                Integer pcmLength = (Integer) decodeMethod.invoke(
                    decoder, 
                    Integer.valueOf(dataLength), 
                    Integer.valueOf(dataLength), 
                    silkBuffer, 
                    pcmBuffer
                );
                
                if (pcmLength.intValue() > 0) {
                    fos.write(pcmBuffer, 0, pcmLength.intValue());
                    frameCount++;
                }
            }
            fos.flush();
            return pcmPath;
            
        } catch (Exception e) {
            error(e);
            toast("失败: " + e.toString());
            return null;
        } finally {
            try {
                if (decoder != null) {
                    Method closeMethod = decoder.getClass().getMethod("close");
                    closeMethod.invoke(decoder);
                }
                if (fis != null) fis.close();
                if (fos != null) fos.close();
            } catch (Exception e) {
                error(e);
            }
        }
    }
}
