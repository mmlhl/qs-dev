package me.mm.qs.scripts.voice_converter.utils;

import me.mm.qs.script.QScriptBase;
import me.mm.qs.scripts.voice_converter.utils.SilkLibraryLoader;
import me.mm.qs.scripts.voice_converter.utils.AudioDecoderState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static me.mm.qs.script.Globals.*;

/**
 * Silk 音频解码工具类
 * 使用底层 native 方法直接解码，自己实现头部解析，兼容多版本 QQ
 */
public class SilkAudioDecoder extends QScriptBase {

    // Silk 支持的采样率数组
    private static final int[] SAMPLE_RATES = {8000, 12000, 16000, 24000, 36000, 44100, 48000};
    
    // Silk 文件头标识
    private static final String SILK_HEADER = "#!SILK_V";

    /**
     * 检查 Silk 库是否已加载
     * 通过读取静态布尔常量 L 判断
     */
    private boolean isSilkLibraryLoaded(Class silkWrapperClass) {
        try {
            Field loadedField = silkWrapperClass.getDeclaredField("L");
            loadedField.setAccessible(true);
            return ((Boolean) loadedField.get(null)).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 读取 Silk 文件头部，返回采样率索引
     * 自己实现，不依赖 QQAudioUtils
     * 
     * @param inputStream 输入流
     * @return 采样率索引，失败返回 -1
     */
    private int readSilkHeader(InputStream inputStream) throws Exception {
        byte[] header = new byte[10];
        int bytesRead = inputStream.read(header, 0, 10);
        if (bytesRead != 10) {
            return -1;
        }
        
        // 验证 Silk 文件头标识 "#!SILK_V"
        // 头部格式：第1字节是采样率索引，后9字节是 "#!SILK_V" + 版本号
        String headerStr = new String(header, 1, 9);
        if (!headerStr.startsWith(SILK_HEADER)) {
            return -1;
        }
        
        // 第一个字节是采样率索引
        int sampleRateIndex = header[0] & 0xFF;
        if (sampleRateIndex >= SAMPLE_RATES.length) {
            return -1;
        }
        
        return sampleRateIndex;
    }

    /**
     * 根据索引获取采样率
     */
    private int getSampleRate(int index) {
        if (index >= 0 && index < SAMPLE_RATES.length) {
            return SAMPLE_RATES[index];
        }
        return 16000; // 默认采样率
    }

    public String decodeVoiceMessage(String localPath) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        Object decoder = null;
        long codecHandle = 0;
        
        try {
            File inputFile = new File(localPath);
            if (!inputFile.exists()) {
                toast("文件不存在");
                return null;
            }

            Class silkWrapperClass = loader.loadClass("com.tencent.mobileqq.utils.SilkCodecWrapper");

            // 通过静态布尔常量 L 检查 so 是否加载
            if (!isSilkLibraryLoaded(silkWrapperClass)) {
                SilkLibraryLoader silkLoader = new SilkLibraryLoader();
                silkLoader.loadSilkLibrary();
            }

            fis = new FileInputStream(inputFile);

            // 自己解析 Silk 文件头部
            int sampleRateIndex = readSilkHeader(fis);
            if (sampleRateIndex == -1) {
                toast("无效的Silk文件");
                return null;
            }

            int sampleRate = getSampleRate(sampleRateIndex);
            AudioDecoderState.lastSampleRate = sampleRate;

            // 输出路径
            String fileName = localPath.substring(localPath.lastIndexOf("/") + 1);
            if (fileName.endsWith(".slk") || fileName.endsWith(".silk")) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            fileName = fileName + ".pcm";
            String pcmPath = appPath + "/" + fileName;

            // 创建解码器实例（仅用于调用 native 方法）
            decoder = silkWrapperClass.getConstructor(
                loader.loadClass("android.content.Context"), 
                boolean.class
            ).newInstance(context, Boolean.FALSE);

            // 直接调用 native 方法创建解码器
            Method decoderNewMethod = silkWrapperClass.getMethod("SilkDecoderNew", int.class, int.class);
            codecHandle = ((Long) decoderNewMethod.invoke(decoder, Integer.valueOf(sampleRate), Integer.valueOf(0))).longValue();
            
            if (codecHandle == 0) {
                toast("创建解码器失败");
                return null;
            }

            // 获取底层 native decode 方法
            Method nativeDecodeMethod = silkWrapperClass.getMethod("decode", 
                long.class, byte[].class, byte[].class, int.class, int.class);
            Method deleteCodecMethod = silkWrapperClass.getMethod("deleteCodec", long.class);

            fos = new FileOutputStream(pcmPath);
            
            byte[] silkBuffer = new byte[4096];
            byte[] pcmBuffer = new byte[4096];
            int frameCount = 0;
            
            while (true) {
                // 读取数据块长度（小端序）
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
                
                // 直接调用 native decode 方法
                Integer pcmLength = (Integer) nativeDecodeMethod.invoke(
                    decoder,
                    Long.valueOf(codecHandle),
                    silkBuffer,
                    pcmBuffer,
                    Integer.valueOf(dataLength),
                    Integer.valueOf(dataLength)
                );
                
                if (pcmLength.intValue() > 0) {
                    fos.write(pcmBuffer, 0, pcmLength.intValue());
                    frameCount++;
                }
            }
            fos.flush();
            
            // 释放解码器
            if (codecHandle != 0) {
                deleteCodecMethod.invoke(decoder, Long.valueOf(codecHandle));
                codecHandle = 0;
            }
            
            return pcmPath;
            
        } catch (Exception e) {
            error(e);
            toast("失败: " + e.toString());
            return null;
        } finally {
            try {
                // 确保释放解码器
                if (codecHandle != 0 && decoder != null) {
                    Method deleteCodecMethod = decoder.getClass().getMethod("deleteCodec", long.class);
                    deleteCodecMethod.invoke(decoder, Long.valueOf(codecHandle));
                }
                if (fis != null) fis.close();
                if (fos != null) fos.close();
            } catch (Exception e) {
                error(e);
            }
        }
    }
}
