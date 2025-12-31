//
// Decompiled by Jadx - 559ms
//
package com.tencent.liteav.base.util;

import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;

public class SoLoader {
    private static final String TAG = "SoLoader";
    private static ExternalSoLoader mExternalSoLoader = null;
    private static boolean mHasLoaded = false;
    private static String mLibraryPath = "";
    private static final Object mLoadLock = new Object();

    public static String getLibraryPath() {
        return mLibraryPath;
    }

    public static boolean loadAllLibraries() {
        boolean z;
        synchronized (mLoadLock) {
            if (!mHasLoaded) {
                ArrayList arrayList = new ArrayList();
                arrayList.add("txsoundtouch");
                arrayList.add("txffmpeg");
                arrayList.add("liteavsdk");
                if (mExternalSoLoader != null) {
                    mHasLoaded = loadLibraryByExternal((String[]) arrayList.toArray(new String[arrayList.size()]));
                } else if (TextUtils.isEmpty(mLibraryPath)) {
                    mHasLoaded = loadLibraryDefault((String[]) arrayList.toArray(new String[arrayList.size()]));
                } else {
                    mHasLoaded = loadLibraryInCustomerPath((String[]) arrayList.toArray(new String[arrayList.size()]));
                }
            }
            z = mHasLoaded;
        }
        return z;
    }

    public static boolean loadLibrary(String str) {
        try {
            synchronized (mLoadLock) {
                ExternalSoLoader externalSoLoader = mExternalSoLoader;
                if (externalSoLoader != null) {
                    return externalSoLoader.loadLibrary(new String[]{str});
                }
                if (!TextUtils.isEmpty(mLibraryPath)) {
                    boolean loadLibrary = loadLibrary(mLibraryPath, str);
                    Log.w(TAG, "load library " + str + " in customer path:" + loadLibrary);
                    return loadLibrary;
                }
                Log.w(TAG, "load library " + str + " from system path ");
                System.loadLibrary(str);
                return true;
            }
        } catch (Throwable th) {
            Log.w(TAG, "load library " + str + " failed: " + th);
            return false;
        }
    }

    private static boolean loadLibraryByExternal(String[] strArr) {
        ExternalSoLoader externalSoLoader = mExternalSoLoader;
        if (externalSoLoader != null && strArr != null && strArr.length != 0) {
            try {
                boolean loadLibrary = externalSoLoader.loadLibrary(strArr);
                Log.i(TAG, "load libraries " + Arrays.toString(strArr) + " by external: " + loadLibrary);
                return loadLibrary;
            } catch (Throwable th) {
                Log.w(TAG, "load libraries " + Arrays.toString(strArr) + " by external: " + th);
            }
        }
        return false;
    }

    private static boolean loadLibraryDefault(String[] strArr) {
        if (strArr != null && strArr.length != 0) {
            try {
                for (String str : strArr) {
                    System.loadLibrary(str);
                    Log.i(TAG, "load library " + str + " in default path success.");
                }
                return true;
            } catch (Throwable th) {
                Log.w(TAG, "load libraries " + Arrays.toString(strArr) + " in default path error: " + th);
            }
        }
        return false;
    }

    private static boolean loadLibraryInCustomerPath(String[] strArr) {
        int i = 0;
        if (strArr == null || strArr.length == 0 || TextUtils.isEmpty(mLibraryPath)) {
            return false;
        }
        int length = strArr.length;
        boolean z = false;
        while (i < length) {
            String str = strArr[i];
            boolean loadLibrary = loadLibrary(mLibraryPath, str);
            Log.i(TAG, "load library " + str + " in customer path:" + loadLibrary);
            if (!loadLibrary) {
                return loadLibrary;
            }
            i++;
            z = loadLibrary;
        }
        return z;
    }

    public static void setExternalSoLoader(ExternalSoLoader externalSoLoader) {
        synchronized (mLoadLock) {
            mExternalSoLoader = externalSoLoader;
        }
    }

    public static void setLibraryPath(String str) {
        Log.w(TAG, "setLibraryPath ".concat(String.valueOf(str)));
        mLibraryPath = str;
    }

    private static boolean loadLibrary(String str, String str2) {
        try {
            if (TextUtils.isEmpty(str)) {
                return false;
            }
            Log.w(TAG, "load library " + str2 + " from path " + str);
            System.load(str + "/lib" + str2 + ".so");
            return true;
        } catch (Throwable th) {
            Log.w(TAG, "load library " + str2 + "in path" + str + " failed: " + th);
            return false;
        }
    }
}
