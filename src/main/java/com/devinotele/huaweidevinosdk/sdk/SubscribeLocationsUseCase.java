package com.devinotele.huaweidevinosdk.sdk;

import android.content.Context;

class SubscribeLocationsUseCase extends BaseUC {

    SubscribeLocationsUseCase(HelpersPackage hp) {
        super(hp);
    }

    void run(Context context, int intervalMinutes) {
        sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_GPS_SUBSCRIPTION_ACTIVE, true);
        sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_GPS_INTERVAL, intervalMinutes);
        DevinoLocationReceiver.setAlarm(context, 0);
    }
}