package com.devinotele.huaweidevinosdk.sdk;

import android.content.Context;
import android.widget.Toast;

public class UpdateApiBaseUrlUseCase extends BaseUC {

    private final DevinoLogsCallback logsCallback;
    private final RetrofitClientInstance retrofitClientInstance;

    UpdateApiBaseUrlUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
        retrofitClientInstance = new RetrofitClientInstance();
    }

    void run(String newApiBaseUrl, Context ctx) {
        sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_API_BASE_URL, newApiBaseUrl);
        logsCallback.onMessageLogged("Api Root Url was changed -> " + newApiBaseUrl);
        retrofitClientInstance.setApiBaseUrl(newApiBaseUrl);
        updateNetworkRepository();
        Toast.makeText(ctx, "Api Root Url was updated", Toast.LENGTH_SHORT).show();
    }
}