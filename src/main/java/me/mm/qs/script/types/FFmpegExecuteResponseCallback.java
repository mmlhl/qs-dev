package me.mm.qs.script.types;

/**
 * FFmpeg 执行回调接口
 * 从 QQ 反编译，用于编译时类型检查
 */
public interface FFmpegExecuteResponseCallback {
    void onFailure(String str);
    void onFinish(boolean z);
    void onProgress(String str);
    void onStart();
    void onSuccess(String str);
}
