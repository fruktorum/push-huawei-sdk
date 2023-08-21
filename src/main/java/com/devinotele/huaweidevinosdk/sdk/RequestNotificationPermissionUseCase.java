package com.devinotele.huaweidevinosdk.sdk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

class RequestNotificationPermissionUseCase extends BaseUC {

    private final DevinoLogsCallback logsCallback;

    RequestNotificationPermissionUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    void run(Activity activity, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    activity, new String[] {Manifest.permission.POST_NOTIFICATIONS},
                    requestCode
            );
        } else {
            logsCallback.onMessageLogged("Notification permission has already been granted");
        }
    }
}