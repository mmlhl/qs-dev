//
// Decompiled by Jadx - 567ms
//
package com.tencent.mobileqq.videocodec.ffmpeg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;
import com.tencent.biz.qqstory.utils.d;
import com.tencent.image.SafeBitmapFactory;
import com.tencent.mobileqq.editor.database.PublishVideoEntry;
import com.tencent.mobileqq.qfix.redirect.IPatchRedirector;
import com.tencent.mobileqq.shortvideo.VideoEnvironment;
import com.tencent.mobileqq.utils.BaseImageUtil;
import com.tencent.mobileqq.utils.FileUtils;
import com.tencent.qphone.base.util.BaseApplication;
import com.tencent.qphone.base.util.QLog;
import dq.a;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FFmpegUtils {
    static IPatchRedirector $redirector_;

    public FFmpegUtils() {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 1)) {
            return;
        }
        iPatchRedirector.redirect((short) 1, this);
    }

    public static void changeOrientationInVideo(Context context, String str, String str2, String str3, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        ArrayList arrayList = new ArrayList();
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 12;
        fFmpegCommandUnit.arguments = new FFmpegUtils$1(str, str2, str3);
        fFmpegCommandUnit.callback = fFmpegExecuteResponseCallback;
        arrayList.add(fFmpegCommandUnit);
        FFmpeg fFmpeg = FFmpeg.getInstance(context);
        if (fFmpeg.isFFmpegCommandRunning()) {
            fFmpeg.insertFFmpegQueue(arrayList);
        } else {
            fFmpeg.cmdFFmpegQueue(arrayList);
        }
    }

    private static void clipAudio(PublishVideoEntry publishVideoEntry, ExecuteBinResponseCallback executeBinResponseCallback, String str, int i, int i2, String str2, ArrayList<FFmpegCommandUnit> arrayList) {
        if (FileUtils.fileExists(str2)) {
            d.e(str2);
        }
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 3;
        fFmpegCommandUnit.arguments = new FFmpegUtils$12(str, str2, i, i2);
        fFmpegCommandUnit.callback = new FFmpegUtils$13(executeBinResponseCallback, publishVideoEntry);
        arrayList.add(fFmpegCommandUnit);
    }

    public static void combinBackgroundMusic(Context context, String str, String str2, int i, int i2, String str3, ExecuteBinResponseCallback executeBinResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        combinBackgroundMusic(context, str, str2, i, i2, str3, true, executeBinResponseCallback);
    }

    public static void combinBackgroundMusicWithVideCodecH264(Context context, String str, String str2, int i, int i2, String str3, ExecuteBinResponseCallback executeBinResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        combineBackgroundMusicInner(false, context, str, str2, "h264", i, i2, str3, false, executeBinResponseCallback);
    }

    private static void combineBackgroundMusicInner(boolean z, Context context, String str, String str2, String str3, int i, int i2, String str4, boolean z2, ExecuteBinResponseCallback executeBinResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        long currentTimeMillis = System.currentTimeMillis();
        String auidoType = getAuidoType(str2);
        String str5 = a.c;
        File file = new File(str5);
        if (!file.exists()) {
            file.mkdirs();
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(str5);
        stringBuffer.append(str2.hashCode());
        stringBuffer.append("_");
        stringBuffer.append(i);
        stringBuffer.append("_");
        stringBuffer.append(i2);
        stringBuffer.append(auidoType);
        String str6 = new String(stringBuffer);
        ArrayList arrayList = new ArrayList();
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 3;
        fFmpegCommandUnit.arguments = new FFmpegUtils$6(str2, str6, i, i2);
        fFmpegCommandUnit.callback = new FFmpegUtils$7(executeBinResponseCallback);
        arrayList.add(fFmpegCommandUnit);
        FFmpegCommandUnit fFmpegCommandUnit2 = new FFmpegCommandUnit();
        fFmpegCommandUnit2.cmdType = 4;
        fFmpegCommandUnit2.arguments = new FFmpegUtils$8(str, str6, str3, str4, z2, i2);
        fFmpegCommandUnit2.callback = new FFmpegUtils$9(executeBinResponseCallback, str6, currentTimeMillis);
        arrayList.add(fFmpegCommandUnit2);
        FFmpeg newInstance = z ? FFmpeg.newInstance(context, true) : FFmpeg.getInstance(context, true);
        if (newInstance.isFFmpegCommandRunning()) {
            newInstance.insertFFmpegQueue(arrayList);
        } else {
            newInstance.cmdFFmpegQueue(arrayList);
        }
    }

    public static void combineDoodle(Context context, String str, String str2, String str3, ExecuteBinResponseCallback executeBinResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 15;
        fFmpegCommandUnit.arguments = new FFmpegUtils$10(str, str2, str3);
        fFmpegCommandUnit.callback = new FFmpegUtils$11(executeBinResponseCallback);
        ArrayList arrayList = new ArrayList();
        arrayList.add(fFmpegCommandUnit);
        FFmpeg fFmpeg = FFmpeg.getInstance(context, true);
        if (fFmpeg.isFFmpegCommandRunning()) {
            fFmpeg.insertFFmpegQueue(arrayList);
        } else {
            fFmpeg.cmdFFmpegQueue(arrayList);
        }
    }

    public static boolean combineTwoImg(String str, String str2, String str3) {
        if (!TextUtils.isEmpty(str) && !TextUtils.isEmpty(str2) && !TextUtils.isEmpty(str3)) {
            Bitmap decodeFile = SafeBitmapFactory.decodeFile(str2);
            Bitmap decodeFile2 = SafeBitmapFactory.decodeFile(str);
            if (decodeFile != null && decodeFile2 != null) {
                return combineTwoImg(decodeFile2, decodeFile, str3);
            }
        }
        return false;
    }

    private static void combineVideoAndAudio(String str, PublishVideoEntry publishVideoEntry, boolean z, ExecuteBinResponseCallback executeBinResponseCallback, int i, long j, String str2, ArrayList<FFmpegCommandUnit> arrayList, String str3, String str4, String str5) {
        if (FileUtils.fileExists(str)) {
            d.e(str);
        }
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 4;
        fFmpegCommandUnit.arguments = new FFmpegUtils$14(str5, str4, str, z, i);
        fFmpegCommandUnit.callback = new FFmpegUtils$15(executeBinResponseCallback, publishVideoEntry, str2, str4, str5, str3, j);
        arrayList.add(fFmpegCommandUnit);
    }

    public static void compressLocalVideo(Context context, String str, int i, int i2, int i3, boolean z, String str2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        ArrayList arrayList = new ArrayList();
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 11;
        fFmpegCommandUnit.arguments = new FFmpegUtils$3(str, i, i2, i3, z, str2);
        fFmpegCommandUnit.callback = fFmpegExecuteResponseCallback;
        arrayList.add(fFmpegCommandUnit);
        FFmpeg fFmpeg = FFmpeg.getInstance(context);
        if (fFmpeg.isFFmpegCommandRunning()) {
            fFmpeg.insertFFmpegQueue(arrayList);
        } else {
            fFmpeg.cmdFFmpegQueue(arrayList);
        }
    }

    public static void compressVideoWithBitrate(String str, String str2, int i, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws IOException, FFmpegCommandAlreadyRunningException {
        boolean isAvcodecNewVersion = VideoEnvironment.isAvcodecNewVersion();
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-i");
        arrayList.add(new File(str).getCanonicalPath());
        arrayList.add("-b:v");
        arrayList.add("" + i);
        if (!isAvcodecNewVersion) {
            arrayList.add("-profile:v");
            arrayList.add("baseline");
        }
        arrayList.add("-bufsize");
        arrayList.add("800k");
        arrayList.add("-r");
        arrayList.add("25");
        if (isAvcodecNewVersion) {
            arrayList.add("-c:v");
            arrayList.add("libo264rt");
        }
        arrayList.add("-c:a");
        arrayList.add("copy");
        arrayList.add(new File(str2).getCanonicalPath());
        FFmpeg.getInstance(BaseApplication.getContext()).execute((String[]) arrayList.toArray(new String[0]), fFmpegExecuteResponseCallback);
    }

    public static void convertPicToVideo(Context context, String str, String str2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        ArrayList arrayList = new ArrayList();
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 9;
        fFmpegCommandUnit.arguments = new FFmpegUtils$4(str, str2);
        fFmpegCommandUnit.callback = fFmpegExecuteResponseCallback;
        arrayList.add(fFmpegCommandUnit);
        FFmpeg fFmpeg = FFmpeg.getInstance(context);
        if (fFmpeg.isFFmpegCommandRunning()) {
            fFmpeg.insertFFmpegQueue(arrayList);
        } else {
            fFmpeg.cmdFFmpegQueue(arrayList);
        }
    }

    public static void convertPicToVideoWidthDuration(Context context, String str, String str2, String str3, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        ArrayList arrayList = new ArrayList();
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 20;
        fFmpegCommandUnit.arguments = new FFmpegUtils$5(str, str2, str3);
        fFmpegCommandUnit.callback = fFmpegExecuteResponseCallback;
        arrayList.add(fFmpegCommandUnit);
        FFmpeg fFmpeg = FFmpeg.getInstance(context);
        if (fFmpeg.isFFmpegCommandRunning()) {
            fFmpeg.insertFFmpegQueue(arrayList);
        } else {
            fFmpeg.cmdFFmpegQueue(arrayList);
        }
    }

    public static void convertVideoToMp3(boolean z, Context context, String str, String str2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        ArrayList arrayList = new ArrayList();
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 16;
        fFmpegCommandUnit.arguments = new FFmpegUtils$25(str, str2);
        fFmpegCommandUnit.callback = fFmpegExecuteResponseCallback;
        arrayList.add(fFmpegCommandUnit);
        FFmpeg newInstance = z ? FFmpeg.newInstance(context, false) : FFmpeg.getInstance(context);
        if (newInstance.isFFmpegCommandRunning()) {
            newInstance.insertFFmpegQueue(arrayList);
        } else {
            newInstance.cmdFFmpegQueue(arrayList);
        }
    }

    public static void detectMediaVolume(Context context, String str, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        ArrayList arrayList = new ArrayList();
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 22;
        fFmpegCommandUnit.arguments = new FFmpegUtils$24(str);
        fFmpegCommandUnit.callback = fFmpegExecuteResponseCallback;
        arrayList.add(fFmpegCommandUnit);
        FFmpeg fFmpeg = FFmpeg.getInstance(context);
        if (fFmpeg.isFFmpegCommandRunning()) {
            fFmpeg.insertFFmpegQueue(arrayList);
        } else {
            fFmpeg.cmdFFmpegQueue(arrayList);
        }
    }

    public static void generateGifFromVideoWithPalette(String str, String str2, String str3, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException {
        ArrayList arrayList = new ArrayList();
        arrayList.add("-ss");
        arrayList.add("0");
        arrayList.add("-t");
        arrayList.add("4");
        arrayList.add("-i");
        arrayList.add(str);
        arrayList.add("-i");
        arrayList.add(str2);
        arrayList.add("-lavfi");
        arrayList.add("fps=18,scale=280:-1:flags=lanczos,paletteuse=dither=floyd_steinberg");
        arrayList.add("-y");
        arrayList.add(str3);
        FFmpeg.getInstance(BaseApplication.getContext()).execute((String[]) arrayList.toArray(new String[0]), fFmpegExecuteResponseCallback);
    }

    public static void generatePalettePNG(String str, String str2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws IOException, FFmpegCommandAlreadyRunningException {
        ArrayList arrayList = new ArrayList();
        arrayList.add("-ss");
        arrayList.add("0");
        arrayList.add("-t");
        arrayList.add("1");
        arrayList.add("-i");
        arrayList.add(str);
        arrayList.add("-vf");
        arrayList.add("fps=18,scale=280:-1:flags=lanczos,palettegen=stats_mode=diff");
        arrayList.add("-y");
        arrayList.add(str2);
        FFmpeg.getInstance(BaseApplication.getContext()).execute((String[]) arrayList.toArray(new String[0]), fFmpegExecuteResponseCallback);
    }

    private static void getAudioFromMp4(PublishVideoEntry publishVideoEntry, String str, String str2, ExecuteBinResponseCallback executeBinResponseCallback, ArrayList<FFmpegCommandUnit> arrayList) {
        if (FileUtils.fileExists(str2)) {
            d.e(str2);
        }
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 18;
        fFmpegCommandUnit.arguments = new FFmpegUtils$18(str, str2);
        fFmpegCommandUnit.callback = new FFmpegUtils$19(executeBinResponseCallback, publishVideoEntry);
        arrayList.add(fFmpegCommandUnit);
    }

    public static String getAuidoType(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if (str.startsWith("http") && str.contains("?")) {
            str = str.substring(0, str.indexOf("?"));
        }
        int lastIndexOf = str.lastIndexOf(".");
        return (lastIndexOf <= -1 || lastIndexOf >= str.length() + (-1)) ? "" : str.substring(lastIndexOf);
    }

    public static String getSavePublishVidFileName(String str, String str2) {
        StringBuilder sb = new StringBuilder("QQStoryMoment");
        sb.append(System.currentTimeMillis());
        sb.append("_");
        sb.append(str);
        if (str2 == null) {
            str2 = ".mp4";
        }
        sb.append(str2);
        return sb.toString();
    }

    private static void getVideoFromMp4(PublishVideoEntry publishVideoEntry, String str, String str2, ExecuteBinResponseCallback executeBinResponseCallback, ArrayList<FFmpegCommandUnit> arrayList) {
        if (FileUtils.fileExists(str)) {
            d.e(str);
        }
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 19;
        fFmpegCommandUnit.arguments = new FFmpegUtils$16(str2, str);
        fFmpegCommandUnit.callback = new FFmpegUtils$17(executeBinResponseCallback, publishVideoEntry);
        arrayList.add(fFmpegCommandUnit);
    }

    public static boolean isProcessCompleted(Process process) {
        return Util.isProcessCompleted(process);
    }

    public static void killRunningProcesses() {
        FFmpeg.getInstance(BaseApplication.getContext()).killRunningProcessesForShortVideo(false);
    }

    private static void mixMusicAndOriginal(PublishVideoEntry publishVideoEntry, String str, String str2, String str3, float f, float f2, ExecuteBinResponseCallback executeBinResponseCallback, ArrayList<FFmpegCommandUnit> arrayList) {
        if (FileUtils.fileExists(str3)) {
            d.e(str3);
        }
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 17;
        fFmpegCommandUnit.arguments = new FFmpegUtils$22(str, str2, str3, f, f2);
        fFmpegCommandUnit.callback = new FFmpegUtils$23(executeBinResponseCallback, publishVideoEntry);
        arrayList.add(fFmpegCommandUnit);
    }

    public static void mixOriginalAndBackgroundMusic(Context context, String str, String str2, PublishVideoEntry publishVideoEntry, boolean z, ExecuteBinResponseCallback executeBinResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        String str3 = publishVideoEntry.backgroundMusicPath;
        int i = publishVideoEntry.backgroundMusicOffset;
        int i2 = publishVideoEntry.backgroundMusicDuration;
        float floatValue = Float.valueOf(publishVideoEntry.getStringExtra("originalRecordVolume", String.valueOf(0.7f))).floatValue();
        float floatValue2 = Float.valueOf(publishVideoEntry.getStringExtra("backgroundVolume", String.valueOf(1.0f))).floatValue();
        long currentTimeMillis = System.currentTimeMillis();
        String auidoType = getAuidoType(str3);
        boolean z2 = auidoType.equals(".mp4") || auidoType.equals(".m4a");
        String str4 = a.c;
        File file = new File(str4);
        if (!file.exists()) {
            file.mkdirs();
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(str4);
        stringBuffer.append(str3.hashCode());
        stringBuffer.append("_");
        stringBuffer.append(i);
        stringBuffer.append("_");
        stringBuffer.append(i2);
        stringBuffer.append(".m4a");
        String str5 = new String(stringBuffer);
        String str6 = publishVideoEntry.videoUploadTempDir + "clipNoneMp4Temp" + auidoType;
        ArrayList arrayList = new ArrayList();
        clipAudio(publishVideoEntry, executeBinResponseCallback, str3, i, i2, z2 ? str5 : str6, arrayList);
        if (!z2) {
            transcodeAudio(publishVideoEntry, str6, str5, executeBinResponseCallback, arrayList);
        }
        String str7 = publishVideoEntry.videoUploadTempDir + "mc_audio.mp4";
        String str8 = publishVideoEntry.videoUploadTempDir + "local_video_audio.m4a";
        String str9 = publishVideoEntry.videoUploadTempDir + "transcode_local_video_audio.m4a";
        if (publishVideoEntry.isLocalPublish) {
            getAudioFromMp4(publishVideoEntry, str, str8, executeBinResponseCallback, arrayList);
            transcodeAudio(publishVideoEntry, str8, str9, executeBinResponseCallback, arrayList);
        }
        StringBuffer stringBuffer2 = new StringBuffer();
        stringBuffer2.append(str4);
        stringBuffer2.append(str7.hashCode());
        stringBuffer2.append("_mix_with_music.mp4");
        String str10 = new String(stringBuffer2);
        String str11 = str4 + str.hashCode() + "_none_audio_video.mp4";
        if (!publishVideoEntry.isLocalPublish) {
            str9 = str7;
        }
        mixMusicAndOriginal(publishVideoEntry, str9, str5, str10, floatValue, floatValue2, executeBinResponseCallback, arrayList);
        getVideoFromMp4(publishVideoEntry, str11, str, executeBinResponseCallback, arrayList);
        combineVideoAndAudio(str2, publishVideoEntry, z, executeBinResponseCallback, i2, currentTimeMillis, str5, arrayList, str8, str10, str11);
        FFmpeg fFmpeg = FFmpeg.getInstance(context, true);
        if (fFmpeg.isFFmpegCommandRunning()) {
            fFmpeg.insertFFmpegQueue(arrayList);
        } else {
            fFmpeg.cmdFFmpegQueue(arrayList);
        }
    }

    public static void setTimestamp(Context context, String str, String str2, int i, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        ArrayList arrayList = new ArrayList();
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 13;
        fFmpegCommandUnit.arguments = new FFmpegUtils$2(str, str2, i);
        fFmpegCommandUnit.callback = fFmpegExecuteResponseCallback;
        arrayList.add(fFmpegCommandUnit);
        FFmpeg fFmpeg = FFmpeg.getInstance(context);
        if (fFmpeg.isFFmpegCommandRunning()) {
            fFmpeg.insertFFmpegQueue(arrayList);
        } else {
            fFmpeg.cmdFFmpegQueue(arrayList);
        }
    }

    private static void transcodeAudio(PublishVideoEntry publishVideoEntry, String str, String str2, ExecuteBinResponseCallback executeBinResponseCallback, ArrayList<FFmpegCommandUnit> arrayList) {
        if (FileUtils.fileExists(str2)) {
            d.e(str2);
        }
        FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
        fFmpegCommandUnit.cmdType = 21;
        fFmpegCommandUnit.arguments = new FFmpegUtils$20(str, str2);
        fFmpegCommandUnit.callback = new FFmpegUtils$21(executeBinResponseCallback, publishVideoEntry);
        arrayList.add(fFmpegCommandUnit);
    }

    public static void video2Gif(String str, String str2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws IOException, FFmpegCommandAlreadyRunningException {
        ArrayList arrayList = new ArrayList();
        arrayList.add("-i");
        arrayList.add(str);
        arrayList.add("-y");
        arrayList.add(str2);
        FFmpeg.getInstance(BaseApplication.getContext()).execute((String[]) arrayList.toArray(new String[0]), fFmpegExecuteResponseCallback);
    }

    public static void combinBackgroundMusic(Context context, String str, String str2, int i, int i2, String str3, boolean z, ExecuteBinResponseCallback executeBinResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        combineBackgroundMusicInner(false, context, str, str2, "copy", i, i2, str3, z, executeBinResponseCallback);
    }

    public static boolean combineTwoImg(Bitmap bitmap, Bitmap bitmap2, String str) {
        if (bitmap != null && bitmap2 != null && !TextUtils.isEmpty(str)) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int width2 = bitmap2.getWidth();
            int height2 = bitmap2.getHeight();
            Bitmap createBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, (Paint) null);
            if (width == width2 && height == height2) {
                canvas.drawBitmap(bitmap2, 0.0f, 0.0f, (Paint) null);
            } else {
                Matrix matrix = new Matrix();
                matrix.postScale(width / width2, height / height2);
                canvas.drawBitmap(bitmap2, matrix, null);
            }
            canvas.save();
            try {
                try {
                    BaseImageUtil.saveBitmapToFile(createBitmap, new File(str));
                    bitmap.recycle();
                    bitmap2.recycle();
                    return true;
                } catch (IOException e) {
                    if (QLog.isColorLevel()) {
                        QLog.e("FFmpegCmd", 2, "FFmpegUtils combineTwoImg IOException " + e.getMessage());
                    }
                    bitmap.recycle();
                    bitmap2.recycle();
                    return false;
                }
            } catch (Throwable th) {
                bitmap.recycle();
                bitmap2.recycle();
                throw th;
            }
        }
        QLog.e("FFmpegCmd", 2, "FFmpegUtils combineTwoImg error");
        return false;
    }

    public static Bitmap combineTwoImg(Bitmap bitmap, Bitmap bitmap2) {
        if (bitmap == null && bitmap2 == null) {
            QLog.e("FFmpegCmd", 2, "FFmpegUtils combineTwoImg error");
            return null;
        }
        if (bitmap == null) {
            return bitmap2;
        }
        if (bitmap2 == null) {
            return bitmap;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int width2 = bitmap2.getWidth();
        int height2 = bitmap2.getHeight();
        Bitmap createBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, (Paint) null);
        if (width == width2 && height == height2) {
            canvas.drawBitmap(bitmap2, 0.0f, 0.0f, (Paint) null);
        } else {
            Matrix matrix = new Matrix();
            matrix.postScale(width / width2, height / height2);
            canvas.drawBitmap(bitmap2, matrix, null);
        }
        canvas.save();
        bitmap.recycle();
        bitmap2.recycle();
        return createBitmap;
    }
}
