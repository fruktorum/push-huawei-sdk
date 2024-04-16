package com.devinotele.huaweidevinosdk.sdk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class RequestForegroundGeoAndNotificationPermissionUseCase extends BaseUC {

    private final DevinoLogsCallback logsCallback;

    RequestForegroundGeoAndNotificationPermissionUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    void run(Activity activity, int requestCode) {

        logsCallback.onMessageLogged("Geo and Notification permissions run");

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    requestCode);
        } else {
            logsCallback.onMessageLogged("Foreground Geo and Notification permissions has already been granted");
        }
    }
}