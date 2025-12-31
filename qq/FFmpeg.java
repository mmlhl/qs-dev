//
// Decompiled by Jadx - 1260ms
//
package com.tencent.mobileqq.videocodec.ffmpeg;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.text.TextUtils;
import com.tencent.biz.qqstory.utils.d;
import com.tencent.common.config.AppSetting;
import com.tencent.mobileqq.qfix.redirect.IPatchRedirector;
import com.tencent.mobileqq.qfix.redirect.PatchRedirectCenter;
import com.tencent.mobileqq.shortvideo.VideoEnvironment;
import com.tencent.mobileqq.utils.FileUtils;
import com.tencent.mobileqq.utils.ag;
import com.tencent.qmethod.pandoraex.monitor.DeviceInfoMonitor;
import dq.a;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mq.c;

public class FFmpeg {
    static IPatchRedirector $redirector_ = null;
    public static final String MESSAGE_COPY_CMD_FAIL = "copy_fail";
    public static final String MESSAGE_COPY_CMD_SUCCESS = "copy_success";
    public static final String MESSAGE_INPUT_NULL = "input path is null";
    public static final String MESSAGE_TS_DONE = "TS file exists";
    protected static final long MINIMUM_TIMEOUT = 10000;
    protected static final int RESULT_FIAL = 0;
    protected static final int RESULT_NOT_DONE = -9999;
    protected static final int RESULT_SUCCESS = 1;
    public static final String TAG = "FFmpegCmd";
    protected static volatile FFmpeg instance;
    public final Context context;
    public FFmpegExecuteAsyncTask ffmpegExecuteAsyncTask;
    public ArrayList<FFmpegCommandUnit> mCmdQueue;
    public FFmpegCommandUnit mCurrentCommandUnit;
    public String mCurrentTaskUni;
    public boolean mIsFFmpegingCloseScreen;
    protected boolean mIsWorkThreadCallback;
    public int mLastTaskResult;
    protected long timeout;
    public ArrayList<String> tsFileList;

    static {
        IPatchRedirector redirector = PatchRedirectCenter.getRedirector(39483);
        $redirector_ = redirector;
        if (redirector == null || !redirector.hasPatch((short) 33)) {
            instance = null;
        } else {
            redirector.redirect((short) 33);
        }
    }

