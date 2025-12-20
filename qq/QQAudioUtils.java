//
// Decompiled by Jadx - 492ms
//
package com.tencent.mobileqq.qqaudio;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import com.tencent.mobileqq.app.ThreadManager;
import com.tencent.mobileqq.mediafocus.d;
import com.tencent.mobileqq.pttview.a;
import com.tencent.mobileqq.qfix.redirect.IPatchRedirector;
import com.tencent.mobileqq.qfix.redirect.PatchRedirectCenter;
import com.tencent.mobileqq.qroute.annotation.ConfigInject;
import com.tencent.mobileqq.utils.AudioUtil;
import com.tencent.qphone.base.util.QLog;
import com.tencent.util.VersionUtils;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
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
    private static final d.c d;

    static {
        IPatchRedirector redirector = PatchRedirectCenter.getRedirector(23862);
        $redirector_ = redirector;
        if (redirector != null && redirector.hasPatch((short) 2)) {
            redirector.redirect((short) 2);
            return;
        }
        a = new int[]{8000, 12000, 16000, 24000, 36000, 44100, 48000};
        ArrayList<Class<? extends a>> arrayList = new ArrayList<>();
        b = arrayList;
        arrayList.add(a.class);
        b.add(s03.a.class);
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

    public static byte d(InputStream inputStream) throws Exception {
        byte[] bArr = new byte[10];
        inputStream.read(bArr, 0, 10);
        if (e(bArr)) {
            return bArr[0];
        }
        return (byte) -1;
    }

    /* JADX WARN: Code restructure failed: missing block: B:31:0x0058, code lost:
    
        if (r5 == null) goto L37;
     */
    /* JADX WARN: Code restructure failed: missing block: B:34:0x005a, code lost:
    
        r5.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:43:0x004f, code lost:
    
        if (r5 == null) goto L37;
     */
    /* JADX WARN: Removed duplicated region for block: B:21:0x0069  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x0089 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:26:0x008a  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean e(byte[] bArr) throws Exception {
        ByteArrayInputStream byteArrayInputStream;
        String str;
        if (bArr != null && bArr.length == 10) {
            MediaPlayer mediaPlayer = AudioUtil.b;
            DataInputStream dataInputStream = null;
            try {
                byte[] bArr2 = new byte[11];
                bArr2[0] = (byte) 0;
                bArr2[1] = (byte) 9;
                System.arraycopy(bArr, 1, bArr2, 2, 9);
                byteArrayInputStream = new ByteArrayInputStream(bArr2);
                try {
                    DataInputStream dataInputStream2 = new DataInputStream(byteArrayInputStream);
                    try {
                        str = dataInputStream2.readUTF();
                        try {
                            dataInputStream2.close();
                        } catch (IOException unused) {
                        }
                        try {
                            byteArrayInputStream.close();
                        } catch (IOException unused2) {
                            if (QLog.isColorLevel()) {
                                QLog.d("QQAudioUtils", 2, "getSilkFs " + ((int) bArr[0]) + str);
                            }
                            if (!str.startsWith("#!SILK_V")) {
                                return true;
                            }
                            QLog.e("QQAudioUtils", 1, "isSilkFileHead: headString = ".concat(str));
                            return false;
                        }
                    } catch (IOException unused3) {
                        dataInputStream = dataInputStream2;
                        if (dataInputStream != null) {
                            try {
                                dataInputStream.close();
                            } catch (IOException unused4) {
                            }
                        }
                    } catch (Exception unused5) {
                        dataInputStream = dataInputStream2;
                        if (dataInputStream != null) {
                            try {
                                dataInputStream.close();
                            } catch (IOException unused6) {
                            }
                        }
                    } catch (Throwable th) {
                        th = th;
                        dataInputStream = dataInputStream2;
                        if (dataInputStream != null) {
                            try {
                                dataInputStream.close();
                            } catch (IOException unused7) {
                            }
                        }
                        if (byteArrayInputStream == null) {
                            throw th;
                        }
                        try {
                            byteArrayInputStream.close();
                            throw th;
                        } catch (IOException unused8) {
                            throw th;
                        }
                    }
                } catch (IOException unused9) {
                } catch (Exception unused10) {
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (IOException unused11) {
                byteArrayInputStream = null;
            } catch (Exception unused12) {
                byteArrayInputStream = null;
            } catch (Throwable th3) {
                th = th3;
                byteArrayInputStream = null;
            }
        }
        return false;
        str = "";
        if (QLog.isColorLevel()) {
        }
        if (!str.startsWith("#!SILK_V")) {
        }
    }

    public static boolean f(Context context, boolean z) {
        return g(context, z, null);
    }

    @TargetApi(8)
    public static boolean g(Context context, boolean z, AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener) {
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
            r1 = audioManager.requestAudioFocus(onAudioFocusChangeListener, 3, 2) == 1;
            d.g().k(1, d);
        } else {
            try {
                r1 = audioManager.abandonAudioFocus(onAudioFocusChangeListener) == 1;
                ThreadManager.getSubThreadHandler().postDelayed(new QQAudioUtils$2(), 1000L);
            } catch (NullPointerException e) {
                QLog.e("QQAudioUtils", 1, "caught npe", e);
            }
        }
        if (QLog.isColorLevel()) {
            QLog.d("QQAudioUtils", 2, "pauseMusic bMute=" + z + " result=" + r1);
        }
        return r1;
    }

    protected static void h() {
        Iterator<a> it = c.iterator();
        while (it.hasNext()) {
            it.next().a();
        }
    }
}
