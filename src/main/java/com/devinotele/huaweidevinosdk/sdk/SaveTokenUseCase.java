package com.devinotele.huaweidevinosdk.sdk;

import android.text.TextUtils;
import android.util.Log;

import com.huawei.agconnect.AGConnectOptions;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;

class SaveTokenUseCase extends BaseUC {

    private final DevinoLogsCallback logsCallback;

    SaveTokenUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    void run(AGConnectOptions connectOptions, HmsInstanceId hmsInstanceId) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String tokenScope = "HCM";
                    String agAppId = connectOptions.getString("client/app_id");
                    String token = hmsInstanceId.getToken(agAppId, tokenScope);
                    Log.d("DevinoPush", "SaveTokenUseCase: " + token);

                    if (!TextUtils.isEmpty(token)) {
                        SaveTokenUseCase.this.run(token);
                    }
                } catch (ApiException e) {
                    logsCallback.onMessageLogged("Push Kit Error: " + e.getMessage());
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
            logsCallback.onMessageLogged("Push token persisted\n" + token);
            DevinoSdk.getInstance().appStarted();
        }
    }
}