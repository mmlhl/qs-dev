//
// Decompiled by Jadx - 906ms
//
package com.tencent.mobileqq.videocodec.ffmpeg;

public interface FFmpegExecuteResponseCallback {
    void onFailure(String str);

    void onFinish(boolean z);

    void onProgress(String str);

    void onStart();

    void onSuccess(String str);
}
