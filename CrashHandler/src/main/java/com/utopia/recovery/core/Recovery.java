package com.utopia.recovery.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import androidx.annotation.Keep;

import com.utopia.recovery.callback.RecoveryActivityLifecycleCallback;
import com.utopia.recovery.exception.RecoveryException;
import com.utopia.recovery.tools.RecoveryLog;
import com.utopia.recovery.tools.RecoveryUtil;
import com.utopia.reflecct.Reflect;
import com.utopia.reflecct.handle.MethodHandle;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Recovery {

    private volatile static Recovery sInstance;

    private final Context mContext;

    private static boolean isDebug = false;
    private final RecoveryConfig mConfig ;
    private SilentMode mSilentMode = SilentMode.RECOVER_ACTIVITY_STACK;
    private final List<Class<? extends Activity>> mSkipActivities = new ArrayList<>();

    private Recovery(Context context , RecoveryConfig config) {
        mContext = context;
        mConfig = config;
        silent(Recovery.SilentMode.RECOVER_ACTIVITY_STACK);
        registerRecoveryHandler();
        registerRecoveryLifecycleCallback();
    }

    public static Recovery getInstance() {
        if (sInstance == null)
            throw new RecoveryException("Please initialize Recovery first！");
        return sInstance;
    }


    public static void init(Context context , RecoveryConfig config) {
        if (context == null)
            throw new RecoveryException("Context can not be null!");
        if (config == null)
            throw new RecoveryException("RecoveryConfig can not be null!");

        if (!(context instanceof Application))
            context = context.getApplicationContext();

        if (!RecoveryUtil.isMainProcess(context))
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

        }
        sInstance = new Recovery(context , config);
    }


    public static void debug(boolean isDebug) {
        Recovery.isDebug = isDebug;
    }

    public void silent(SilentMode mode) {
        this.mSilentMode = mode == null ? SilentMode.RECOVER_ACTIVITY_STACK : mode;
    }

    private void registerRecoveryHandler() {
        RecoveryHandler.newInstance(Thread.getDefaultUncaughtExceptionHandler()).setCallback(mConfig.getCallback()).register();
    }

    private void registerRecoveryLifecycleCallback() {
        ((Application) mContext).registerActivityLifecycleCallbacks(new RecoveryActivityLifecycleCallback());
    }

    public Context getContext() {
        return RecoveryUtil.checkNotNull(mContext, "The context is not initialized");
    }

    public boolean isDebug() {
        return isDebug;
    }

    boolean isRecoverInBackground() {
        return mConfig.isRecoverInBackground();
    }

    boolean isRecoverStack() {
        return mConfig.isRecoverStack();
    }

    boolean isRecoverEnabled() {
        return mConfig.isRecoverEnabled();
    }

    Class<? extends Activity> getMainPageClass() {
        return mConfig.getmMainPageClass();
    }

    boolean isSilentEnabled() {
        return mConfig.isSilentEnabled();
    }

    SilentMode getSilentMode() {
        return mSilentMode;
    }

    public List<Class<? extends Activity>> getSkipActivities() {
        return mSkipActivities;
    }

    @Keep
    private void registerRecoveryProxy() {
        //OS version in the 5.0 ~ 6.0 don`t register agent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        if (mConfig.getmMainPageClass() == null)
            return;
        if (!RecoveryUtil.isMainProcess(RecoveryUtil.checkNotNull(getContext(), "The context is not initialized")))
            return;
        new Thread(() -> {
            while (true) {
                boolean isSuccess = RecoveryComponentHook.hookActivityManagerProxy();
                RecoveryLog.e("hook is success:" + isSuccess);
                if (isSuccess)
                    break;
            }
        }).start();
    }

    public enum SilentMode {
        RESTART(1),
        RECOVER_ACTIVITY_STACK(2),
        RECOVER_TOP_ACTIVITY(3),
        RESTART_AND_CLEAR(4);

        int value;

        SilentMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SilentMode getMode(int value) {
            switch (value) {
                case 1:
                    return RESTART;
                case 2:
                    return RECOVER_ACTIVITY_STACK;
                case 3:
                    return RECOVER_TOP_ACTIVITY;
                case 4:
                    return RESTART_AND_CLEAR;
                default:
                    return RECOVER_ACTIVITY_STACK;
            }
        }
    }
}
