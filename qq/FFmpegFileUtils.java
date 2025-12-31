//
// Decompiled by Jadx - 562ms
//
package com.tencent.mobileqq.videocodec.ffmpeg;

import android.content.Context;
import android.text.TextUtils;
import com.tencent.mobileqq.qfix.redirect.IPatchRedirector;
import com.tencent.mobileqq.qfix.redirect.PatchRedirectCenter;
import com.tencent.mobileqq.shortvideo.VideoEnvironment;
import com.tencent.mobileqq.statistics.StatisticCollector;
import com.tencent.qphone.base.util.BaseApplication;
import com.tencent.qphone.base.util.QLog;
import com.tencent.video.decode.ShortVideoSoLoad;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class FFmpegFileUtils {
    static IPatchRedirector $redirector_ = null;
    public static final String FFMPEG_BIN_FILE_NAME_PIC = "trim_process_pic";
    public static final String FFMPEG_BIN_FILE_NAME_PIE = "trim_process_pie";
    private static String s_binFilePath;

    static {
        IPatchRedirector redirector = PatchRedirectCenter.getRedirector(39827);
        $redirector_ = redirector;
        if (redirector == null || !redirector.hasPatch((short) 2)) {
            s_binFilePath = null;
        } else {
            redirector.redirect((short) 2);
        }
    }

    public FFmpegFileUtils() {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 1)) {
            return;
        }
        iPatchRedirector.redirect((short) 1, this);
    }

    public static String getAVCodecSoFilePath(Context context) {
        return ShortVideoSoLoad.getShortVideoSoPath(context) + VideoEnvironment.getShortVideoSoLibName();
    }

    public static String getFFmpeg(Context context) {
        int i;
        if (TextUtils.isEmpty(s_binFilePath)) {
            File file = new File(context.getApplicationInfo().nativeLibraryDir, "/libtrim_process_pie.so");
            Throwable th = null;
            if (file.exists()) {
                i = 1;
            } else {
                QLog.d("FFmpegFileUtils", 1, "fix bin file path nativeLibPath notExists: " + file.getAbsolutePath());
                i = 2;
                try {
                    file = new File((String) ClassLoader.class.getDeclaredMethod("findLibrary", String.class).invoke(FFmpegFileUtils.class.getClassLoader(), FFMPEG_BIN_FILE_NAME_PIE));
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    th = e;
                    QLog.e("FFmpegFileUtils", 1, "getFFmpeg exception. ", th);
                }
            }
            if (!file.exists()) {
                i++;
                QLog.d("FFmpegFileUtils", 1, "fix bin file path classLoaderLibPath notExists: " + file.getAbsolutePath());
                file = new File(new File(context.getPackageCodePath()).getParentFile(), "/lib/arm64/libtrim_process_pie.so");
            }
            if (!file.exists() || th != null) {
                HashMap hashMap = new HashMap();
                hashMap.put("scene", "FFmpegFileUtils");
                hashMap.put("step", i + "");
                hashMap.put("exists", file.exists() + "");
                if (th != null) {
                    hashMap.put("exception", th.getClass().getSimpleName());
                }
                StatisticCollector.getInstance(BaseApplication.getContext()).collectPerformance((String) null, "ffmpeg_fix_bin_path", true, 0L, 0L, hashMap, (String) null);
            }
            if (!file.exists()) {
                QLog.d("FFmpegFileUtils", 1, "fix bin file path use old path");
                return ShortVideoSoLoad.getShortVideoSoPath(context) + FFMPEG_BIN_FILE_NAME_PIE;
            }
            s_binFilePath = file.getAbsolutePath();
        }
        return s_binFilePath;
    }
}
