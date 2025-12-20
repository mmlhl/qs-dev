//
// Decompiled by Jadx - 535ms
//
package com.tencent.mobileqq.utils;

import android.content.Context;
import android.os.SystemClock;
import com.tencent.mobileqq.qfix.redirect.IPatchRedirector;
import com.tencent.mobileqq.qfix.redirect.PatchRedirectCenter;
import com.tencent.mobileqq.qqaudio.QQAudioUtils;
import com.tencent.mobileqq.qqaudio.audioprocessor.a;
import com.tencent.mobileqq.qqaudio.audioprocessor.c;
import com.tencent.mobileqq.qqaudio.b;
import com.tencent.qphone.base.util.QLog;
import java.io.IOException;

public final class SilkCodecWrapper extends a {
    static IPatchRedirector $redirector_;
    private static boolean L;
    private int F;
    long G;
    boolean H;
    long I;
    int J;
    long K;

    static {
        IPatchRedirector redirector = PatchRedirectCenter.getRedirector(28983);
        $redirector_ = redirector;
        if (redirector == null || !redirector.hasPatch((short) 16)) {
            L = false;
        } else {
            redirector.redirect((short) 16);
        }
    }

    public SilkCodecWrapper(Context context, boolean z) {
        super(context);
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 1)) {
            iPatchRedirector.redirect((short) 1, new Object[]{this, context, Boolean.valueOf(z)});
            return;
        }
        this.F = 0;
        if (!L) {
            com.tencent.mobileqq.qqaudio.silk.a.b(((a) this).d, "codecsilk");
            L = true;
        }
        this.H = z;
        this.I = 0L;
        this.J = 0;
        this.K = 0L;
    }

    public static boolean e() {
        return L;
    }

    public native long SilkDecoderNew(int i, int i2);

    public native long SilkEncoderNew(int i, int i2);

    public void a(int i, int i2, int i3) throws IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 11)) {
            iPatchRedirector.redirect((short) 11, new Object[]{this, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3)});
            return;
        }
        super.a(i, i2, i3);
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

    public c.a b(int i, int i2, byte[] bArr) throws IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 3)) {
            return (c.a) iPatchRedirector.redirect((short) 3, new Object[]{this, bArr, Integer.valueOf(i), Integer.valueOf(i2)});
        }
        long uptimeMillis = SystemClock.uptimeMillis();
        c.a b = super.b(i, i2, bArr);
        long uptimeMillis2 = SystemClock.uptimeMillis() - uptimeMillis;
        if (uptimeMillis2 > this.K) {
            this.K = uptimeMillis2;
        }
        this.I += uptimeMillis2;
        this.J++;
        return b;
    }

    public int c(int i, int i2, byte[] bArr, byte[] bArr2) {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 15)) {
            return ((Integer) iPatchRedirector.redirect((short) 15, new Object[]{this, bArr, bArr2, Integer.valueOf(i), Integer.valueOf(i2)})).intValue();
        }
        long uptimeMillis = SystemClock.uptimeMillis();
        long j = this.G;
        if (j == 0) {
            if (com.tencent.mobileqq.inject.a.a.isDebugVersion()) {
                throw new IllegalStateException("not open");
            }
            return 0;
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

    public void close() throws IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 12)) {
            iPatchRedirector.redirect((short) 12, this);
        } else {
            super.close();
            d();
        }
    }

    public void d() {
        int i;
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 14)) {
            iPatchRedirector.redirect((short) 14, this);
            return;
        }
        long j = this.G;
        if (j != 0) {
            deleteCodec(j);
            long j2 = this.I;
            if (j2 > 0 && (i = this.J) > 0) {
                if (this.H) {
                    b.c(j2, i, this.K, 0);
                } else {
                    b.c(j2, i, this.K, 1);
                }
            }
        }
        this.G = 0L;
    }

    public native int decode(long j, byte[] bArr, byte[] bArr2, int i, int i2);

    public native void deleteCodec(long j);

    public native int encode(long j, byte[] bArr, byte[] bArr2, int i);

    public int read(byte[] bArr, int i, int i2) throws IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 4)) {
            return ((Integer) iPatchRedirector.redirect((short) 4, new Object[]{this, bArr, Integer.valueOf(i), Integer.valueOf(i2)})).intValue();
        }
        if (this.G == 0) {
            if (com.tencent.mobileqq.inject.a.a.isDebugVersion()) {
                throw new IllegalStateException("not open");
            }
            return 0;
        }
        if (((a) this).f.read(((a) this).h, 0, ((a) this).E) == -1) {
            return -1;
        }
        if (this.H) {
            this.F = encode(this.G, ((a) this).h, ((a) this).i, ((a) this).E);
        }
        int i3 = this.F;
        int[] iArr = QQAudioUtils.a;
        bArr[i] = (byte) (i3 & 255);
        bArr[i + 1] = (byte) ((i3 >> 8) & 255);
        System.arraycopy(((a) this).i, 0, bArr, i + 2, i3);
        return this.F + 2;
    }

    public void release() throws IOException {
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 13)) {
            iPatchRedirector.redirect((short) 13, this);
        } else {
            super.release();
            d();
        }
    }

    public SilkCodecWrapper(Context context) {
        super(context);
        IPatchRedirector iPatchRedirector = $redirector_;
        if (iPatchRedirector != null && iPatchRedirector.hasPatch((short) 2)) {
            iPatchRedirector.redirect((short) 2, this, context);
            return;
        }
        this.F = 0;
        if (!L) {
            com.tencent.mobileqq.qqaudio.silk.a.b(((a) this).d, "codecsilk");
            L = true;
        }
        this.H = true;
        this.I = 0L;
        this.J = 0;
        this.K = 0L;
    }
}
