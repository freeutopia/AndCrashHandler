package com.utopia.recovery.core;

import android.app.Activity;
import com.utopia.recovery.callback.RecoveryCallback;


public class RecoveryConfig {
    private final boolean isRecoverStack ;//恢复整个activity堆栈
    private final boolean isRecoverInBackground ;  //后台恢复
    private final Class<? extends Activity> mMainPageClass;//入口Activity
    private final RecoveryCallback mCallback;//回调
    private final boolean isSilentEnabled ;//静默恢复
    private final boolean isRecoverEnabled ;//允许恢复

    private RecoveryConfig(Builder builder) {
        this.isRecoverStack = builder.isRecoverStack;
        this.isRecoverInBackground = builder.isRecoverInBackground;
        this.mMainPageClass = builder.mMainPageClass;
        this.mCallback = builder.mCallback;
        this.isSilentEnabled = builder.isSilentEnabled;
        this.isRecoverEnabled = builder.isRecoverEnabled;
    }

    public static final class Builder {
        boolean isRecoverStack = true;
        boolean isRecoverInBackground = false;//是否后台恢复
        Class<? extends Activity> mMainPageClass;//入口Activity
        RecoveryCallback mCallback;//回调
        boolean isSilentEnabled = false;//静默 恢复
        boolean isRecoverEnabled = true;

        public RecoveryConfig.Builder recoverStack(boolean recoverStack) {
            this.isRecoverStack = recoverStack;
            return this;
        }

        public RecoveryConfig.Builder recoverInBackground(boolean recoverInBackground) {
            this.isRecoverInBackground = recoverInBackground;
            return this;
        }

        public RecoveryConfig.Builder mainPageActivity(Class<? extends Activity> mMainPageClass) {
            this.mMainPageClass = mMainPageClass;
            return this;
        }

        public RecoveryConfig.Builder callback(RecoveryCallback mCallback) {
            this.mCallback = mCallback;
            return this;
        }

        public RecoveryConfig.Builder silent(boolean silentEnabled) {
            this.isSilentEnabled = silentEnabled;
            return this;
        }

        public RecoveryConfig.Builder recoverEnabled(boolean recoverEnabled) {
            this.isRecoverEnabled = recoverEnabled;
            return this;
        }

        public RecoveryConfig build() {
            return new RecoveryConfig(this);
        }
    }

    public boolean isRecoverStack() {
        return isRecoverStack;
    }

    public boolean isRecoverInBackground() {
        return isRecoverInBackground;
    }

    public Class<? extends Activity> getmMainPageClass() {
        return mMainPageClass;
    }

    public RecoveryCallback getCallback() {
        return mCallback;
    }

    public boolean isSilentEnabled() {
        return isSilentEnabled;
    }

    public boolean isRecoverEnabled() {
        return isRecoverEnabled;
    }
}
