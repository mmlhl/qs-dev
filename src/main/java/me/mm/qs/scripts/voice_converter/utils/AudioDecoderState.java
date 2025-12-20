package me.mm.qs.scripts.voice_converter.utils;

import me.mm.qs.script.QScriptBase;
import me.mm.qs.script.annotation.GlobalInstance;

/**
 * 音频解码器状态数据类
 * 用于存储解码过程中的共享状态数据
 */
@GlobalInstance
public class AudioDecoderState extends QScriptBase {
    
    // 最后一次解码的采样率
    public static int lastSampleRate = 16000;
    
    // 最后一次解码的位深度
    public static int lastBitDepth = 16;
    
    // 最后一次解码的声道数
    public static int lastChannels = 1;
}
