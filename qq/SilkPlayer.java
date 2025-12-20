//
// Decompiled by Jadx - 639ms
//
package com.tencent.mobileqq.qqaudio.audioplayer;

import android.app.Application;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.tencent.mobileqq.qfix.redirect.IPatchRedirector;
import com.tencent.mobileqq.qqaudio.QQAudioUtils;
import com.tencent.mobileqq.qqaudio.audioplayer.sonic.a;
import com.tencent.mobileqq.statistics.StatisticCollector;
import com.tencent.qmethod.pandoraex.monitor.DeviceInfoMonitor;
import com.tencent.qphone.base.util.BaseApplication;
import com.tencent.qphone.base.util.QLog;
import com.tencent.util.WeakReferenceHandler;
import java.io.FileInputStream;
import java.util.HashMap;

public final class SilkPlayer implements Handler.Callback, k {
    static IPatchRedirector $redirector_;
    private byte C;
    private float D;
    private j E;
    private Application F;
    private SilkPlayerThread G;
    private l H;
    private WeakReferenceHandler I;
    private long J;
    private int d;
    private String e;
    private int f;
    private int h;
    private int i;
    private int m;

    public SilkPlayer() {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 1)) {
            iPatchRedirector.redirect((short) 1, this);
            return;
        }
        this.d = 3;
        this.f = -1;
        this.h = -1;
        this.i = 0;
        this.m = 0;
        this.C = (byte) -1;
        this.D = 1.0f;
        this.J = -1L;
        this.F = BaseApplication.getContext();
        this.I = new WeakReferenceHandler(Looper.getMainLooper(), this);
    }

    public void a(byte b, int i) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 4)) {
            iPatchRedirector.redirect((short) 4, new Object[]{this, Integer.valueOf(i), Byte.valueOf(b)});
        } else {
            this.f = i;
            this.C = b;
        }
    }

    public String d() {
        IPatchRedirector iPatchRedirector = $redirector_;
        return (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 11)) ? this.e : (String) iPatchRedirector.redirect((short) 11, this);
    }

    public void e(l lVar) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 13)) {
            this.H = lVar;
        } else {
            iPatchRedirector.redirect((short) 13, this, lVar);
        }
    }

    public void f(int i) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 3)) {
            this.d = i;
        } else {
            iPatchRedirector.redirect((short) 3, this, i);
        }
    }

    public int g() {
        IPatchRedirector iPatchRedirector = $redirector_;
        return (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 9)) ? this.h : ((Integer) iPatchRedirector.redirect((short) 9, this)).intValue();
    }

    public int getCurrentPosition() {
        IPatchRedirector iPatchRedirector = $redirector_;
        return (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 8)) ? this.m * 20 : ((Integer) iPatchRedirector.redirect((short) 8, this)).intValue();
    }

    public int getDuration() {
        IPatchRedirector iPatchRedirector = $redirector_;
        return (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 10)) ? this.f : ((Integer) iPatchRedirector.redirect((short) 10, this)).intValue();
    }

    @Override
    public boolean handleMessage(Message message) {
        l lVar;
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 23)) {
            return ((Boolean) iPatchRedirector.redirect((short) 23, this, message)).booleanValue();
        }
        int i = message.what;
        if (i == 1) {
            l lVar2 = this.H;
            if (lVar2 != null) {
                lVar2.a(this, message.arg1, 0, (String) message.obj, this.e);
            }
        } else if (i == 2) {
            l lVar3 = this.H;
            if (lVar3 != null) {
                lVar3.b(this.e);
            }
        } else if (i == 3 && (lVar = this.H) != null) {
            lVar.onProgressChanged(((Integer) message.obj).intValue());
        }
        return true;
    }

    public boolean isPlaying() {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 12)) {
            return ((Boolean) iPatchRedirector.redirect((short) 12, this)).booleanValue();
        }
        SilkPlayerThread silkPlayerThread = this.G;
        return silkPlayerThread != null && SilkPlayerThread.c(silkPlayerThread);
    }

    public void pause() {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 16)) {
            iPatchRedirector.redirect((short) 16, this);
            return;
        }
        SilkPlayerThread silkPlayerThread = this.G;
        if (silkPlayerThread == null || !silkPlayerThread.isAlive()) {
            return;
        }
        SilkPlayerThread.d(this.G, false);
        this.G = null;
    }

    public void prepare() {
        FileInputStream fileInputStream;
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 14)) {
            iPatchRedirector.redirect((short) 14, this);
            return;
        }
        if (this.f == -1 || this.C == -1) {
            FileInputStream fileInputStream2 = null;
            try {
                try {
                    fileInputStream = new FileInputStream(this.e);
                } catch (Exception e) {
                    e = e;
                }
            } catch (Throwable th) {
                th = th;
            }
            try {
                try {
                    if (this.C == -1) {
                        this.C = QQAudioUtils.d(fileInputStream);
                    } else {
                        fileInputStream.skip(10L);
                    }
                    if (this.f == -1) {
                        int[] iArr = QQAudioUtils.a;
                        byte[] bArr = new byte[2];
                        int i = 0;
                        while (fileInputStream.read(bArr) > 0) {
                            int b = QQAudioUtils.b(bArr);
                            i += 20;
                            if (b > 0) {
                                fileInputStream.skip(b);
                            }
                        }
                        this.f = i;
                    }
                } catch (Exception e2) {
                    e = e2;
                    fileInputStream2 = fileInputStream;
                    if (QLog.isColorLevel()) {
                        QLog.d("SilkPlayer", 2, "silk player prepare exception=" + e.getMessage());
                    }
                    if (fileInputStream2 != null) {
                        fileInputStream = fileInputStream2;
                        fileInputStream.close();
                    }
                    return;
                } catch (Throwable th2) {
                    th = th2;
                    fileInputStream2 = fileInputStream;
                    if (fileInputStream2 != null) {
                        try {
                            fileInputStream2.close();
                        } catch (Exception unused) {
                        }
                    }
                    throw th;
                }
                fileInputStream.close();
            } catch (Exception unused2) {
            }
        }
    }

    public void release() {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 19)) {
            return;
        }
        iPatchRedirector.redirect((short) 19, this);
    }

    public void reset() {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 18)) {
            return;
        }
        iPatchRedirector.redirect((short) 18, this);
    }

    public void seekTo(int i) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 7)) {
            iPatchRedirector.redirect((short) 7, this, i);
            return;
        }
        this.h = i;
        if (QLog.isColorLevel()) {
            QLog.d("SilkPlayer", 2, "seekTo=" + i);
        }
    }

    public void setDataSource(String str) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 2)) {
            this.e = str;
        } else {
            iPatchRedirector.redirect((short) 2, this, str);
        }
    }

    public void setPlaySpeed(float f) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 5)) {
            iPatchRedirector.redirect((short) 5, new Object[]{this, Float.valueOf(f)});
            return;
        }
        this.D = f;
        a.c();
        if (QLog.isColorLevel()) {
            QLog.d("SilkPlayer", 2, "setPlaySpeed=" + f);
        }
    }

    public void start() {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 15)) {
            iPatchRedirector.redirect((short) 15, this);
            return;
        }
        if (this.G == null) {
            this.G = new SilkPlayerThread(this, this.F);
            if (this.h == -1 && this.m > 0) {
                seekTo(getCurrentPosition());
            }
            this.G.start();
        }
    }

    public void stop() {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 17)) {
            iPatchRedirector.redirect((short) 17, this);
            return;
        }
        SilkPlayerThread silkPlayerThread = this.G;
        if (silkPlayerThread != null && silkPlayerThread.isAlive()) {
            SilkPlayerThread.d(this.G, false);
            this.G = null;
        }
        this.f = -1;
        this.C = (byte) -1;
        this.i = 0;
        this.m = 0;
    }

    public void t() {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 21)) {
            iPatchRedirector.redirect((short) 21, this);
            return;
        }
        this.G = null;
        if (this.H != null) {
            this.I.sendEmptyMessage(2);
        }
    }

    public void u(int i, int i2, String str) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 20)) {
            iPatchRedirector.redirect((short) 20, new Object[]{this, Integer.valueOf(i), Integer.valueOf(i2), str});
            return;
        }
        this.G = null;
        if (this.H != null) {
            Message obtain = Message.obtain();
            obtain.what = 1;
            obtain.arg1 = i2;
            obtain.obj = str;
            this.I.sendMessage(obtain);
        }
        HashMap hashMap = new HashMap();
        hashMap.put("param_succ_flag", "0");
        hashMap.put("errCode", i + "");
        hashMap.put("param_version", Build.VERSION.SDK_INT + "");
        hashMap.put("param_deviceName", Build.MANUFACTURER + "_" + DeviceInfoMonitor.getModel());
        StatisticCollector.getInstance(BaseApplication.getContext()).collectPerformance((String) null, "PttSilkPlaryerError", true, 0L, 0L, hashMap, (String) null);
    }

    public void v(int i) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 22)) {
            iPatchRedirector.redirect((short) 22, this, i);
            return;
        }
        if (this.H != null) {
            if (this.J < 0) {
                this.I.sendMessage(this.I.obtainMessage(3, Integer.valueOf(i)));
                this.J = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - this.J > 100) {
                this.I.sendMessage(this.I.obtainMessage(3, Integer.valueOf(i)));
                this.J = System.currentTimeMillis();
            }
        }
    }

    void w(Exception exc, AudioTrack audioTrack, int i, int i2) {
        try {
            if (exc instanceof IllegalStateException) {
                HashMap hashMap = new HashMap();
                if (audioTrack != null) {
                    int state = audioTrack.getState();
                    int playState = audioTrack.getPlayState();
                    hashMap.put("param_state", state + "");
                    hashMap.put("param_play_state", playState + "");
                }
                String str = this.e;
                if (str != null) {
                    hashMap.put("param_filePath", str);
                }
                hashMap.put("param_streamType", this.d + "");
                hashMap.put("param_sampleRate", i + "");
                hashMap.put("param_playBufferSize", i2 + "");
                hashMap.put("param_version", Build.VERSION.SDK_INT + "");
                hashMap.put("param_deviceName", Build.MANUFACTURER + "_" + DeviceInfoMonitor.getModel());
                hashMap.put("param_exception_detail", exc.toString());
                StringBuilder sb = new StringBuilder();
                for (String str2 : hashMap.keySet()) {
                    sb.append(str2);
                    sb.append(" = ");
                    sb.append((String) hashMap.get(str2));
                    sb.append("; ");
                }
                QLog.e("SilkPlayer", 1, "reportIllegalStateException, " + sb.toString());
                StatisticCollector.getInstance(BaseApplication.getContext()).collectPerformance((String) null, "PttSilkPlayerStateError", true, 0L, 0L, hashMap, (String) null);
            }
        } catch (Throwable th) {
            QLog.e("SilkPlayer", 1, "reportIllegalStateException error.", th);
        }
    }

    public void x(j jVar) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector == null || !iPatchRedirector.hasPatch((short) 6)) {
            this.E = jVar;
        } else {
            iPatchRedirector.redirect((short) 6, this, jVar);
        }
    }
}
