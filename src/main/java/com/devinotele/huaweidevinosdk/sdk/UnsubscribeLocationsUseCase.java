package com.devinotele.huaweidevinosdk.sdk;

import android.content.Context;

class UnsubscribeLocationsUseCase extends BaseUC {

    UnsubscribeLocationsUseCase(HelpersPackage hp) {
        super(hp);
    }

    public void run(Context context) {
        sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_GPS_SUBSCRIPTION_ACTIVE, false);
        DevinoLocationReceiver.cancelAlarm(context);
    }
}
