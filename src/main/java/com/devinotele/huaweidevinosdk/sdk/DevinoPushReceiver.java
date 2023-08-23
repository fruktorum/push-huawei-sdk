package com.devinotele.huaweidevinosdk.sdk;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.Arrays;

public class DevinoPushReceiver extends BroadcastReceiver {

    static final String KEY_DEEPLINK = "deepLink";
    static String KEY_DEFAULT_ACTION = "devino://default-push-action";
    static final String KEY_PUSH_ID = "pushId";

    @Override
    public void onReceive(Context context, Intent intent) {

        DevinoSdk.getInstance().hideNotification(context);

        String deepLink = intent.getStringExtra(KEY_DEEPLINK);
        String pushId = intent.getStringExtra(KEY_PUSH_ID);

        Intent startMain = new Intent(Intent.ACTION_VIEW);

        try {
            startMain.setData(Uri.parse(deepLink));
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(startMain);
        } catch (Throwable e) {
            e.printStackTrace();
            Log.d("DevinoPush", "e.localizedMessage =  " + e.getLocalizedMessage());
            new AlertDialog.Builder(context)
                    .setTitle("onReceive() startActivity: " + e.getLocalizedMessage())
                    .setMessage(Arrays.toString(e.getStackTrace()))
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
        }

        DevinoSdk.getInstance().pushEvent(pushId, DevinoSdk.PushStatus.OPENED, deepLink);
    }
}