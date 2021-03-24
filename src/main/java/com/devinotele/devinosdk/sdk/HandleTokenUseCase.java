package com.devinotele.devinosdk.sdk;


import android.text.TextUtils;
import android.util.Log;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;


class HandleTokenUseCase extends BaseUC {


    private DevinoLogsCallback logsCallback;
    private String phone, email;
    private String event = "register token (put) ";

    HandleTokenUseCase(HelpersPackage hp, DevinoLogsCallback callback, String phone, String email) {
        super(hp);
        logsCallback = callback;
        this.phone = phone;
        this.email = email;
    }

    void run(AGConnectServicesConfig config, HmsInstanceId hmsInstanceId) {
        if (sharedPrefsHelper.getBoolean(SharedPrefsHelper.KEY_TOKEN_REGISTERED))
            registerUser(email, phone);
        else {
            new Thread() {
                @Override
                public void run() {
                    try {
                        String tokenScope = "HCM";
                        String agAppId = config.getString("client/app_id");
                        String token = hmsInstanceId.getToken(agAppId, tokenScope);

                        if (!TextUtils.isEmpty(token)) {
                            Log.d("TOKEN", token);
                            sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_PUSH_TOKEN, token);
                            DevinoSdk.getInstance().appStarted();
                            registerUser(email, phone);
                        }
                    } catch (ApiException e) {
                        logsCallback.onMessageLogged("Push Kit Error: " + e.getMessage());
                    }
                }
            }.start();
        }
    }

    private void registerUser(String email, String phone) {

        trackSubscription(networkRepository.registerUser(email, phone)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        json -> {
                            sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_TOKEN_REGISTERED, true);
                            logsCallback.onMessageLogged(event + json.toString());
                        },
                        throwable -> {
                            if (throwable instanceof HttpException)
                                logsCallback.onMessageLogged(getErrorMessage(event, ((HttpException) throwable)));
                            else
                                logsCallback.onMessageLogged(event + throwable.getMessage());
                        }
                )
        );
    }
}