    FFmpeg(Context context) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 1)) {
            iPatchRedirector.redirect((short) 1, this, context);
            return;
        }
        this.timeout = Long.MAX_VALUE;
        this.mIsWorkThreadCallback = false;
        this.mCmdQueue = new ArrayList<>();
        this.tsFileList = new ArrayList<>();
        this.mIsFFmpegingCloseScreen = false;
        this.mLastTaskResult = RESULT_NOT_DONE;
        Context applicationContext = context.getApplicationContext();
        this.context = applicationContext;
        Util.setFileExecutable(new File(FFmpegFileUtils.getFFmpeg(applicationContext)));
    }

    private void detectMediaInfoVolume(String str, ExecuteBinResponseCallback executeBinResponseCallback) throws FFmpegCommandAlreadyRunningException {
        ArrayList arrayList = new ArrayList();
        arrayList.add("-i");
        arrayList.add(str);
        arrayList.add("-filter_complex");
        arrayList.add("volumedetect");
        arrayList.add("-c:v");
        arrayList.add("copy");
        arrayList.add("-f");
        arrayList.add("null");
        arrayList.add("/dev/null");
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), executeBinResponseCallback);
    }

    private void getAudioFromMP4(String str, String str2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException {
        ArrayList arrayList = new ArrayList();
        arrayList.add("-i");
        arrayList.add(str);
        arrayList.add("-vn");
        arrayList.add("-acodec");
        arrayList.add("copy");
        arrayList.add(str2);
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    public static FFmpeg getInstance(Context context) {
        return getInstance(context, false);
    }

    private void getVideoFromMP4(String str, String str2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException {
        ArrayList arrayList = new ArrayList();
        arrayList.add("-i");
        arrayList.add(str);
        arrayList.add("-an");
        arrayList.add("-vcodec");
        arrayList.add("copy");
        arrayList.add(str2);
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    private void mixAudio(String str, String str2, String str3, float f, float f2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException {
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-i");
        arrayList.add(str);
        arrayList.add("-i");
        arrayList.add(str2);
        arrayList.add("-filter_complex");
        arrayList.add("[0:a]volume=" + f + "[a0];[1:a]volume=" + f2 + "[a1];[a0][a1]amix=inputs=2:duration=first:dropout_transition=3");
        arrayList.add("-strict");
        arrayList.add("-2");
        arrayList.add("-vn");
        arrayList.add(str3);
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    public static FFmpeg newInstance(Context context, boolean z) {
        FFmpeg fFmpeg = new FFmpeg(context);
        fFmpeg.mIsWorkThreadCallback = z;
        return fFmpeg;
    }

    private String parseCmdStringByMillSecond(int i) {
        return String.format("%02d:%02d:%02d.%03d", Integer.valueOf((int) Math.floor(i / 3600000)), Integer.valueOf(((int) Math.floor(i % 3600000)) / 60000), Integer.valueOf((int) Math.floor((i % 60000) / 1000)), Integer.valueOf((int) Math.floor(i % 1000)));
    }

    public void changeOrientationInVideo(String str, String str2, String str3, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws IOException, FFmpegCommandAlreadyRunningException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 11)) {
            iPatchRedirector.redirect((short) 11, new Object[]{this, str, str2, str3, fFmpegExecuteResponseCallback});
            return;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-i");
        arrayList.add(new File(str).getCanonicalPath());
        arrayList.add("-metadata:s:v");
        arrayList.add("rotate=" + str2);
        arrayList.add("-codec");
        arrayList.add("copy");
        arrayList.add(new File(str3).getCanonicalPath());
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    public void changeOrientationInVideoByTranspose(String str, String str2, String str3, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws IOException, FFmpegCommandAlreadyRunningException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 12)) {
            iPatchRedirector.redirect((short) 12, new Object[]{this, str, str2, str3, fFmpegExecuteResponseCallback});
            return;
        }
        int intValue = Integer.valueOf(str2).intValue();
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-i");
        arrayList.add(new File(str).getCanonicalPath());
        arrayList.add("-vf");
        int i = intValue % 360;
        if (i == 0) {
            c.c(TAG, "no need rotate.");
            return;
        }
        if (i == 90) {
            arrayList.add("transpose=1");
        } else if (i == 180) {
            arrayList.add("transpose=1,transpose=1");
        } else {
            if (i != 270) {
                c.t(TAG, "unSupport orientation:%s", new Object[]{str2});
                return;
            }
            arrayList.add("transpose=1,transpose=1,transpose=1");
        }
        if (VideoEnvironment.isAvcodecNewVersion()) {
            arrayList.add("-c:v");
            arrayList.add("libo264rt");
            arrayList.add("-o264rt_params");
            arrayList.add("RCMode=-1:MinQp=22:MaxQp=22");
        }
        arrayList.add("-c:a");
        arrayList.add("copy");
        arrayList.add(new File(str3).getCanonicalPath());
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    public boolean checkSameTask(String str) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 32)) {
            return ((Boolean) iPatchRedirector.redirect((short) 32, this, str)).booleanValue();
        }
        String str2 = this.mCurrentTaskUni;
        return (str2 == null || str == null || !str.equals(str2)) ? false : true;
    }

    public void clipAudio(String str, String str2, int i, int i2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 22)) {
            iPatchRedirector.redirect((short) 22, new Object[]{this, str, str2, Integer.valueOf(i), Integer.valueOf(i2), fFmpegExecuteResponseCallback});
            return;
        }
        c.r(TAG, "clipAudio arguments: \n inAudio" + str + "\n outAudio:" + str2 + "\n start:" + i + "\n duration:" + i2);
        if (!d.g(str)) {
            c.g(TAG, "clipAudio but inAudio file is not exist");
            fFmpegExecuteResponseCallback.onFailure(String.valueOf(941001));
            fFmpegExecuteResponseCallback.onFinish(false);
            return;
        }
        String parseCmdStringByMillSecond = parseCmdStringByMillSecond(i);
        String parseCmdStringByMillSecond2 = parseCmdStringByMillSecond(i2);
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-i");
        arrayList.add(new File(str).getCanonicalPath());
        arrayList.add("-ss");
        arrayList.add(parseCmdStringByMillSecond);
        arrayList.add("-t");
        arrayList.add(parseCmdStringByMillSecond2);
        arrayList.add("-acodec");
        arrayList.add("copy");
        arrayList.add("-vn");
        arrayList.add(new File(str2).getCanonicalPath());
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    public void cmdFFmpegQueue(ArrayList<FFmpegCommandUnit> arrayList) throws FFmpegCommandAlreadyRunningException, IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 26)) {
            iPatchRedirector.redirect((short) 26, this, arrayList);
            return;
        }
        this.mCmdQueue = arrayList;
        if (arrayList.size() <= 0) {
            return;
        }
        FFmpegCommandUnit remove = this.mCmdQueue.remove(RESULT_FIAL);
        ArrayList arrayList2 = remove.arguments;
        String[] strArr = remove.cmd;
        FFmpeg$6 r12 = new FFmpeg$6(this, remove.callback, strArr, this.mCmdQueue);
        if (strArr != null) {
            execute(strArr, r12);
            return;
        }
        switch (remove.cmdType) {
            case RESULT_SUCCESS:
                watermark((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), (String) arrayList2.get(2), ((Integer) arrayList2.get(3)).intValue(), ((Integer) arrayList2.get(4)).intValue(), r12);
                return;
            case 2:
                concatMedia((List) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), r12, ((Boolean) arrayList2.get(2)).booleanValue());
                return;
            case 3:
                clipAudio((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), ((Integer) arrayList2.get(2)).intValue(), ((Integer) arrayList2.get(3)).intValue(), r12);
                return;
            case 4:
                combineAudioAndVideo((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), (String) arrayList2.get(2), (String) arrayList2.get(3), ((Boolean) arrayList2.get(4)).booleanValue(), ((Integer) arrayList2.get(5)).intValue(), r12);
                return;
            case 5:
                mp4Tots((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), r12);
                return;
            case 6:
                concatTsOutput((List) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), r12);
                return;
            case 7:
                concatMediaByTs((List) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), r12);
                return;
            case 8:
                concatDifferentCodingMedia((List) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), r12);
                return;
            case 9:
                convertPicToVideo((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), r12);
                return;
            case 10:
                hflip((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), r12);
                return;
            case 11:
                compressVideo((String) arrayList2.get(RESULT_FIAL), ((Integer) arrayList2.get(RESULT_SUCCESS)).intValue(), ((Integer) arrayList2.get(2)).intValue(), ((Integer) arrayList2.get(3)).intValue(), ((Boolean) arrayList2.get(4)).booleanValue(), (String) arrayList2.get(5), r12);
                return;
            case 12:
                changeOrientationInVideoByTranspose((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), (String) arrayList2.get(2), r12);
                return;
            case 13:
                setTimestamp((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), ((Integer) arrayList2.get(2)).intValue(), r12);
                return;
            case 14:
                emptyFFmengCmd((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), r12);
                return;
            case 15:
                combineDoodle((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), (String) arrayList2.get(2), r12);
                return;
            case 16:
                convertMp4ToMp3((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), r12);
                return;
            case 17:
                mixAudio((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), (String) arrayList2.get(2), ((Float) arrayList2.get(3)).floatValue(), ((Float) arrayList2.get(4)).floatValue(), r12);
                return;
            case 18:
                getAudioFromMP4((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), r12);
                return;
            case 19:
                getVideoFromMP4((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), r12);
                return;
            case 20:
                convertPicToVideoWithTime((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), (String) arrayList2.get(2), r12);
                return;
            case 21:
                transcodeM4a((String) arrayList2.get(RESULT_FIAL), (String) arrayList2.get(RESULT_SUCCESS), r12);
                return;
            case 22:
                detectMediaInfoVolume((String) arrayList2.get(RESULT_FIAL), r12);
                return;
            default:
                return;
        }
    }

    public Clip combineAudioAndVideo(String str, String str2, String str3, String str4, boolean z, int i, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 23)) {
            return (Clip) iPatchRedirector.redirect((short) 23, new Object[]{this, str, str2, str3, str4, Boolean.valueOf(z), Integer.valueOf(i), fFmpegExecuteResponseCallback});
        }
        Clip clip = new Clip(str4);
        clip.videoCodec = str3;
        return combineAudioAndVideo(str, str2, z, clip, i, fFmpegExecuteResponseCallback);
    }

    public void combineDoodle(String str, String str2, String str3, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 25)) {
            iPatchRedirector.redirect((short) 25, new Object[]{this, str, str2, str3, fFmpegExecuteResponseCallback});
            return;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add("-i");
        arrayList.add(str);
        arrayList.add("-i");
        arrayList.add(str2);
        if (VideoEnvironment.isAvcodecNewVersion()) {
            arrayList.add("-c:v");
            arrayList.add("libo264rt");
        }
        arrayList.add("-filter_complex");
        arrayList.add("[1:v]scale=iw:ih[s];[0:v][s]overlay=0:0");
        arrayList.add(str3);
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    public void compressVideo(String str, int i, int i2, int i3, boolean z, String str2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws IOException, FFmpegCommandAlreadyRunningException {
        int[] iArr;
        int i4;
        int i5;
        int i6;
        IPatchRedirector iPatchRedirector = $redirector_;
        boolean z2 = true;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 10)) {
            iPatchRedirector.redirect((short) 10, new Object[]{this, str, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), Boolean.valueOf(z), str2, fFmpegExecuteResponseCallback});
            return;
        }
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            try {
                mediaMetadataRetriever.setDataSource(str);
                String extractMetadata = mediaMetadataRetriever.extractMetadata(24);
                String extractMetadata2 = mediaMetadataRetriever.extractMetadata(18);
                String extractMetadata3 = mediaMetadataRetriever.extractMetadata(19);
                iArr = new int[3];
                if (extractMetadata == null) {
                    extractMetadata = "0";
                }
                iArr[RESULT_FIAL] = Integer.valueOf(extractMetadata).intValue();
                iArr[RESULT_SUCCESS] = Integer.valueOf(extractMetadata2).intValue();
                iArr[2] = Integer.valueOf(extractMetadata3).intValue();
                try {
                    mediaMetadataRetriever.release();
                } catch (IOException e) {
                    c.r("Q.qqstory:Utils", "retriever. IOException release error" + e);
                } catch (RuntimeException e2) {
                    c.r("Q.qqstory:Utils", "retriever.release error" + e2);
                }
            } catch (Exception e3) {
                c.s("Q.qqstory:Utils", "exception", e3);
                try {
                    mediaMetadataRetriever.release();
                } catch (IOException e4) {
                    c.r("Q.qqstory:Utils", "retriever. IOException release error" + e4);
                } catch (RuntimeException e5) {
                    c.r("Q.qqstory:Utils", "retriever.release error" + e5);
                }
                iArr = null;
            }
            if (iArr == null) {
                fFmpegExecuteResponseCallback.onFailure(MESSAGE_INPUT_NULL);
                fFmpegExecuteResponseCallback.onFinish(false);
                c.g(TAG, "compressVideo input path is null");
                return;
            }
            int i7 = iArr[RESULT_SUCCESS];
            int i8 = iArr[2];
            if (i7 > 960 || i8 > 960) {
                if (i8 < i7) {
                    i4 = (i7 * 540) / i8;
                    i5 = 540;
                } else {
                    i4 = i7;
                    i5 = i8;
                }
                if (i7 <= i8) {
                    i5 = (i8 * 540) / i7;
                    i6 = 540;
                } else {
                    i6 = i4;
                }
                if (i5 % 2 != 0) {
                    i5 += RESULT_SUCCESS;
                }
                if (i6 % 2 != 0) {
                    i6 += RESULT_SUCCESS;
                }
            } else {
                i6 = -1;
                i5 = -1;
            }
            long i9 = d.i(str);
            if (i9 != -1 && i9 < 2202009.6d) {
                z2 = RESULT_FIAL;
            }
            ArrayList arrayList = new ArrayList();
            arrayList.add("-y");
            if (i2 != 0) {
                arrayList.add("-ss");
                arrayList.add(String.valueOf(i / 1000) + "." + String.valueOf(i % 1000));
                arrayList.add("-accurate_seek");
            }
            arrayList.add("-i");
            arrayList.add(new File(str).getCanonicalPath());
            if (i2 != 0) {
                arrayList.add("-t");
                arrayList.add(String.valueOf(i2 / 1000) + "." + String.valueOf(i2 % 1000));
            }
            if (i6 > 0 && i5 > 0) {
                arrayList.add("-vf");
                arrayList.add("scale=" + i6 + ":" + i5);
            }
            if (z) {
                arrayList.add("-an");
            } else {
                arrayList.add("-acodec");
                arrayList.add("aac");
            }
            arrayList.add("-vcodec");
            if (VideoEnvironment.isAvcodecNewVersion()) {
                arrayList.add("libo264rt");
            } else {
                arrayList.add("libx264");
            }
            if (z2) {
                arrayList.add("-b:v");
                arrayList.add("" + i3);
            }
            arrayList.add("-bufsize");
            arrayList.add("800k");
            arrayList.add("-r");
            arrayList.add("25");
            arrayList.add("-metadata");
            arrayList.add("title=" + System.currentTimeMillis());
            arrayList.add("-movflags");
            arrayList.add("faststart");
            arrayList.add(new File(str2).getCanonicalPath());
            execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
        } catch (Throwable th) {
            try {
                mediaMetadataRetriever.release();
                throw th;
            } catch (IOException e6) {
                c.r("Q.qqstory:Utils", "retriever. IOException release error" + e6);
                throw th;
            } catch (RuntimeException e7) {
                c.r("Q.qqstory:Utils", "retriever.release error" + e7);
                throw th;
            }
        }
    }

    public void concatDifferentCodingMedia(List<String> list, String str, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 5)) {
            iPatchRedirector.redirect((short) 5, this, list, str, fFmpegExecuteResponseCallback);
            return;
        }
        c.r(TAG, "concatDifferentCodingMedia arguments: \n inMedias:" + list + "\n outMedia:" + str);
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        StringBuilder sb = new StringBuilder();
        for (int i = RESULT_FIAL; i < list.size(); i += RESULT_SUCCESS) {
            arrayList.add("-i");
            arrayList.add(new File(list.get(i)).getCanonicalPath());
            sb.append("[" + i + ":0]");
            sb.append(" ");
            sb.append("[" + i + ":1]");
            sb.append(" ");
        }
        sb.append("concat=n=" + list.size() + ":v=1:a=1 [v] [a]");
        arrayList.add("-filter_complex");
        arrayList.add(sb.toString());
        arrayList.add("-map");
        arrayList.add("[v]");
        arrayList.add("-map");
        arrayList.add("[a]");
        arrayList.add(new File(str).getCanonicalPath());
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    public void concatMedia(List<String> list, String str, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback, boolean z) throws FFmpegCommandAlreadyRunningException, IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 4)) {
            iPatchRedirector.redirect((short) 4, new Object[]{this, list, str, fFmpegExecuteResponseCallback, Boolean.valueOf(z)});
            return;
        }
        c.r(TAG, "concatMedia arguments: \n inMedias:" + list + "\n outMedia:" + str);
        new File(str);
        File file = new File(a.e + "temp.txt");
        if (file.exists()) {
            file.delete();
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        BufferedWriter bufferedWriter = null;
        try {
            BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(file, true));
            for (int i = RESULT_FIAL; i < list.size(); i += RESULT_SUCCESS) {
                try {
                    bufferedWriter2.write("file '" + new File(list.get(i)).getCanonicalPath() + "'");
                    bufferedWriter2.newLine();
                } catch (Throwable th) {
                    th = th;
                    bufferedWriter = bufferedWriter2;
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    throw th;
                }
            }
            bufferedWriter2.flush();
            bufferedWriter2.close();
            ArrayList arrayList = new ArrayList();
            arrayList.add("-y");
            arrayList.add("-f");
            arrayList.add("concat");
            arrayList.add("-i");
            arrayList.add(file.getCanonicalPath());
            if (z) {
                arrayList.add("-c:v");
                if (VideoEnvironment.isAvcodecNewVersion()) {
                    arrayList.add("libo264rt");
                } else {
                    arrayList.add("libx264");
                }
                arrayList.add("-bsf:a");
                arrayList.add("aac_adtstoasc");
            } else {
                arrayList.add("-c");
                arrayList.add("copy");
            }
            arrayList.add(new File(str).getCanonicalPath());
            execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), new FFmpeg$2(this, fFmpegExecuteResponseCallback, file));
        } catch (Throwable th2) {
            th = th2;
        }
    }

    public void concatMediaByTs(List<String> list, String str, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 6)) {
            iPatchRedirector.redirect((short) 6, this, list, str, fFmpegExecuteResponseCallback);
            return;
        }
        c.r(TAG, "concatMediaByTs arguments: \n inMedias:" + list + "\n outMedia:" + str);
        ArrayList<FFmpegCommandUnit> arrayList = new ArrayList<>();
        for (int i = RESULT_FIAL; i < list.size(); i += RESULT_SUCCESS) {
            String str2 = list.get(i);
            String str3 = a.e + new File(str2).getName().split("\\.")[RESULT_FIAL] + ".ts";
            File file = new File(str3);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FFmpegCommandUnit fFmpegCommandUnit = new FFmpegCommandUnit();
            fFmpegCommandUnit.cmdType = 5;
            fFmpegCommandUnit.arguments = new FFmpeg$3(this, str2, str3);
            fFmpegCommandUnit.callback = new FFmpeg$4(this, fFmpegExecuteResponseCallback);
            arrayList.add(fFmpegCommandUnit);
        }
        FFmpegCommandUnit fFmpegCommandUnit2 = new FFmpegCommandUnit();
        ArrayList<String> arrayList2 = this.tsFileList;
        fFmpegCommandUnit2.cmdType = 2;
        fFmpegCommandUnit2.arguments = new FFmpeg$5(this, arrayList2, str);
        fFmpegCommandUnit2.callback = fFmpegExecuteResponseCallback;
        arrayList.add(fFmpegCommandUnit2);
        cmdFFmpegQueue(arrayList);
    }

    public void concatTsOutput(List<String> list, String str, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 8)) {
            iPatchRedirector.redirect((short) 8, this, list, str, fFmpegExecuteResponseCallback);
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\"concat:");
        for (int i = RESULT_FIAL; i < list.size(); i += RESULT_SUCCESS) {
            stringBuffer.append(list.get(i));
            if (i != list.size() - 1) {
                stringBuffer.append("|");
            }
        }
        stringBuffer.append("\"");
        this.tsFileList = new ArrayList<>();
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-i");
        arrayList.add(new String(stringBuffer));
        arrayList.add("-c");
        arrayList.add("copy");
        arrayList.add("-bsf:a");
        arrayList.add("aac_adtstoasc");
        arrayList.add(str);
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    public void convertMp4ToMp3(String str, String str2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws IOException, FFmpegCommandAlreadyRunningException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 16)) {
            iPatchRedirector.redirect((short) 16, this, str, str2, fFmpegExecuteResponseCallback);
            return;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-i");
        arrayList.add(new File(str).getCanonicalPath());
        arrayList.add("-vn");
        arrayList.add("-c:a");
        arrayList.add("copy");
        arrayList.add(new File(str2).getCanonicalPath());
        String[] strArr = (String[]) arrayList.toArray(new String[RESULT_FIAL]);
        c.t(TAG, "extractAudioFromMp4 args: %s", new Object[]{Arrays.toString(strArr)});
        execute(strArr, fFmpegExecuteResponseCallback);
    }

    public void convertPicToVideo(String str, String str2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws IOException, FFmpegCommandAlreadyRunningException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 13)) {
            iPatchRedirector.redirect((short) 13, this, str, str2, fFmpegExecuteResponseCallback);
            return;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-analyzeduration");
        arrayList.add("2147483647");
        arrayList.add("-probesize");
        arrayList.add("2147483647");
        arrayList.add("-framerate");
        arrayList.add("1/3");
        arrayList.add("-i");
        arrayList.add(new File(str).getCanonicalPath());
        if (VideoEnvironment.isAvcodecNewVersion()) {
            arrayList.add("-vf");
            arrayList.add("scale='min(1920,iw)':min'(1920,ih)':force_original_aspect_ratio=decrease");
            arrayList.add("-c:v");
            arrayList.add("libo264rt");
        } else {
            arrayList.add("-vcodec");
            arrayList.add("libx264");
            arrayList.add("-preset");
            arrayList.add("veryslow");
        }
        arrayList.add("-bufsize");
        arrayList.add("800k");
        arrayList.add("-metadata");
        arrayList.add("title=" + System.currentTimeMillis());
        arrayList.add("-movflags");
        arrayList.add("faststart");
        arrayList.add("-r");
        arrayList.add("25");
        arrayList.add(new File(str2).getCanonicalPath());
        String[] strArr = (String[]) arrayList.toArray(new String[RESULT_FIAL]);
        c.t(TAG, "convertPicToVideo args: %s", new Object[]{Arrays.toString(strArr)});
        execute(strArr, fFmpegExecuteResponseCallback);
    }

    public void convertPicToVideoWithTime(String str, String str2, String str3, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws IOException, FFmpegCommandAlreadyRunningException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 14)) {
            iPatchRedirector.redirect((short) 14, new Object[]{this, str, str2, str3, fFmpegExecuteResponseCallback});
            return;
        }
        boolean isAvcodecNewVersion = VideoEnvironment.isAvcodecNewVersion();
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-analyzeduration");
        arrayList.add("2147483647");
        arrayList.add("-probesize");
        arrayList.add("2147483647");
        arrayList.add("-framerate");
        arrayList.add("1/3");
        arrayList.add("-loop");
        arrayList.add("1");
        arrayList.add("-i");
        arrayList.add(new File(str).getCanonicalPath());
        if (isAvcodecNewVersion) {
            arrayList.add("-vf");
            arrayList.add("scale='min(1920,iw)':min'(1920,ih)':force_original_aspect_ratio=decrease");
            arrayList.add("-c:v");
            arrayList.add("libo264rt");
        } else {
            arrayList.add("-vcodec");
            arrayList.add("libx264");
        }
        arrayList.add("-pix_fmt");
        arrayList.add("yuv420p");
        arrayList.add("-t");
        arrayList.add(str3);
        if (!isAvcodecNewVersion) {
            arrayList.add("-preset");
            arrayList.add("veryslow");
        }
        arrayList.add("-bufsize");
        arrayList.add("800k");
        arrayList.add("-metadata");
        arrayList.add("title=" + System.currentTimeMillis());
        arrayList.add("-movflags");
        arrayList.add("faststart");
        arrayList.add("-r");
        arrayList.add("25");
        arrayList.add(new File(str2).getCanonicalPath());
        String[] strArr = (String[]) arrayList.toArray(new String[RESULT_FIAL]);
        c.t(TAG, "convertPicToVideoWithTime args: %s", new Object[]{Arrays.toString(strArr)});
        execute(strArr, fFmpegExecuteResponseCallback);
    }

    public void emptyFFmengCmd(String str, String str2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 15)) {
            iPatchRedirector.redirect((short) 15, this, str, str2, fFmpegExecuteResponseCallback);
            return;
        }
        c.r(TAG, "fake ffmeng command. arguments: \n inMedia:" + str + "\n outMedia:" + str2);
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            fFmpegExecuteResponseCallback.onFailure(MESSAGE_INPUT_NULL);
            c.g(TAG, "fake ffmeng command. input path is null");
            fFmpegExecuteResponseCallback.onFinish(false);
            return;
        }
        boolean l = d.l(this.context, str, str2);
        if (l) {
            fFmpegExecuteResponseCallback.onSuccess(MESSAGE_COPY_CMD_SUCCESS);
        } else {
            fFmpegExecuteResponseCallback.onFailure(MESSAGE_COPY_CMD_FAIL);
        }
        fFmpegExecuteResponseCallback.onFinish(l);
        c.r(TAG, "copy video to album result is " + l);
    }

    public void execute(String[] strArr, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 3)) {
            iPatchRedirector.redirect((short) 3, this, strArr, fFmpegExecuteResponseCallback);
            return;
        }
        FFmpegExecuteAsyncTask fFmpegExecuteAsyncTask = this.ffmpegExecuteAsyncTask;
        if (fFmpegExecuteAsyncTask != null && !fFmpegExecuteAsyncTask.isProcessCompleted()) {
            c.g(TAG, "FFmpeg command is already running");
            throw new FFmpegCommandAlreadyRunningException("FFmpeg command is already running, you are only allowed to run single command at a time");
        }
        if (strArr.length == 0) {
            throw new IllegalArgumentException("shell command cannot be empty");
        }
        String str = strArr[strArr.length - 1];
        this.mCurrentCommandUnit = new FFmpegCommandUnit(strArr, str, fFmpegExecuteResponseCallback);
        int lastIndexOf = str.lastIndexOf("/");
        if (lastIndexOf > -1 && lastIndexOf < str.length() - 1) {
            File file = new File(str.substring(RESULT_FIAL, lastIndexOf + RESULT_SUCCESS));
            if (!file.exists()) {
                file.mkdir();
            }
        }
        FFmpeg$1 r11 = new FFmpeg$1(this, fFmpegExecuteResponseCallback);
        String[] strArr2 = (String[]) Util.concatenate((String[]) Util.concatenate(new String[]{FFmpegFileUtils.getFFmpeg(this.context)}, strArr), new String[]{FFmpegFileUtils.getAVCodecSoFilePath(this.context)});
        FFmpegExecuteAsyncTask fFmpegExecuteAsyncTask2 = new FFmpegExecuteAsyncTask(this.context, strArr2, this.timeout, this.mIsWorkThreadCallback, r11);
        this.ffmpegExecuteAsyncTask = fFmpegExecuteAsyncTask2;
        fFmpegExecuteAsyncTask2.execute(new Void[RESULT_FIAL]);
        c.r(TAG, "command execute: " + TextUtils.join(" ", strArr2));
    }

    public void hflip(String str, String str2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 21)) {
            iPatchRedirector.redirect((short) 21, this, str, str2, fFmpegExecuteResponseCallback);
            return;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-i");
        arrayList.add(new File(str).getCanonicalPath());
        arrayList.add("-vf");
        arrayList.add("transpose=1");
        if (VideoEnvironment.isAvcodecNewVersion()) {
            arrayList.add("-c:v");
            arrayList.add("libo264rt");
        }
        arrayList.add("-metadata:s:v");
        arrayList.add("rotate=0");
        arrayList.add(str2);
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    public void insertFFmpegQueue(ArrayList<FFmpegCommandUnit> arrayList) throws FFmpegCommandAlreadyRunningException, IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 27)) {
            iPatchRedirector.redirect((short) 27, this, arrayList);
            return;
        }
        killRunningProcesses(false);
        c.r(TAG, "[insertFFmpegQueue][old] " + TextUtils.join(" ", arrayList.toArray()));
        FFmpegCommandUnit fFmpegCommandUnit = this.mCurrentCommandUnit;
        if (fFmpegCommandUnit != null) {
            this.mCmdQueue.add(RESULT_FIAL, fFmpegCommandUnit);
        }
        this.mCmdQueue.addAll(RESULT_FIAL, arrayList);
        cmdFFmpegQueue(this.mCmdQueue);
        c.r(TAG, "[insertFFmpegQueue][new] " + TextUtils.join(" ", arrayList.toArray()));
    }

    public boolean isFFmpegCommandRunning() {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 28)) {
            return ((Boolean) iPatchRedirector.redirect((short) 28, this)).booleanValue();
        }
        FFmpegExecuteAsyncTask fFmpegExecuteAsyncTask = this.ffmpegExecuteAsyncTask;
        return (fFmpegExecuteAsyncTask == null || fFmpegExecuteAsyncTask.isProcessCompleted()) ? false : true;
    }

    public void killRunningProcesses(boolean z) {
        FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback;
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 29)) {
            iPatchRedirector.redirect((short) 29, this, z);
            return;
        }
        FFmpegExecuteAsyncTask fFmpegExecuteAsyncTask = this.ffmpegExecuteAsyncTask;
        if (fFmpegExecuteAsyncTask == null || fFmpegExecuteAsyncTask.isProcessCompleted()) {
            return;
        }
        Util.killFFmpegProcess(this.ffmpegExecuteAsyncTask);
        this.ffmpegExecuteAsyncTask = null;
        this.mLastTaskResult = RESULT_NOT_DONE;
        FFmpegCommandUnit fFmpegCommandUnit = this.mCurrentCommandUnit;
        if (fFmpegCommandUnit != null) {
            if (fFmpegCommandUnit.output != null) {
                File file = new File(this.mCurrentCommandUnit.output);
                if (file.exists()) {
                    file.delete();
                }
            }
            if (z && (fFmpegExecuteResponseCallback = this.mCurrentCommandUnit.callback) != null) {
                fFmpegExecuteResponseCallback.onFailure("FFmpeg任务被强制Kill掉");
                this.mCurrentCommandUnit.callback.onFinish(false);
                c.r(TAG, "FFmpeg任务被强制Kill掉");
            }
        }
        c.c(TAG, "KillFFmpeg!");
    }

    public void killRunningProcessesForShortVideo(boolean z) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 30)) {
            iPatchRedirector.redirect((short) 30, this, z);
            return;
        }
        FFmpegExecuteAsyncTask fFmpegExecuteAsyncTask = this.ffmpegExecuteAsyncTask;
        if (fFmpegExecuteAsyncTask == null || fFmpegExecuteAsyncTask.isProcessCompleted()) {
            return;
        }
        Util.killFFmpegProcess(this.ffmpegExecuteAsyncTask);
        this.ffmpegExecuteAsyncTask = null;
        this.mLastTaskResult = RESULT_NOT_DONE;
        FFmpegCommandUnit fFmpegCommandUnit = this.mCurrentCommandUnit;
        if (fFmpegCommandUnit != null) {
            if (fFmpegCommandUnit.output != null) {
                File file = new File(this.mCurrentCommandUnit.output);
                if (file.exists()) {
                    file.delete();
                }
            }
            FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback = this.mCurrentCommandUnit.callback;
            if (fFmpegExecuteResponseCallback != null) {
                fFmpegExecuteResponseCallback.onFailure("FFmpeg任务被强制Kill掉");
                this.mCurrentCommandUnit.callback.onFinish(false);
                c.r(TAG, "FFmpeg任务被强制Kill掉");
            }
        }
        c.c(TAG, "KillFFmpeg!");
    }

    public void mp4Tots(String str, String str2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 7)) {
            iPatchRedirector.redirect((short) 7, this, str, str2, fFmpegExecuteResponseCallback);
            return;
        }
        c.r(TAG, "mp4Tots arguments: \n input:" + str + "\n outputPath:" + str2);
        this.tsFileList.add(str2);
        if (d.g(str2)) {
            fFmpegExecuteResponseCallback.onSuccess(MESSAGE_TS_DONE);
            fFmpegExecuteResponseCallback.onFinish(true);
            c.r(TAG, MESSAGE_TS_DONE);
            return;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-i");
        arrayList.add(new File(str).getCanonicalPath());
        arrayList.add("-c");
        arrayList.add("copy");
        arrayList.add("-bsf:v");
        arrayList.add("h264_mp4toannexb");
        arrayList.add("-f");
        arrayList.add("mpegts");
        arrayList.add(str2);
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    public void setCurrentTaskUni(String str) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 31)) {
            this.mCurrentTaskUni = str;
        } else {
            iPatchRedirector.redirect((short) 31, this, str);
        }
    }

    public void setFFMpegCanExe() {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 2)) {
            Util.setFileExecutable(new File(FFmpegFileUtils.getFFmpeg(this.context)));
        } else {
            iPatchRedirector.redirect((short) 2, this);
        }
    }

    public void setTimestamp(String str, String str2, int i, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws IOException, FFmpegCommandAlreadyRunningException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 9)) {
            iPatchRedirector.redirect((short) 9, new Object[]{this, str, str2, Integer.valueOf(i), fFmpegExecuteResponseCallback});
            return;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-i");
        arrayList.add(new File(str).getCanonicalPath());
        arrayList.add("-metadata");
        arrayList.add("title=" + System.currentTimeMillis());
        StringBuilder sb = new StringBuilder("dev=");
        float f = ag.i;
        sb.append(DeviceInfoMonitor.getModel());
        sb.append("/os=");
        sb.append(Build.VERSION.RELEASE);
        sb.append("/appVer=");
        sb.append(AppSetting.d());
        arrayList.add("-metadata");
        arrayList.add("comment=" + sb.toString());
        if (i != -1) {
            arrayList.add("-metadata:s:v:0");
            arrayList.add("rotate=" + i);
        }
        arrayList.add("-movflags");
        arrayList.add("faststart");
        arrayList.add("-codec");
        arrayList.add("copy");
        arrayList.add(new File(str2).getCanonicalPath());
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    public void transcodeM4a(String str, String str2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws IOException, FFmpegCommandAlreadyRunningException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 17)) {
            iPatchRedirector.redirect((short) 17, this, str, str2, fFmpegExecuteResponseCallback);
            return;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add("-i");
        arrayList.add(str);
        arrayList.add("-ab");
        arrayList.add("96k");
        arrayList.add("-ar");
        arrayList.add("44100");
        arrayList.add("-ac");
        arrayList.add("1");
        arrayList.add("-vn");
        arrayList.add("-acodec");
        arrayList.add("aac");
        arrayList.add(str2);
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    public void watermark(String str, String str2, String str3, boolean z, int i, int i2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        boolean copyFile;
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 18)) {
            iPatchRedirector.redirect((short) 18, new Object[]{this, str, str2, str3, Boolean.valueOf(z), Integer.valueOf(i), Integer.valueOf(i2), fFmpegExecuteResponseCallback});
            return;
        }
        c.r(TAG, "watermark arguments: \n inImage" + str + "\n inMedia:" + str2 + "\n outMedia:" + str3 + "\n videoWidth:" + i + "\n videoHeight:" + i2);
        if (str2 == null || str3 == null) {
            fFmpegExecuteResponseCallback.onFailure(MESSAGE_INPUT_NULL);
            fFmpegExecuteResponseCallback.onFinish(false);
            c.r(TAG, "watermark input path is null");
            return;
        }
        if (str == null) {
            if (z) {
                copyFile = d.l(this.context, str2, str3);
            } else {
                copyFile = FileUtils.copyFile(str2, str3);
            }
            if (copyFile) {
                fFmpegExecuteResponseCallback.onSuccess(MESSAGE_COPY_CMD_SUCCESS);
            } else {
                fFmpegExecuteResponseCallback.onFailure(MESSAGE_COPY_CMD_FAIL);
            }
            fFmpegExecuteResponseCallback.onFinish(copyFile);
            c.r(TAG, "watermark inImage == null, copy to" + str3 + " result is " + copyFile);
            return;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-i");
        arrayList.add(new File(str2).getCanonicalPath());
        arrayList.add("-vf");
        arrayList.add("movie=" + str + " [watermark]; [watermark]scale=" + i + ":" + i2 + " [watermark]; [in][watermark] overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2 [out] ");
        if (VideoEnvironment.isAvcodecNewVersion()) {
            arrayList.add("-c:v");
            arrayList.add("libo264rt");
        }
        arrayList.add("-max_muxing_queue_size");
        arrayList.add("9999");
        arrayList.add(new File(str3).getCanonicalPath());
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    public void watermarkWithRotation(String str, String str2, String str3, int i, int i2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 20)) {
            iPatchRedirector.redirect((short) 20, new Object[]{this, str, str2, str3, Integer.valueOf(i), Integer.valueOf(i2), fFmpegExecuteResponseCallback});
            return;
        }
        c.r(TAG, "watermarkWithRotation arguments: \n inImage" + str + "\n inMedia:" + str2 + "\n outMedia:" + str3 + "\n videoWidth:" + i + "\n videoHeight:" + i2);
        if (str2 == null || str3 == null) {
            fFmpegExecuteResponseCallback.onFailure(MESSAGE_INPUT_NULL);
            c.r(TAG, "watermarkWithRotation input path is null");
            fFmpegExecuteResponseCallback.onFinish(false);
            return;
        }
        if (str == null) {
            boolean l = d.l(this.context, str2, str3);
            if (l) {
                fFmpegExecuteResponseCallback.onSuccess(MESSAGE_COPY_CMD_SUCCESS);
            } else {
                fFmpegExecuteResponseCallback.onFailure(MESSAGE_COPY_CMD_FAIL);
            }
            fFmpegExecuteResponseCallback.onFinish(l);
            c.r(TAG, "watermarkWithRotation inImage == null, copy to DCIM result is " + l);
            return;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-i");
        arrayList.add(new File(str2).getCanonicalPath());
        arrayList.add("-vf");
        arrayList.add("[in]transpose=1 [in]; movie=" + str + " [watermark]; [watermark]scale=" + i + ":" + i2 + " [watermark]; [in][watermark] overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2 [out]");
        if (VideoEnvironment.isAvcodecNewVersion()) {
            arrayList.add("-c:v");
            arrayList.add("libo264rt");
        }
        arrayList.add("-metadata:s:v");
        arrayList.add("rotate=0");
        arrayList.add(new File(str3).getCanonicalPath());
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
    }

    public static FFmpeg getInstance(Context context, boolean z) {
        if (instance == null) {
            synchronized (FFmpeg.class) {
                if (instance == null) {
                    instance = new FFmpeg(context);
                }
            }
        }
        instance.mIsWorkThreadCallback = z;
        return instance;
    }

    public Clip combineAudioAndVideo(String str, String str2, boolean z, Clip clip, int i, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 24)) {
            return (Clip) iPatchRedirector.redirect((short) 24, new Object[]{this, str, str2, Boolean.valueOf(z), clip, Integer.valueOf(i), fFmpegExecuteResponseCallback});
        }
        c.r(TAG, "combineAudioAndVideo arguments: \n inVideo" + str + "\n inAudio:" + str2 + "\n outMedia:" + clip);
        if (!d.g(str)) {
            c.g(TAG, "clipAudio but inVideo file is not exist");
            fFmpegExecuteResponseCallback.onFailure(String.valueOf(941002));
            fFmpegExecuteResponseCallback.onFinish(false);
            return clip;
        }
        if (!d.g(str2)) {
            c.g(TAG, "clipAudio but inAudio file is not exist");
            fFmpegExecuteResponseCallback.onFailure(String.valueOf(941002));
            fFmpegExecuteResponseCallback.onFinish(false);
            return clip;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add("-y");
        arrayList.add("-i");
        arrayList.add(new File(str).getCanonicalPath());
        if (z) {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(str2);
            String extractMetadata = mediaMetadataRetriever.extractMetadata(9);
            mediaMetadataRetriever.release();
            long j = i;
            try {
                j = Long.parseLong(extractMetadata);
            } catch (Exception unused) {
            }
            int ceil = (int) Math.ceil(i / ((float) j));
            if (ceil > RESULT_SUCCESS) {
                String canonicalPath = new File(str2).getCanonicalPath();
                String str3 = new File(str2).getParent() + "/catAudioFile.txt";
                StringBuilder sb = new StringBuilder();
                for (int i2 = RESULT_FIAL; i2 < ceil; i2 += RESULT_SUCCESS) {
                    if (i2 > 0) {
                        sb.append("\n");
                    }
                    sb.append("file '");
                    sb.append(canonicalPath);
                    sb.append("'");
                }
                FileUtils.writeFile(str3, sb.toString());
                arrayList.add("-f");
                arrayList.add("concat");
                arrayList.add("-safe");
                arrayList.add("0");
                arrayList.add("-i");
                arrayList.add(str3);
            } else {
                arrayList.add("-i");
                arrayList.add(new File(str2).getCanonicalPath());
            }
        } else {
            arrayList.add("-i");
            arrayList.add(new File(str2).getCanonicalPath());
        }
        arrayList.add("-map_chapters");
        arrayList.add("-1");
        arrayList.add("-strict");
        arrayList.add("-2");
        arrayList.add("-vcodec");
        String str4 = clip.videoCodec;
        if (str4 != null) {
            arrayList.add(str4);
        } else {
            arrayList.add("copy");
        }
        arrayList.add("-acodec");
        String str5 = clip.audioCodec;
        if (str5 != null) {
            arrayList.add(str5);
        } else {
            arrayList.add("aac");
        }
        FFmpegUtils.getAuidoType(str2);
        arrayList.add("-bsf:a");
        arrayList.add("aac_adtstoasc");
        if (clip.videoBitrate != -1) {
            arrayList.add("-b:v");
            arrayList.add(clip.videoBitrate + "k");
        }
        if (clip.videoFps != null) {
            arrayList.add("-r");
            arrayList.add(clip.videoFps);
        }
        if (clip.audioBitrate != -1) {
            arrayList.add("-b:a");
            arrayList.add(clip.audioBitrate + "k");
        }
        if (clip.width > 0) {
            arrayList.add("-s");
            arrayList.add(clip.width + "x" + clip.height);
        }
        if (clip.format != null) {
            arrayList.add("-f");
            arrayList.add(clip.format);
        }
        File file = new File(clip.path);
        if (z) {
            arrayList.add("-shortest");
        }
        arrayList.add(file.getCanonicalPath());
        execute((String[]) arrayList.toArray(new String[RESULT_FIAL]), fFmpegExecuteResponseCallback);
        return clip;
    }

    public void watermark(String str, String str2, String str3, int i, int i2, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) throws FFmpegCommandAlreadyRunningException, IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 19)) {
            watermark(str, str2, str3, true, i, i2, fFmpegExecuteResponseCallback);
        } else {
            iPatchRedirector.redirect((short) 19, new Object[]{this, str, str2, str3, Integer.valueOf(i), Integer.valueOf(i2), fFmpegExecuteResponseCallback});
        }
    }
}
