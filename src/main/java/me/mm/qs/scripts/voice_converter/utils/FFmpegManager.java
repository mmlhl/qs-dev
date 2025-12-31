package me.mm.qs.scripts.voice_converter.utils;

import me.mm.qs.script.QScriptBase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static me.mm.qs.script.Globals.*;

/**
 * FFmpeg 二进制文件管理器
 * 负责下载和管理预编译的 FFmpeg 可执行文件
 */
public class FFmpegManager extends QScriptBase {

    // FFmpeg 下载地址（shaka-project 静态编译版本，直接可执行）
    private static final String FFMPEG_URL = 
        "https://github.com/shaka-project/static-ffmpeg-binaries/releases/download/n7.1-2/ffmpeg-linux-arm64";

    private String ffmpegPath = null;  // 可执行路径（应用私有目录）
    private String downloadPath = null; // 下载路径（脚本目录）
    private boolean isReady = false;

    /**
     * 获取 FFmpeg 路径
     * 如果不存在则返回 null
     */
    public String getFFmpegPath() {
        initPaths();
        
        File ffmpegFile = new File(ffmpegPath);
        // 只检查文件存在且大小合理
        if (ffmpegFile.exists() && ffmpegFile.length() > 1000000) {
            isReady = true;
            return ffmpegPath;
        }
        
        return null;
    }
    
    /**
     * 初始化路径
     */
    private void initPaths() {
        if (ffmpegPath == null) {
            // 使用 context 获取应用私有目录
            File filesDir = context.getFilesDir();
            ffmpegPath = filesDir.getAbsolutePath() + "/ffmpeg";
        }
        if (downloadPath == null) {
            downloadPath = appPath + "/ffmpeg";
        }
    }

    /**
     * 检查 FFmpeg 是否已就绪
     */
    public boolean isReady() {
        return getFFmpegPath() != null;
    }

    /**
     * 下载 FFmpeg（同步方法）
     * @return 成功返回路径，失败返回 null
     */
    public String downloadFFmpeg() {
        initPaths();

        File execFile = new File(ffmpegPath);
        File downloadFile = new File(downloadPath);
        
        // 如果可执行目录已存在，直接返回
        if (execFile.exists() && execFile.length() > 1000000) {
            log("[FFmpeg] 已存在: " + ffmpegPath);
            isReady = true;
            return ffmpegPath;
        }
        
        // 如果下载目录已存在，复制到可执行目录
        if (downloadFile.exists() && downloadFile.length() > 1000000) {
            log("[FFmpeg] 复制到应用私有目录...");
            if (copyFileWithJava(downloadFile, execFile)) {
                execFile.setExecutable(true, false);
                log("[FFmpeg] 就绪: " + ffmpegPath);
                isReady = true;
                return ffmpegPath;
            }
        }

        // 下载到脚本目录
        log("[FFmpeg] 开始下载...");
        log("[FFmpeg] URL: " + FFMPEG_URL);
        
        boolean success = downloadFile(FFMPEG_URL, downloadFile);
        
        if (success && downloadFile.exists() && downloadFile.length() > 1000000) {
            log("[FFmpeg] 下载完成，复制到应用私有目录...");
            
            if (copyFileWithJava(downloadFile, execFile)) {
                execFile.setExecutable(true, false);
                log("[FFmpeg] 就绪: " + ffmpegPath);
                isReady = true;
                return ffmpegPath;
            } else {
                log("[FFmpeg] 复制失败");
            }
        } else {
            log("[FFmpeg] 下载失败");
        }

        return null;
    }
    
    /**
     * 使用 Java I/O 复制文件（在 QQ 上下文中可以直接写入私有目录）
     */
    private boolean copyFileWithJava(File src, File dest) {
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
            
            log("[FFmpeg] 复制成功: " + dest.getAbsolutePath());
            return true;
        } catch (Exception e) {
            log("[FFmpeg] 复制异常: " + e.getMessage());
            return false;
        } finally {
            try {
                if (fis != null) fis.close();
                if (fos != null) fos.close();
            } catch (Exception ignored) {}
        }
    }

    /**
     * 下载文件
     */
    private boolean downloadFile(String urlStr, File outputFile) {
        HttpURLConnection connection = null;
        InputStream input = null;
        FileOutputStream output = null;

        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            // 处理重定向
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || 
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == 307 || responseCode == 308) {
                String newUrl = connection.getHeaderField("Location");
                connection.disconnect();
                return downloadFile(newUrl, outputFile);
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                log("[FFmpeg] HTTP 错误: " + responseCode);
                return false;
            }

            int fileLength = connection.getContentLength();
            log("[FFmpeg] 文件大小: " + (fileLength / 1024 / 1024) + " MB");

            input = connection.getInputStream();
            output = new FileOutputStream(outputFile);

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalRead = 0;
            int lastProgress = 0;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
                
                // 每 10% 打印一次进度
                if (fileLength > 0) {
                    int progress = (int) (totalRead * 100 / fileLength);
                    if (progress >= lastProgress + 10) {
                        lastProgress = progress;
                        log("[FFmpeg] 下载进度: " + progress + "%");
                    }
                }
            }

            output.flush();
            return true;

        } catch (Exception e) {
            log("[FFmpeg] 下载失败: " + e.getMessage());
            error(e);
            return false;
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
                if (connection != null) connection.disconnect();
            } catch (Exception ignored) {}
        }
    }

    /**
     * 删除 FFmpeg（用于更新）
     */
    public boolean deleteFFmpeg() {
        boolean result = true;
        
        // 删除可执行目录的文件
        if (ffmpegPath != null) {
            File execFile = new File(ffmpegPath);
            if (execFile.exists()) {
                result = execFile.delete();
            }
        }
        
        // 删除下载目录的文件
        if (downloadPath != null) {
            File downloadFile = new File(downloadPath);
            if (downloadFile.exists()) {
                result = result && downloadFile.delete();
            }
        }
        
        isReady = false;
        return result;
    }
}
