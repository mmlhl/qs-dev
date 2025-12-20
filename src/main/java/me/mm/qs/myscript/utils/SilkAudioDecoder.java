package me.mm.qs.myscript.utils;

import me.mm.qs.script.QScriptBase;
import me.mm.qs.script.annotation.ScriptMethods;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;

import static me.mm.qs.script.Globals.*;

/**
 * Silk 音频解码工具类
 * 绕过InputStream，直接使用字节数组解码
 */
@ScriptMethods
public class SilkAudioDecoder extends QScriptBase {

    public String decodeVoiceMessage(String localPath, String outputDir) {
        FileOutputStream fos = null;
        Object decoder = null;
        
        try {
            File inputFile = new File(localPath);
            if (!inputFile.exists()) {
                toast("文件不存在");
                return null;
            }
            
            // 读取整个文件到字节数组
            RandomAccessFile raf = new RandomAccessFile(inputFile, "r");
            byte[] fileData = new byte[(int)raf.length()];
            raf.readFully(fileData);
            raf.close();
            Class silkWrapperClass = loader.loadClass("com.tencent.mobileqq.utils.SilkCodecWrapper");
            Class audioUtilsClass = loader.loadClass("com.tencent.mobileqq.qqaudio.QQAudioUtils");
            
            // 检查 Silk 库
            Method checkLoaded = silkWrapperClass.getMethod("e");
            Boolean isLoaded = (Boolean) checkLoaded.invoke(null);
            if (!isLoaded.booleanValue()) {
                toast("Silk库未加载");
                return null;
            }

            if (fileData.length < 15) {
                toast("文件太小");
                return null;
            }
            
            // 查找 "#!SILK_V3" 标识（处理前面可能的特殊字符）
            int headerStart = -1;
            for (int i = 0; i < Math.min(10, fileData.length - 10); i++) {
                String checkHeader = new String(fileData, i, 10);
                if (checkHeader.contains("#!SILK_V")) {
                    headerStart = i;
                    // 找到#的确切位置
                    for (int j = 0; j < 10; j++) {
                        if (checkHeader.charAt(j) == '#') {
                            headerStart = i + j;
                            break;
                        }
                    }
                    break;
                }
            }
            
            if (headerStart == -1) {
                toast("非Silk文件");
                return null;
            }

            
            toast("读取采样率索引...");
            byte sampleRateIndex = fileData[headerStart + 10];
            toast("采样率索引值: " + sampleRateIndex);
            
            toast("获取采样率数组...");
            int[] sampleRates = (int[]) audioUtilsClass.getField("a").get(null);
            toast("采样率数组长度: " + sampleRates.length);
            if (sampleRateIndex < 0 || sampleRateIndex >= sampleRates.length) {
                toast("采样率错误");
                return null;
            }
            int sampleRate = sampleRates[sampleRateIndex];
            toast("采样率: " + sampleRate);
            
            // 输出路径
            String fileName = localPath.substring(localPath.lastIndexOf("/") + 1);
            if (fileName.endsWith(".slk") || fileName.endsWith(".silk")) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            fileName = fileName + ".pcm";
            
            String pcmPath;
            if (outputDir == null || outputDir.isEmpty()) {
                pcmPath = appPath + "/" + fileName;
            } else {
                pcmPath = outputDir + "/" + fileName;
            }

            // 创建解码器
            toast("创建解码器...");
            decoder = silkWrapperClass.getConstructor(
                loader.loadClass("android.content.Context"), 
                boolean.class
            ).newInstance(context, Boolean.FALSE);

            toast("初始化...");
            Method initMethod = silkWrapperClass.getMethod("a", int.class, int.class, int.class);
            initMethod.invoke(decoder, Integer.valueOf(sampleRate), Integer.valueOf(sampleRate), Integer.valueOf(1));

            fos = new FileOutputStream(pcmPath);
            
            // 解码
            Method decodeMethod = silkWrapperClass.getMethod("c", int.class, int.class, byte[].class, byte[].class);
            byte[] pcmBuffer = new byte[4096];
            
            int offset = headerStart + 11;
            int frameCount = 0;
            
            while (offset < fileData.length - 2) {
                int dataLength = (fileData[offset] & 0xFF) | ((fileData[offset + 1] & 0xFF) << 8);
                offset += 2;
                
                if (dataLength <= 0 || dataLength > 4096 || offset + dataLength > fileData.length) {
                    break;
                }
                
                byte[] silkBlock = new byte[dataLength];
                System.arraycopy(fileData, offset, silkBlock, 0, dataLength);
                offset += dataLength;
                
                Integer pcmLength = (Integer) decodeMethod.invoke(
                    decoder, 
                    Integer.valueOf(dataLength), 
                    Integer.valueOf(dataLength), 
                    silkBlock, 
                    pcmBuffer
                );
                
                if (pcmLength.intValue() > 0) {
                    fos.write(pcmBuffer, 0, pcmLength.intValue());
                    frameCount++;
                }
            }
            
            fos.flush();
            fos.close();
            fos = null;
            
            toast("解码成功！");
            toast("输出: " + pcmPath);
            toast("帧数: " + frameCount);
            
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
                if (fos != null) fos.close();
            } catch (Exception e) {
                error(e);
            }
        }
    }

    public String decodeVoiceMessage(String localPath) {
        return decodeVoiceMessage(localPath, null);
    }
}
