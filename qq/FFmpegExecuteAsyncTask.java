//
// Decompiled by Jadx - 639ms
//
package com.tencent.mobileqq.videocodec.ffmpeg;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.text.TextUtils;
import com.tencent.mobileqq.app.ThreadExcutor;
import com.tencent.mobileqq.app.ThreadManagerV2;
import com.tencent.mobileqq.qfix.redirect.IPatchRedirector;
import com.tencent.qphone.base.util.QLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import mq.c;

class FFmpegExecuteAsyncTask extends AsyncTask<Void, String, CommandResult> {
    static IPatchRedirector $redirector_ = null;
    public static final String TAG = "Q.qqstory.ffmpeg.FFmpegExecuteAsyncTask";
    public final String[] cmd;
    public final FFmpegExecuteResponseCallback ffmpegExecuteResponseHandler;
    public boolean isFFmpegExecutable;
    public boolean isWorkThread;
    public Context mContext;
    public Boolean mIsDebug;
    public Process mProcess;
    public StringBuilder output;
    public final ShellCommand shellCommand;
    public long startTime;
    public final long timeout;

    FFmpegExecuteAsyncTask(Context context, String[] strArr, long j, boolean z, FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 1)) {
            iPatchRedirector.redirect((short) 1, new Object[]{this, context, strArr, Long.valueOf(j), Boolean.valueOf(z), fFmpegExecuteResponseCallback});
            return;
        }
        this.mIsDebug = Boolean.FALSE;
        this.isFFmpegExecutable = false;
        this.mContext = context;
        this.cmd = strArr;
        this.timeout = j;
        this.isWorkThread = z;
        this.ffmpegExecuteResponseHandler = fFmpegExecuteResponseCallback;
        this.shellCommand = new ShellCommand();
        this.output = new StringBuilder();
    }

    /* JADX WARN: Code restructure failed: missing block: B:36:0x0000, code lost:
    
        continue;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void checkAndUpdateProcess() throws TimeoutException {
        FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback;
        while (!Util.isProcessCompleted(this.mProcess)) {
            if (this.timeout != Long.MAX_VALUE && SystemClock.uptimeMillis() > this.startTime + this.timeout) {
                QLog.i("FFmpegCmd", 1, "timeout");
                throw new TimeoutException("FFmpeg timed out");
            }
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.mProcess.getInputStream()));
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine != null) {
                        if (isCancelled()) {
                            return;
                        }
                        StringBuilder sb = this.output;
                        sb.append(readLine);
                        sb.append("\n");
                        if (!this.isWorkThread) {
                            publishProgress(readLine);
                        } else if (!TextUtils.isEmpty(readLine) && (fFmpegExecuteResponseCallback = this.ffmpegExecuteResponseHandler) != null) {
                            fFmpegExecuteResponseCallback.onProgress(readLine);
                        }
                    }
                }
            } catch (IOException e) {
                QLog.i("FFmpegCmd", 1, "IOException");
                e.printStackTrace();
            }
        }
    }

    private CommandResult getFailCommandResult() {
        CommandResult dummyFailureResponse = CommandResult.getDummyFailureResponse();
        if (this.isWorkThread) {
            handleResult(dummyFailureResponse);
            dummyFailureResponse.isDone = true;
        }
        return dummyFailureResponse;
    }

    private CommandResult getOutputCommandResult(Process process) {
        CommandResult outputFromProcess = CommandResult.getOutputFromProcess(process);
        if (this.isWorkThread) {
            handleResult(outputFromProcess);
            outputFromProcess.isDone = true;
        }
        return outputFromProcess;
    }

    private void handleResult(CommandResult commandResult) {
        if (this.ffmpegExecuteResponseHandler != null) {
            this.output.append(commandResult.output);
            if (commandResult.success) {
                this.ffmpegExecuteResponseHandler.onSuccess(this.output.toString());
            } else {
                this.ffmpegExecuteResponseHandler.onFailure(this.output.toString());
            }
            this.ffmpegExecuteResponseHandler.onFinish(commandResult.success);
            if (QLog.isColorLevel()) {
                QLog.d(TAG, 2, "ThreadName:" + Thread.currentThread().getName());
            }
        }
    }

    boolean isProcessCompleted() {
        return Util.isProcessCompleted(this.mProcess);
    }

    @Override
    protected void onPreExecute() {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 2)) {
            iPatchRedirector.redirect((short) 2, this);
            return;
        }
        this.startTime = SystemClock.uptimeMillis();
        if (this.ffmpegExecuteResponseHandler != null) {
            if (this.isWorkThread) {
                ThreadManagerV2.post(new FFmpegExecuteAsyncTask$1(this), 5, (ThreadExcutor.IThreadListener) null, true);
                return;
            }
            if (QLog.isColorLevel()) {
                QLog.d(TAG, 2, "ThreadName:" + Thread.currentThread().getName());
            }
            this.ffmpegExecuteResponseHandler.onStart();
        }
    }

    @Override
    public CommandResult doInBackground(Void... voidArr) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 3)) {
            return (CommandResult) iPatchRedirector.redirect((short) 3, this, voidArr);
        }
        try {
            try {
                if (!this.isFFmpegExecutable) {
                    this.isFFmpegExecutable = Util.setFileExecutable(new File(FFmpegFileUtils.getFFmpeg(this.mContext)));
                }
                c.r(TAG, "[story_ffmpeg]execute start cmd=" + Arrays.toString(this.cmd));
                Process run = this.shellCommand.run(this.cmd);
                this.mProcess = run;
                if (run == null) {
                    CommandResult failCommandResult = getFailCommandResult();
                    Util.destroyProcess(this.mProcess);
                    c.r(TAG, "[story_ffmpeg]execute end cmd=" + Arrays.toString(this.cmd));
                    return failCommandResult;
                }
                if (this.mIsDebug.booleanValue()) {
                    StringBuilder sb = new StringBuilder();
                    for (String str : this.cmd) {
                        sb.append(str);
                        sb.append(' ');
                    }
                    publishProgress(sb.toString());
                }
                checkAndUpdateProcess();
                CommandResult outputCommandResult = getOutputCommandResult(run);
                Util.destroyProcess(this.mProcess);
                c.r(TAG, "[story_ffmpeg]execute end cmd=" + Arrays.toString(this.cmd));
                return outputCommandResult;
            } catch (TimeoutException e) {
                c.h(TAG, "FFmpeg timed out", e);
                CommandResult commandResult = new CommandResult(false, e.getMessage());
                Util.destroyProcess(this.mProcess);
                c.r(TAG, "[story_ffmpeg]execute end cmd=" + Arrays.toString(this.cmd));
                return commandResult;
            } catch (Exception e2) {
                c.h(TAG, "Error running FFmpeg", e2);
                Util.destroyProcess(this.mProcess);
                c.r(TAG, "[story_ffmpeg]execute end cmd=" + Arrays.toString(this.cmd));
                return getFailCommandResult();
            }
        } catch (Throwable th) {
            Util.destroyProcess(this.mProcess);
            c.r(TAG, "[story_ffmpeg]execute end cmd=" + Arrays.toString(this.cmd));
            throw th;
        }
    }

    @Override
    public void onPostExecute(CommandResult commandResult) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 5)) {
            iPatchRedirector.redirect((short) 5, this, commandResult);
        } else {
            if (commandResult.isDone) {
                return;
            }
            handleResult(commandResult);
        }
    }

    @Override
    public void onProgressUpdate(String... strArr) {
        String str;
        FFmpegExecuteResponseCallback fFmpegExecuteResponseCallback;
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 4)) {
            iPatchRedirector.redirect((short) 4, this, strArr);
        } else {
            if (strArr == null || (str = strArr[0]) == null || (fFmpegExecuteResponseCallback = this.ffmpegExecuteResponseHandler) == null) {
                return;
            }
            fFmpegExecuteResponseCallback.onProgress(str);
        }
    }
}
