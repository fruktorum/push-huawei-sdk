package com.devinotele.huaweidevinosdk.sdk;

import static android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

class SubscribeLocationsUseCase extends BaseUC {

    SubscribeLocationsUseCase(HelpersPackage hp) {
        super(hp);
    }

    void run(Context context, int intervalMinutes) {
        sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_GPS_SUBSCRIPTION_ACTIVE, true);
        sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_GPS_INTERVAL, intervalMinutes);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (alarm.canScheduleExactAlarms()) {
                DevinoSdk.getInstance().getLogCallBack().onMessageLogged("Alarm permission granted");
            } else {
                DevinoSdk.getInstance().getLogCallBack().onMessageLogged("Alarm permission required");
                runPermissionRequest(context);
                return;
            }
        }

        DevinoLocationReceiver.setAlarm(context, 0);

        DevinoSdk.getInstance().getLogCallBack()
                .onMessageLogged(
                        context.getString(R.string.subscribed_geo_interval,
                                intervalMinutes,
                                context.getString(R.string.min)
                        )
                );
    }

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private void runPermissionRequest(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
        context.startActivity(intent);
    }
}