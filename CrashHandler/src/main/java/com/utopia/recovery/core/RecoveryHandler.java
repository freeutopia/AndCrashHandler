package com.utopia.recovery.core;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.RelativeLayout;


import com.utopia.recovery.activity.RecoveryActivity;
import com.utopia.recovery.callback.RecoveryCallback;
import com.utopia.recovery.tools.DefaultHandlerUtil;
import com.utopia.recovery.tools.RecoverySharedPrefsUtil;
import com.utopia.recovery.tools.RecoverySilentSharedPrefsUtil;
import com.utopia.recovery.tools.RecoveryUtil;

import java.io.PrintWriter;
import java.io.StringWriter;


final class RecoveryHandler implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;

    private RecoveryCallback mCallback;

    private RecoveryStore.ExceptionData mExceptionData;

    private String mStackTrace;

    private String mCause;

    private RecoveryHandler(Thread.UncaughtExceptionHandler defHandler) {
        mDefaultUncaughtExceptionHandler = defHandler;
    }

    static RecoveryHandler newInstance(Thread.UncaughtExceptionHandler defHandler) {
        return new RecoveryHandler(defHandler);
    }

    @Override
    public synchronized void uncaughtException(Thread t, Throwable e) {

        if (Recovery.getInstance().isRecoverEnabled()) {
            if (Recovery.getInstance().isSilentEnabled()) {
                RecoverySilentSharedPrefsUtil.recordCrashData();
            } else {
                RecoverySharedPrefsUtil.recordCrashData();
            }
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();

        String stackTrace = sw.toString();
        String cause = e.getMessage();
        Throwable rootTr = e;
        while (e.getCause() != null) {
            e = e.getCause();
            if (e.getStackTrace().length > 0)
                rootTr = e;
            String msg = e.getMessage();
            if (!TextUtils.isEmpty(msg))
                cause = msg;
        }

        String exceptionType = rootTr.getClass().getName();

        String throwClassName;
        String throwMethodName;
        int throwLineNumber;

        if (rootTr.getStackTrace().length > 0) {
            StackTraceElement trace = rootTr.getStackTrace()[0];
            throwClassName = trace.getClassName();
            throwMethodName = trace.getMethodName();
            throwLineNumber = trace.getLineNumber();
        } else {
            throwClassName = "unknown";
            throwMethodName = "unknown";
            throwLineNumber = 0;
        }

        mExceptionData = RecoveryStore.ExceptionData.newInstance()
                .type(exceptionType)
                .className(throwClassName)
                .methodName(throwMethodName)
                .lineNumber(throwLineNumber);

        mStackTrace = stackTrace;
        mCause = cause;

        if (mCallback != null) {
            mCallback.stackTrace(stackTrace);
            mCallback.cause(cause);
            mCallback.exception(exceptionType, throwClassName, throwMethodName, throwLineNumber);
            mCallback.throwable(e);
        }

        if (!DefaultHandlerUtil.isSystemDefaultUncaughtExceptionHandler(mDefaultUncaughtExceptionHandler)) {
            if (mDefaultUncaughtExceptionHandler == null) {
                killProcess();
                return;
            }
            recover();
            mDefaultUncaughtExceptionHandler.uncaughtException(t, e);
        } else {
            recover();
            killProcess();
        }

    }

    RecoveryHandler setCallback(RecoveryCallback callback) {
        mCallback = callback;
        return this;
    }

    private void recover() {
        if (!Recovery.getInstance().isRecoverEnabled())
            return;

        Context context = Recovery.getInstance().getContext();
        if (RecoveryUtil.isAppInBackground(context)
                && !Recovery.getInstance().isRecoverInBackground()) {
            killProcess();
            return;
        }

        if (Recovery.getInstance().isSilentEnabled()) {
            startRecoverService();
        } else {
            startRecoverActivity();
        }
    }

    private void startRecoverActivity() {
        Intent intent = new Intent();
        Context context = Recovery.getInstance().getContext();
        intent.setClass(context, RecoveryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        if (RecoveryStore.getInstance().getIntent() != null)
            intent.putExtra(RecoveryStore.RECOVERY_INTENT, RecoveryStore.getInstance().getIntent());
        if (!RecoveryStore.getInstance().getIntents().isEmpty())
            intent.putParcelableArrayListExtra(RecoveryStore.RECOVERY_INTENTS, RecoveryStore.getInstance().getIntents());
        intent.putExtra(RecoveryStore.RECOVERY_STACK, Recovery.getInstance().isRecoverStack());
        intent.putExtra(RecoveryStore.IS_DEBUG, Recovery.getInstance().isDebug());
        if (mExceptionData != null)
            intent.putExtra(RecoveryStore.EXCEPTION_DATA, mExceptionData);
        intent.putExtra(RecoveryStore.STACK_TRACE, String.valueOf(mStackTrace));
        intent.putExtra(RecoveryStore.EXCEPTION_CAUSE, String.valueOf(mCause));
        context.startActivity(intent);
    }

    private void startRecoverService() {
        Intent intent = new Intent();
        Context context = Recovery.getInstance().getContext();
        intent.setClass(context, RecoveryService.class);
        if (RecoveryStore.getInstance().getIntent() != null)
            intent.putExtra(RecoveryStore.RECOVERY_INTENT, RecoveryStore.getInstance().getIntent());
        if (!RecoveryStore.getInstance().getIntents().isEmpty())
            intent.putParcelableArrayListExtra(RecoveryStore.RECOVERY_INTENTS, RecoveryStore.getInstance().getIntents());
        intent.putExtra(RecoveryService.RECOVERY_SILENT_MODE_VALUE, Recovery.getInstance().getSilentMode().getValue());
        RecoveryService.start(context, intent);
    }

    void register() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private void killProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

}
