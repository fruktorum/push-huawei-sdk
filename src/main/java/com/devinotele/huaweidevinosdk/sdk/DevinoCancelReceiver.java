package com.devinotele.huaweidevinosdk.sdk;

import android.content.Context;
import android.content.Intent;

public class DevinoCancelReceiver extends DevinoPushReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String pushId = intent.getStringExtra(KEY_PUSH_ID);
        DevinoSdk.getInstance().pushEvent(pushId, DevinoSdk.PushStatus.CANCELED, null);
    }
}