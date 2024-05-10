package com.devinotele.huaweidevinosdk.sdk;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class DevinoAlarmPermissionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)) {
            DevinoSdk.getInstance().getLogCallBack().onMessageLogged("Alarm permission changed");
            DevinoSdk.getInstance().subscribeGeo(context, 1);
        }
    }
}
