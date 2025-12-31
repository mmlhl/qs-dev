package me.mm.qs.scripts.voice_converter.utils;

import me.mm.qs.script.QScriptBase;
import me.mm.qs.scripts.voice_converter.utils.AudioDecoderState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import static me.mm.qs.script.Globals.*;

/**
 * PCM 音频格式转换器
 * 使用外部 FFmpeg 二进制文件进行音频编码
 * 输出 MP3 格式
 */
public class PcmToMp3Converter extends QScriptBase {

    private String binDir = null;       // FFmpeg 可执行文件目录（QQ 私有目录，只读）
    private String ffmpegPath = null;   // FFmpeg 可执行文件路径
    private String sourcePath = null;   // FFmpeg 源文件路径（脚本目录）

    /**
     * 检查 FFmpeg 是否已就绪
     */
    public boolean isReady() {
        initPaths();
        File ffmpegFile = new File(ffmpegPath);
        return ffmpegFile.exists() && ffmpegFile.canExecute();
    }

    /**
     * 初始化路径
     */
    private void initPaths() {
        if (sourcePath == null) {
            sourcePath = appPath + "/ffmpeg/bin/ffmpeg";
        }
        if (binDir == null) {
            File filesDir = context.getFilesDir();
            binDir = filesDir.getAbsolutePath() + "/bin";
        }
        if (ffmpegPath == null) {
            ffmpegPath = binDir + "/ffmpeg";
        }
    }

    /**
     * 确保 FFmpeg 可用（复制到只读目录）
     */
    public boolean ensureFFmpeg() {
        initPaths();
        
        File binDirFile = new File(binDir);
        File ffmpegFile = new File(ffmpegPath);
        File sourceFile = new File(sourcePath);
        
        // 检查源文件是否存在
        if (!sourceFile.exists()) {
            log("[FFmpeg] 源文件不存在: " + sourceFile.getAbsolutePath());
            toast("FFmpeg 文件不存在");
            return false;
        }
        log("[FFmpeg] 源文件: " + sourceFile.getAbsolutePath() + ", 大小: " + sourceFile.length());
        
        // 如果已存在且可执行，直接返回
        if (ffmpegFile.exists() && ffmpegFile.canExecute() && ffmpegFile.length() == sourceFile.length()) {
            log("[FFmpeg] 已就绪: " + ffmpegPath);
            return true;
        }
        
        // 如果目录存在但不可写，先设置为可写
        if (binDirFile.exists()) {
            binDirFile.setWritable(true, false);
        } else {
            binDirFile.mkdirs();
        }
        
        // 复制 ffmpeg 可执行文件（静态编译版本，不需要 .so 库）
        log("[FFmpeg] 复制 ffmpeg 到: " + ffmpegPath);
        if (!copyFile(sourceFile, ffmpegFile)) {
            log("[FFmpeg] 复制 ffmpeg 失败");
            return false;
        }
        ffmpegFile.setExecutable(true, false);
        ffmpegFile.setReadable(true, false);
        
        // 设置目录为只读（绕过 W^X 保护）
        binDirFile.setWritable(false, false);
        log("[FFmpeg] 目录设为只读: " + binDir + ", 可写: " + binDirFile.canWrite());
        
        // 验证可执行
        log("[FFmpeg] ffmpeg 可执行: " + ffmpegFile.canExecute());
        
        return ffmpegFile.exists();
    }

