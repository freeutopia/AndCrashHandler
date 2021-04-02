package com.utopia.recovery.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.utopia.recovery.tools.RecoveryLog;
import com.utopia.recovery.tools.RecoveryUtil;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * ActivityManager动态代理
 */
class ActivityManagerDelegate implements InvocationHandler {

    private final Object mBaseActivityManagerProxy;

    ActivityManagerDelegate(Object o) {
        this.mBaseActivityManagerProxy = o;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("finishActivity".equals(method.getName())) {
            Class<? extends Activity> clazz = Recovery.getInstance().getMainPageClass();
            if (clazz == null) {
                return method.invoke(mBaseActivityManagerProxy, args);
            }

            Context context = Recovery.getInstance().getContext();
            int count = ActivityStackCompat.getActivityCount(context);
            String baseActivityName = ActivityStackCompat.getBaseActivityName(context);
            if (TextUtils.isEmpty(baseActivityName)) {
                return method.invoke(mBaseActivityManagerProxy, args);
            }

            RecoveryLog.e("currentActivityCount: " + count);
            RecoveryLog.e("baseActivityName: " + baseActivityName);

            if (count == 1 && !clazz.getName().equals(baseActivityName)) {
                Intent intent = new Intent(Recovery.getInstance().getContext(), clazz);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                if (RecoveryUtil.isIntentAvailable(Recovery.getInstance().getContext(), intent))
                    Recovery.getInstance().getContext().startActivity(intent);
            }
        }

        return method.invoke(mBaseActivityManagerProxy, args);
    }

}
