package com.devinotele.huaweidevinosdk.sdk;

import android.text.TextUtils;
import android.util.Log;

import com.huawei.agconnect.AGConnectOptions;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

class HandleTokenUseCase extends BaseUC {

    private final DevinoLogsCallback logsCallback;
    private final String phone;
    private final String email;
    private final String event = "Register token (put) ";

    HandleTokenUseCase(HelpersPackage hp, DevinoLogsCallback callback, String phone, String email) {
        super(hp);
        logsCallback = callback;
        this.phone = phone;
        this.email = email;
    }

    void run(AGConnectOptions connectOptions, HmsInstanceId hmsInstanceId) {
        HashMap<String, Object> customData =
                sharedPrefsHelper.getHashMap(SharedPrefsHelper.KEY_CUSTOM_DATA);
        if (sharedPrefsHelper.getBoolean(SharedPrefsHelper.KEY_TOKEN_REGISTERED)) {
            registerUser(email, phone, customData);
        }
        else {
            new Thread() {
                @Override
                public void run() {
                    try {
                        String tokenScope = "HCM";
                        String agAppId = connectOptions.getString("client/app_id");
                        String token = hmsInstanceId.getToken(agAppId, tokenScope);

                        if (!TextUtils.isEmpty(token)) {
                            Log.d("DevinoPush", "HandleTokenUseCase: " + token);
                            sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_PUSH_TOKEN, token);
                            DevinoSdk.getInstance().appStarted();
                            registerUser(email, phone, customData);
                        }
                    } catch (ApiException e) {
                        logsCallback.onMessageLogged("Push Kit Error: " + e.getMessage());
                    }
                }
            }.start();
        }
    }

    private void registerUser(String email, String phone, HashMap<String, Object> customData) {
        trackSubscription(networkRepository.registerUser(email, phone, customData)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        json -> {
                            sharedPrefsHelper.saveData(
                                    SharedPrefsHelper.KEY_TOKEN_REGISTERED,
                                    true
                            );
                            logsCallback.onMessageLogged(event + " -> " + json.toString());
                        },
                        throwable -> {
                            if (throwable instanceof HttpException)
                                logsCallback.onMessageLogged(
                                        getErrorMessage(
                                                event + " -> ",
                                                ((HttpException) throwable))
                                );
                            else
                                logsCallback.onMessageLogged(
                                        event + " -> " + throwable.getMessage()
                                );
                        }
                )
        );
    }
}