    /**
     * 复制文件
     */
    private boolean copyFile(File src, File dest) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(src);
            fos = new FileOutputStream(dest);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
            return true;
        } catch (Exception e) {
            log("[FFmpeg] 复制异常: " + e.getMessage());
            error(e);
            return false;
        } finally {
            try {
                if (fis != null) fis.close();
                if (fos != null) fos.close();
            } catch (Exception ignored) {}
        }
    }

    /**
     * 执行 FFmpeg 命令
     */
    private Process execFFmpeg(String[] args) throws Exception {
        String[] cmd = new String[args.length + 1];
        cmd[0] = ffmpegPath;
        System.arraycopy(args, 0, cmd, 1, args.length);
        
        log("[FFmpeg] 执行: " + java.util.Arrays.toString(cmd));
        
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        return pb.start();
    }

    /**
     * 获取 FFmpeg 版本信息（测试用）
     */
    public String getVersion() {
        if (!ensureFFmpeg()) {
            return null;
        }
        
        try {
            Process process = execFFmpeg(new String[]{ "-version" });
            
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log("[FFmpeg] " + line);
            }
            reader.close();
            
            int exitCode = process.waitFor();
            log("[FFmpeg] 退出码: " + exitCode);
            
            if (exitCode == 0) {
                return output.toString();
            } else {
                log("[FFmpeg] 执行失败");
                return null;
            }
        } catch (Exception e) {
            log("[FFmpeg] 异常: " + e.getMessage());
            error(e);
            return null;
        }
    }

    /**
     * 将 PCM 文件转换为 MP3 格式
     * 
     * @param pcmPath PCM 文件路径
     * @return MP3 文件路径，失败返回 null
     */
    public String convertToMp3(String pcmPath) {
        return convertToMp3(pcmPath, null);
    }

    /**
     * 将 PCM 文件转换为 MP3 格式
     * 
     * @param pcmPath PCM 文件路径
     * @param outputPath 输出路径，为 null 则自动生成
     * @return MP3 文件路径，失败返回 null
     */
    public String convertToMp3(String pcmPath, String outputPath) {
        try {
            File pcmFile = new File(pcmPath);
            if (!pcmFile.exists()) {
                log("[MP3] PCM 文件不存在: " + pcmPath);
                toast("PCM 文件不存在");
                return null;
            }
            if (pcmFile.length() == 0) {
                log("[MP3] PCM 文件为空: " + pcmPath);
                toast("PCM 文件为空");
                return null;
            }
            log("[MP3] PCM 文件大小: " + pcmFile.length() + " bytes");

            // 确保 FFmpeg 可用
            if (!ensureFFmpeg()) {
                return null;
            }

            // 生成输出路径（使用 .mp3 后缀）
            if (outputPath == null || outputPath.isEmpty()) {
                String fileName = pcmFile.getName();
                if (fileName.endsWith(".pcm")) {
                    fileName = fileName.substring(0, fileName.length() - 4);
                }
                outputPath = pcmFile.getParent() + "/" + fileName + ".mp3";
            }

            // 确保后缀是 .mp3
            if (!outputPath.toLowerCase().endsWith(".mp3")) {
                int lastDot = outputPath.lastIndexOf('.');
                if (lastDot > 0) {
                    outputPath = outputPath.substring(0, lastDot) + ".mp3";
                } else {
                    outputPath = outputPath + ".mp3";
                }
            }

            // 删除已存在的输出文件
            File outputFile = new File(outputPath);
            if (outputFile.exists()) {
                outputFile.delete();
            }

            // 从 AudioDecoderState 获取音频参数
            int sampleRate = AudioDecoderState.lastSampleRate;
            int channels = AudioDecoderState.lastChannels;
            
            if (sampleRate <= 0) {
                sampleRate = 16000; // 默认采样率
            }
            if (channels <= 0) {
                channels = 1; // 默认单声道
            }

            log("[MP3] 开始转换");
            log("[MP3] FFmpeg: " + ffmpegPath);
            log("[MP3] 输入: " + pcmPath);
            log("[MP3] 输出: " + outputPath);
            log("[MP3] 采样率: " + sampleRate + ", 声道: " + channels);

            // 构建 FFmpeg 参数
            String[] args = new String[]{
                "-y",                              // 覆盖输出文件
                "-f", "s16le",                     // 输入格式：16位小端序 PCM
                "-ar", String.valueOf(sampleRate), // 输入采样率
                "-ac", String.valueOf(channels),   // 输入声道数
                "-i", pcmPath,                     // 输入文件
                "-codec:a", "libmp3lame",          // MP3 编码器
                "-b:a", "128k",                    // 输出比特率
                outputPath                         // 输出文件
            };

            // 执行命令
            Process process = execFFmpeg(args);

            // 读取输出（避免进程阻塞）
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log("[MP3 stdout] " + line);
            }
            reader.close();

            // 等待进程完成
            int exitCode = process.waitFor();

            log("[MP3] FFmpeg 退出码: " + exitCode);
            log("[MP3] 输出文件存在: " + outputFile.exists() + ", 大小: " + (outputFile.exists() ? outputFile.length() : 0));
            
            if (exitCode == 0 && outputFile.exists() && outputFile.length() > 0) {
                return outputPath;
            } else {
                log("[MP3] FFmpeg 转换失败");
                log("[MP3] 输出: " + output.toString());
                return null;
            }

        } catch (Exception e) {
            error(e);
            toast("MP3 转换失败: " + e.getMessage());
            return null;
        }
    }
}
