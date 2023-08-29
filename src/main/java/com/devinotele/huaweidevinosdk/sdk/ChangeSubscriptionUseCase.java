package com.devinotele.huaweidevinosdk.sdk;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

class ChangeSubscriptionUseCase extends BaseUC {

    private final DevinoLogsCallback logsCallback;
    private final String eventTemplate = "Set subscribed (%s)";

    ChangeSubscriptionUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    void run(Boolean subscribed) {
        String token = sharedPrefsHelper.getString(SharedPrefsHelper.KEY_PUSH_TOKEN);
        HashMap<String, Object> customData =
                sharedPrefsHelper.getHashMap(SharedPrefsHelper.KEY_CUSTOM_DATA);
        sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_SUBSCRIBED, subscribed);
        if (token.length() > 0) {
            trackSubscription(networkRepository.changeSubscription(subscribed, customData)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            json -> logsCallback.onMessageLogged(
                                    String.format(
                                            eventTemplate,
                                            subscribed.toString()) + " -> " + json.toString()
                            ),
                            throwable -> {
                                if (throwable instanceof HttpException)
                                    logsCallback.onMessageLogged(
                                            getErrorMessage(
                                                    String.format(
                                                            eventTemplate,
                                                            subscribed.toString()
                                                    ),
                                                    ((HttpException) throwable))
                                    );
                                else
                                    logsCallback.onMessageLogged(
                                            String.format(
                                                    eventTemplate,
                                                    subscribed.toString()
                                            ) + " -> " + throwable.getMessage()
                                    );
                            }
                    )
            );
        }
        else {
            logsCallback.onMessageLogged("Can't set subscribed -> token not registered");
            logsCallback.onMessageLogged("The token was requested again.");
            DevinoSdk.getInstance().saveToken();
        }
    }
}