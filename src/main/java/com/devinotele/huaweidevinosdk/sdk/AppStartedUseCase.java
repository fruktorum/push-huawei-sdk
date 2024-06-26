package com.devinotele.huaweidevinosdk.sdk;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

class AppStartedUseCase extends BaseUC {

    private final DevinoLogsCallback logsCallback;
    private final String event = "App started: ";
    private final RetrofitClientInstance retrofitClientInstance;

    AppStartedUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
        retrofitClientInstance = new RetrofitClientInstance();
    }

    public void run(String appVersion) {
        String token = sharedPrefsHelper.getString(SharedPrefsHelper.KEY_PUSH_TOKEN);
        if (token.length() > 1) {
            Boolean subscribed = sharedPrefsHelper.getBoolean(SharedPrefsHelper.KEY_SUBSCRIBED);
            HashMap<String, Object> customData =
                    sharedPrefsHelper.getHashMap(SharedPrefsHelper.KEY_CUSTOM_DATA);
            trackSubscription(networkRepository.appStarted(appVersion, subscribed, customData)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            json -> logsCallback.onMessageLogged(
                                    event
                                            + retrofitClientInstance.getCurrentRequestUrl()
                                            + " -> "
                                            + json.toString()
                            ),
                            throwable -> {
                                if (throwable instanceof HttpException)
                                    logsCallback.onMessageLogged(
                                            getErrorMessage(event
                                                    + retrofitClientInstance.getCurrentRequestUrl(),
                                                    ((HttpException) throwable))
                                    );
                                else
                                    logsCallback.onMessageLogged(
                                            event
                                                    + retrofitClientInstance.getCurrentRequestUrl()
                                                    + throwable.getMessage()
                                    );
                            }
                    )
            );
        } else {
            logsCallback.onMessageLogged("Application has no push token yet");
            logsCallback.onMessageLogged("The token was requested again.");
            DevinoSdk.getInstance().saveToken();
        }
    }
}