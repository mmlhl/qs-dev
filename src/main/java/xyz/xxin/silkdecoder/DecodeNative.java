package xyz.xxin.silkdecoder;

/**
 * Native decoder wrapper for libdecoder.so
 * This class provides JNI bindings to the native silk2mp3 function
 */
public class DecodeNative {
    
    /**
     * Convert silk audio file to mp3
     * @param inputPath input silk/pcm file path
     * @param outputPath output mp3 file path
     * @return 0 on success, error code otherwise
     */
    public static native int silk2mp3(String inputPath, String outputPath);
}
