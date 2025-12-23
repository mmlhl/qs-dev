//
// Decompiled by Jadx - 1519ms
//
package com.tencent.mobileqq.utils;

import android.content.Context;
import android.os.SystemClock;
import com.tencent.mobileqq.qqaudio.QQAudioUtils;
import com.tencent.mobileqq.qqaudio.audioprocessor.a;
import com.tencent.mobileqq.qqaudio.audioprocessor.c;
import com.tencent.mobileqq.qqaudio.b;
import com.tencent.qphone.base.util.QLog;
import java.io.IOException;

public final class SilkCodecWrapper extends a {
    private static boolean L;
    private int F;
    long G;
    boolean H;
    long I;
    int J;
    long K;

    public SilkCodecWrapper(Context context, boolean z) {
        super(context);
        this.F = 0;
        e(z);
    }

    private void e(boolean z) {
        if (!L) {
            com.tencent.mobileqq.qqaudio.silk.a.c(((a) this).d, "codecsilk");
            L = true;
        }
        this.H = z;
        this.I = 0L;
        this.J = 0;
        this.K = 0L;
    }

    public static boolean h() {
        return L;
    }

    public native long SilkDecoderNew(int i, int i2);

    public native long SilkEncoderNew(int i, int i2);

    public c.a a(byte[] bArr, int i, int i2) throws IOException {
        long uptimeMillis = SystemClock.uptimeMillis();
        c.a a = super.a(bArr, i, i2);
        long uptimeMillis2 = SystemClock.uptimeMillis() - uptimeMillis;
        if (uptimeMillis2 > this.K) {
            this.K = uptimeMillis2;
        }
        this.I += uptimeMillis2;
        this.J++;
        return a;
    }

    public void b(int i, int i2, int i3) throws IOException {
        super.b(i, i2, i3);
        try {
            if (this.H) {
                this.G = SilkEncoderNew(i, i2);
            } else {
                this.G = SilkDecoderNew(i, i2);
            }
        } catch (UnsatisfiedLinkError e) {
            if (QLog.isColorLevel()) {
                QLog.d("SilkCodecWrapper", 2, "init silk codec =" + e.toString());
            }
            this.G = 0L;
            L = false;
        }
        int c = QQAudioUtils.c(i);
        ((a) this).E = c;
        ((a) this).h = new byte[c];
        ((a) this).i = new byte[c];
        byte[] bArr = new byte[c];
        ((a) this).m = bArr;
        ((a) this).C = new c.a(bArr, 0);
    }

    public void close() throws IOException {
        super.close();
        g();
    }

    public native int decode(long j, byte[] bArr, byte[] bArr2, int i, int i2);

    public native void deleteCodec(long j);

    public native int encode(long j, byte[] bArr, byte[] bArr2, int i);

    public int f(byte[] bArr, byte[] bArr2, int i, int i2) {
        long uptimeMillis = SystemClock.uptimeMillis();
        long j = this.G;
        if (j == 0) {
            if (!com.tencent.mobileqq.inject.a.a.isDebugVersion()) {
                return 0;
            }
            throw new IllegalStateException("not open");
        }
        try {
            int decode = decode(j, bArr, bArr2, i, i2);
            long uptimeMillis2 = SystemClock.uptimeMillis() - uptimeMillis;
            this.I += uptimeMillis2;
            if (uptimeMillis2 > this.K) {
                this.K = uptimeMillis2;
            }
            this.J++;
            return decode;
        } catch (Throwable th) {
            th.printStackTrace();
            return 0;
        }
    }

    public void g() {
        int i;
        long j = this.G;
        if (j != 0) {
            deleteCodec(j);
            long j2 = this.I;
            if (j2 > 0 && (i = this.J) > 0) {
                if (this.H) {
                    b.d(j2, i, this.K, 0);
                } else {
                    b.d(j2, i, this.K, 1);
                }
            }
        }
        this.G = 0L;
    }

    public int read(byte[] bArr, int i, int i2) throws IOException {
        if (this.G == 0) {
            if (!com.tencent.mobileqq.inject.a.a.isDebugVersion()) {
                return 0;
            }
            throw new IllegalStateException("not open");
        }
        if (((a) this).f.read(((a) this).h, 0, ((a) this).E) == -1) {
            return -1;
        }
        if (this.H) {
            this.F = encode(this.G, ((a) this).h, ((a) this).i, ((a) this).E);
        }
        QQAudioUtils.g(this.F, bArr, i);
        System.arraycopy(((a) this).i, 0, bArr, i + 2, this.F);
        return this.F + 2;
    }

    public void release() throws IOException {
        super.release();
        g();
    }

    public SilkCodecWrapper(Context context) {
        super(context);
        this.F = 0;
        e(true);
    }
}
