package com.devinotele.huaweidevinosdk.sdk;

import android.util.Log;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

class PushEventUseCase extends BaseUC {

    private final DevinoLogsCallback logsCallback;
    private final String eventTemplate = "Push event (%s, %s, %s): ";
    private final RetrofitClientInstance retrofitClientInstance;

    PushEventUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
        retrofitClientInstance = new RetrofitClientInstance();
    }

    void run(String pushId, String actionType, String actionId) {
        String token = sharedPrefsHelper.getString(SharedPrefsHelper.KEY_PUSH_TOKEN);
        Log.d("DevinoPush", "PushEventUseCase: " + token);
        HashMap<String, Object> customData =
                sharedPrefsHelper.getHashMap(SharedPrefsHelper.KEY_CUSTOM_DATA);
        if (token.length() > 0) {
            trackSubscription(networkRepository.pushEvent(pushId, actionType, actionId, customData)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            json -> {
                                logsCallback.onMessageLogged(
                                        String.format(
                                                eventTemplate
                                                        + retrofitClientInstance.getCurrentRequestUrl(),
                                                pushId,
                                                actionType,
                                                actionId
                                        )
                                                + " -> "
                                                + json.toString()
                                );
                            },
                            throwable -> {
                                if (throwable instanceof HttpException)
                                    logsCallback.onMessageLogged(
                                            getErrorMessage(
                                                    String.format(
                                                            eventTemplate
                                                                    + retrofitClientInstance.getCurrentRequestUrl(),
                                                            pushId,
                                                            actionType,
                                                            actionId
                                                    )
                                                            + " -> ",
                                                    ((HttpException) throwable)
                                            )
                                    );
                                else
                                    logsCallback.onMessageLogged(
                                            String.format(
                                                    eventTemplate
                                                            + retrofitClientInstance.getCurrentRequestUrl(),
                                                    pushId,
                                                    actionType,
                                                    actionId
                                            )
                                                    + " -> "
                                                    + throwable.getMessage()
                                    );
                            }
                    )
            );
        } else {
            logsCallback.onMessageLogged("Can't send push event -> token not registered");
            logsCallback.onMessageLogged("The token was requested again.");
            DevinoSdk.getInstance().saveToken();
        }
    }
}