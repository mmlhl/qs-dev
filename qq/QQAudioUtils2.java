//
// Decompiled by Jadx - 924ms
//
package com.tencent.mobileqq.qqaudio;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import com.tencent.mobileqq.app.ThreadManager;
import com.tencent.mobileqq.mediafocus.b;
import com.tencent.mobileqq.pttview.a;
import com.tencent.mobileqq.qfix.redirect.IPatchRedirector;
import com.tencent.mobileqq.qfix.redirect.PatchRedirectCenter;
import com.tencent.mobileqq.qroute.annotation.ConfigInject;
import com.tencent.mobileqq.utils.AudioUtil;
import com.tencent.qphone.base.util.QLog;
import com.tencent.util.VersionUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class QQAudioUtils {
    static IPatchRedirector $redirector_;
    public static final int[] a;

    @ConfigInject(configPath = "AutoInjectYml/Component/AudioKit/Inject_QQAudioFocusLossProcessor.yml", version = 1)
    public static ArrayList<Class<? extends a>> b;
    private static final ArrayList<a> c;
    private static final b.c d;

    static {
        IPatchRedirector redirector = PatchRedirectCenter.getRedirector(36648);
        $redirector_ = redirector;
        if (redirector != null && redirector.hasPatch((short) 2)) {
            redirector.redirect((short) 2);
            return;
        }
        a = new int[]{8000, 12000, 16000, 24000, 36000, 44100, 48000};
        ArrayList<Class<? extends a>> arrayList = new ArrayList<>();
        b = arrayList;
        arrayList.add(a.class);
        b.add(fx3.a.class);
        d = new a();
        c = new ArrayList<>();
        Iterator<Class<? extends a>> it = b.iterator();
        while (it.hasNext()) {
            try {
                c.add(it.next().newInstance());
            } catch (IllegalAccessException | InstantiationException e) {
                QLog.e("QQAudioUtils", 1, "initAudioFocusLossProcessor error! ", e);
            }
        }
    }

    public static int b(byte[] bArr) {
        return (bArr[0] & 255) + ((bArr[1] & 255) << 8);
    }

    public static int c(int i) {
        return ((i * 20) * 2) / 1000;
    }

    public static int d(byte b2) {
        if (b2 >= 0) {
            int[] iArr = a;
            if (b2 < iArr.length) {
                return iArr[b2];
            }
        }
        return 0;
    }

    public static int e(byte b2, InputStream inputStream) throws IOException {
        byte[] bArr = new byte[2];
        int i = 0;
        while (inputStream.read(bArr) > 0) {
            int b3 = b(bArr);
            i += 20;
            if (b3 > 0) {
                inputStream.skip(b3);
            }
        }
        return i;
    }

    public static byte f(InputStream inputStream) throws Exception {
        byte[] bArr = new byte[10];
        inputStream.read(bArr, 0, 10);
        if (h(bArr)) {
            return bArr[0];
        }
        return (byte) -1;
    }

    public static byte[] g(int i, byte[] bArr, int i2) {
        bArr[i2] = (byte) (i & 255);
        bArr[i2 + 1] = (byte) ((i >> 8) & 255);
        return bArr;
    }

    public static boolean h(byte[] bArr) throws Exception {
        if (bArr != null && bArr.length == 10) {
            String x = AudioUtil.x(bArr, 1, 9);
            if (QLog.isColorLevel()) {
                QLog.d("QQAudioUtils", 2, "getSilkFs " + ((int) bArr[0]) + x);
            }
            if (x.startsWith("#!SILK_V")) {
                return true;
            }
            QLog.e("QQAudioUtils", 1, "isSilkFileHead: headString = " + x);
        }
        return false;
    }

    @TargetApi(8)
    public static boolean i(Context context, boolean z) {
        boolean z2 = false;
        if (context == null) {
            if (QLog.isColorLevel()) {
                QLog.d("AudioUtil", 2, "context is null.");
            }
            return false;
        }
        if (!VersionUtils.isrFroyo()) {
            if (QLog.isColorLevel()) {
                QLog.d("QQAudioUtils", 2, "Android 2.1 and below can not stop music");
            }
            return false;
        }
        AudioManager audioManager = (AudioManager) context.getSystemService("audio");
        if (z) {
            if (audioManager.requestAudioFocus(null, 3, 2) == 1) {
                z2 = true;
            }
            b.i().l(1, d);
        } else {
            try {
                if (audioManager.abandonAudioFocus(null) == 1) {
                    z2 = true;
                }
                ThreadManager.getSubThreadHandler().postDelayed(new QQAudioUtils$2(), 1000L);
            } catch (NullPointerException e) {
                QLog.e("QQAudioUtils", 1, "caught npe", e);
            }
        }
        if (QLog.isColorLevel()) {
            QLog.d("QQAudioUtils", 2, "pauseMusic bMute=" + z + " result=" + z2);
        }
        return z2;
    }

    protected static void j() {
        Iterator<a> it = c.iterator();
        while (it.hasNext()) {
            it.next().a();
        }
    }
}
