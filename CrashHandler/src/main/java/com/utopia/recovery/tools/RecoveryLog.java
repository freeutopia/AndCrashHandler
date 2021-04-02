package com.utopia.recovery.tools;

import android.util.Log;

import com.utopia.recovery.core.Recovery;

public class RecoveryLog {

    private static final String TAG = "Recovery";

    public static void e(String message) {
        if (Recovery.getInstance().isDebug())
            Log.e(TAG, message);
    }
}
