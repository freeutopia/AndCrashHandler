package com.utopia.recovery.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.utopia.mvp.presenter.ActivityPresenter;
import com.utopia.recovery.R;
import com.utopia.recovery.core.RecoveryStore;
import com.utopia.recovery.model.RecoveryModel;
import com.utopia.recovery.tools.RecoverySharedPrefsUtil;
import com.utopia.recovery.tools.RecoveryUtil;
import com.utopia.recovery.view.RecoveryView;

import java.io.File;
import java.util.ArrayList;

public class RecoveryActivity extends ActivityPresenter<RecoveryView, RecoveryModel> {
    public static final String RECOVERY_MODE_ACTIVE = "recovery_mode_active";
    private AlertDialog warningDialog;

    @Override
    public void inCreat(Bundle savedInstanceState) {
        warningDialog = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定要进行该操作吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    factoryReset();
                    dialog.dismiss();
                }).setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create();
        setOnClickListener();
    }

    private void setOnClickListener(){
        getViewRef().getBtnRescue().setOnClickListener(v->{
            boolean restart = RecoverySharedPrefsUtil.shouldRestartApp();
            if (restart) {
                RecoverySharedPrefsUtil.clear();
                restart();
                return;
            }
            if (isRecoverStack()) {
                recoverActivityStack();
            } else {
                recoverTopActivity();
            }
        });
        getViewRef().getTvRecovery().setOnClickListener(v->{
            warningDialog.show();
        });
    }

    @Override
    public void inDestory() {

    }

    //启动recovery模式
    public void factoryReset() {
        Intent recoverIntent = new Intent("android.intent.action.MASTER_CLEAR");
        recoverIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        sendBroadcast(recoverIntent);
    }

    private RecoveryStore.ExceptionData getExceptionData() {
        return getIntent().getParcelableExtra(RecoveryStore.EXCEPTION_DATA);
    }

    private String getCause() {
        return getIntent().getStringExtra(RecoveryStore.EXCEPTION_CAUSE);
    }

    private String getStackTrace() {
        return getIntent().getStringExtra(RecoveryStore.STACK_TRACE);
    }

    private void recoverTopActivity() {
        Intent intent = getRecoveryIntent();
        if (intent != null && RecoveryUtil.isIntentAvailable(this, intent)) {
            intent.setExtrasClassLoader(getClassLoader());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(RECOVERY_MODE_ACTIVE, true);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return;
        }
        restart();
    }

    private boolean isRecoverStack() {
        boolean hasRecoverStack = getIntent().hasExtra(RecoveryStore.RECOVERY_STACK);
        return !hasRecoverStack || getIntent().getBooleanExtra(RecoveryStore.RECOVERY_STACK, true);
    }

    private void recoverActivityStack() {
        ArrayList<Intent> intents = getRecoveryIntents();
        if (intents != null && !intents.isEmpty()) {
            ArrayList<Intent> availableIntents = new ArrayList<>();
            for (Intent tmp : intents) {
                if (tmp != null && RecoveryUtil.isIntentAvailable(this, tmp)) {
                    tmp.setExtrasClassLoader(getClassLoader());
                    availableIntents.add(tmp);
                }
            }

            int activityIntentCount = availableIntents.size();
            if (activityIntentCount > 0) {
                availableIntents.get(0).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                availableIntents.get(activityIntentCount - 1).putExtra(RECOVERY_MODE_ACTIVE, true);
                startActivities(availableIntents.toArray(new Intent[activityIntentCount]));
                overridePendingTransition(0, 0);
                finish();
                return;
            }
        }
        restart();
    }

    private void restart() {
        Intent launchIntent = getApplication().getPackageManager().getLaunchIntentForPackage(this.getPackageName());
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(launchIntent);
            overridePendingTransition(0, 0);
        }
        finish();
    }

    private Intent getRecoveryIntent() {
        boolean hasRecoverIntent = getIntent().hasExtra(RecoveryStore.RECOVERY_INTENT);
        if (!hasRecoverIntent)
            return null;
        return getIntent().getParcelableExtra(RecoveryStore.RECOVERY_INTENT);
    }

    private ArrayList<Intent> getRecoveryIntents() {
        boolean hasRecoveryIntents = getIntent().hasExtra(RecoveryStore.RECOVERY_INTENTS);
        if (!hasRecoveryIntents)
            return null;
        return getIntent().getParcelableArrayListExtra(RecoveryStore.RECOVERY_INTENTS);
    }

    private void killProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
