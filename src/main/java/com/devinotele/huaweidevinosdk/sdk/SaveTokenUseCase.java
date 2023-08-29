package com.devinotele.huaweidevinosdk.sdk;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.agconnect.AGConnectOptions;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class SaveTokenUseCase extends BaseUC {

    private final DevinoLogsCallback logsCallback;
    private final AGConnectOptions connectOptions;
    private final HmsInstanceId hmsInstanceId;

    SaveTokenUseCase(HelpersPackage hp, DevinoLogsCallback callback, AGConnectOptions connectOpt, HmsInstanceId hmsInstId) {
        super(hp);
        logsCallback = callback;
        connectOptions = connectOpt;
        hmsInstanceId = hmsInstId;
    }

    void run(/*AGConnectOptions connectOptions, HmsInstanceId hmsInstanceId*/) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String tokenScope = "HCM";
                    String agAppId = connectOptions.getString("client/app_id");
                    String token = hmsInstanceId.getToken(agAppId, tokenScope);
                    Log.d("DevinoPush", "1 SaveTokenUseCase: " + token);

                    if (!TextUtils.isEmpty(token)) {
                        Log.d("DevinoPush", "SaveTokenUseCase run save token");
                        SaveTokenUseCase.this.run(token);
                        Log.d("DevinoPush", "SaveTokenUseCase run save finihed");
                    }
                } catch (ApiException e) {
                    logsCallback.onMessageLogged("Push Kit Error: " + e.getLocalizedMessage());
                    Log.d("DevinoPush", "SaveTokenUseCase ApiException: " + e.getLocalizedMessage());
                }
            }
        }.start();
    }

    void run (String token) {
        String persistedToken = sharedPrefsHelper.getString(SharedPrefsHelper.KEY_PUSH_TOKEN);
        if (!token.equals(persistedToken)) {
            sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_PUSH_TOKEN, token);
            Log.d("DevinoPush", "SaveTokenUseCase: token saved ");
            networkRepository.updateToken(token);
            //logsCallback.onMessageLogged("The token has been received:\n" + token);
            DevinoSdk.getInstance().appStarted();
        }
    }
}