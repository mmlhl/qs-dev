package me.mm.qs.myscript.utils;

import me.mm.qs.script.QScriptBase;
import me.mm.qs.script.annotation.ScriptMethods;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * PCM 转 WAV 格式转换工具
 * 为解码后的 PCM 文件添加 WAV 文件头，使其可以被标准音频播放器播放
 */
@ScriptMethods
public class PcmToWavConverter extends QScriptBase {

    /**
     * 将 PCM 文件转换为 WAV 文件
     * 
     * @param pcmPath    PCM 文件路径
     * @param wavPath    WAV 输出文件路径
     * @param sampleRate 采样率 (Hz)，通常为 24000
     * @param channels   声道数，通常为 1 (单声道)
     * @param bitDepth   位深度，通常为 16
     * @return 转换是否成功
     */
    public boolean convertPcmToWav(String pcmPath, String wavPath, int sampleRate, int channels, int bitDepth) {
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(pcmPath);
            fos = new FileOutputStream(wavPath);

            // 获取 PCM 数据大小
            long pcmDataSize = fis.available();

            // 写入 WAV 文件头
            writeWavHeader(fos, pcmDataSize, sampleRate, channels, bitDepth);

            // 复制 PCM 数据
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            toast("WAV 转换成功: " + wavPath);
            return true;

        } catch (Exception e) {
            error(e);
            toast("WAV 转换失败: " + e.getMessage());
            return false;
        } finally {
            try {
                if (fis != null) fis.close();
                if (fos != null) fos.close();
            } catch (Exception e) {
                error(e);
            }
        }
    }

    /**
     * 简化版本 - 使用默认参数 (24000Hz, 单声道, 16位)
     * 
     * @param pcmPath PCM 文件路径
     * @param wavPath WAV 输出文件路径
     * @return 转换是否成功
     */
    public boolean convertPcmToWav(String pcmPath, String wavPath) {
        return convertPcmToWav(pcmPath, wavPath, 24000, 1, 16);
    }

    /**
     * 自动生成 WAV 文件名
     * 
     * @param pcmPath PCM 文件路径
     * @return WAV 文件路径，失败返回 null
     */
    public String convertPcmToWav(String pcmPath) {
        String wavPath = pcmPath.replace(".pcm", ".wav");
        if (convertPcmToWav(pcmPath, wavPath)) {
            return wavPath;
        }
        return null;
    }

    /**
     * 写入 WAV 文件头 (44 字节)
     */
    private void writeWavHeader(FileOutputStream fos, long pcmDataSize, int sampleRate, int channels, int bitDepth) throws Exception {
        long byteRate = (long) sampleRate * channels * bitDepth / 8;
        long totalSize = pcmDataSize + 36;

        // RIFF chunk descriptor
        fos.write("RIFF".getBytes());                    // ChunkID (4 bytes)
        writeInt(fos, (int) totalSize);                  // ChunkSize (4 bytes)
        fos.write("WAVE".getBytes());                    // Format (4 bytes)

        // fmt sub-chunk
        fos.write("fmt ".getBytes());                    // Subchunk1ID (4 bytes)
        writeInt(fos, 16);                               // Subchunk1Size (4 bytes) - 16 for PCM
        writeShort(fos, (short) 1);                      // AudioFormat (2 bytes) - 1 for PCM
        writeShort(fos, (short) channels);               // NumChannels (2 bytes)
        writeInt(fos, sampleRate);                       // SampleRate (4 bytes)
        writeInt(fos, (int) byteRate);                   // ByteRate (4 bytes)
        writeShort(fos, (short) (channels * bitDepth / 8)); // BlockAlign (2 bytes)
        writeShort(fos, (short) bitDepth);               // BitsPerSample (2 bytes)

        // data sub-chunk
        fos.write("data".getBytes());                    // Subchunk2ID (4 bytes)
        writeInt(fos, (int) pcmDataSize);                // Subchunk2Size (4 bytes)
    }

    /**
     * 写入 32 位整数 (小端序)
     */
    private void writeInt(FileOutputStream fos, int value) throws Exception {
        fos.write(value & 0xFF);
        fos.write((value >> 8) & 0xFF);
        fos.write((value >> 16) & 0xFF);
        fos.write((value >> 24) & 0xFF);
    }

    /**
     * 写入 16 位短整数 (小端序)
     */
    private void writeShort(FileOutputStream fos, short value) throws Exception {
        fos.write(value & 0xFF);
        fos.write((value >> 8) & 0xFF);
    }
}
