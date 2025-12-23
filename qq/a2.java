//
// Decompiled by Jadx - 871ms
//
package com.tencent.mobileqq.qqaudio.silk;

import android.content.Context;
import com.tencent.commonsdk.soload.SoLoadCore;
import com.tencent.commonsdk.soload.SoLoadUtilNew;
import com.tencent.mobileqq.qfix.redirect.IPatchRedirector;
import com.tencent.mobileqq.qfix.redirect.PatchRedirectCenter;
import com.tencent.mobileqq.qqaudio.b;
import com.tencent.mobileqq.qqaudio.c;
import com.tencent.qphone.base.util.BaseApplication;
import com.tencent.qphone.base.util.QLog;
import java.io.File;

public class a {
    static IPatchRedirector $redirector_;
    public static boolean a;

    static {
        IPatchRedirector redirector = PatchRedirectCenter.getRedirector(37999);
        $redirector_ = redirector;
        if (redirector != null && redirector.hasPatch((short) 2)) {
            redirector.redirect((short) 2);
        } else {
            a = false;
        }
    }

    public static String a() {
        String appWorkPath = SoLoadCore.getAppWorkPath(BaseApplication.getContext());
        if (appWorkPath == null) {
            if (QLog.isColorLevel()) {
                QLog.i("SilkSoLoader", 2, "getFilesDir is null");
                return "";
            }
            return "";
        }
        return appWorkPath + "/txPttlib/";
    }

    public static String b() {
        String appWorkPath = SoLoadCore.getAppWorkPath(BaseApplication.getContext());
        if (appWorkPath == null) {
            if (QLog.isColorLevel()) {
                QLog.i("SilkSoLoader", 2, "getFilesDir is null");
                return "";
            }
            return "";
        }
        return appWorkPath + "/UnCompressPttSoTemp/";
    }

    public static boolean c(Context context, String str) {
        boolean z;
        int c = c.c();
        String a2 = a();
        String str2 = "";
        if (c > 2) {
            str2 = a2 + "lib" + str + "_658_v7.so";
        }
        synchronized ("SilkSoLoader") {
            boolean exists = new File(str2).exists();
            if (QLog.isColorLevel()) {
                QLog.i("SilkSoLoader", 2, "start LoadPttSo: " + str2 + " soFileExist=" + exists);
            }
            if (str.equals("codecsilk")) {
                exists = false;
            }
            z = true;
            if (exists) {
                try {
                    System.load(str2);
                    a = true;
                } catch (UnsatisfiedLinkError e) {
                    if (QLog.isColorLevel()) {
                        QLog.i("SilkSoLoader", 2, "load from txlib failed: " + e.getMessage());
                    }
                    z = SoLoadUtilNew.loadSoByName(context, str);
                }
            } else {
                if (QLog.isColorLevel()) {
                    QLog.i("SilkSoLoader", 2, "no ptt so in txlib.");
                }
                long currentTimeMillis = System.currentTimeMillis();
                boolean loadSoByName = SoLoadUtilNew.loadSoByName(context, str);
                QLog.i("SonicLibraryLoad", 1, "9015 卡顿监测, silk so load cost: " + (System.currentTimeMillis() - currentTimeMillis));
                z = loadSoByName;
            }
        }
        b.b(a, str);
        if (QLog.isColorLevel()) {
            QLog.i("SilkSoLoader", 2, "load " + str2 + " result=" + a);
        }
        return z;
    }
}
