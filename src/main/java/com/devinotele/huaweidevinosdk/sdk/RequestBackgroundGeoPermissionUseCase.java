package com.devinotele.huaweidevinosdk.sdk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class RequestBackgroundGeoPermissionUseCase extends BaseUC {

    private final DevinoLogsCallback logsCallback;

    RequestBackgroundGeoPermissionUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    void run(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(activity,
                        new String[]{
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        },
                        requestCode);
            } else {
                logsCallback.onMessageLogged("Background Geo Permission has already been granted");
            }
        }
    }
}