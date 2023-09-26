package com.devinotele.huaweidevinosdk.sdk;

import java.util.HashMap;

import retrofit2.HttpException;

class SendLocationUseCase extends BaseUC {

    private final DevinoLogsCallback logsCallback;
    private final String event = "Send geo: ";
    private final RetrofitClientInstance retrofitClientInstance;

    SendLocationUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
        retrofitClientInstance = new RetrofitClientInstance();
    }

    void run() {
        HashMap<String, Object> customData =
                sharedPrefsHelper.getHashMap(SharedPrefsHelper.KEY_CUSTOM_DATA);
        trackSubscription(
                devinoLocationHelper.getNewLocation()
                        .flatMap(location ->
                                networkRepository.geo(
                                        location.getLatitude(),
                                        location.getLongitude(),
                                        customData
                                )
                        )
                        .subscribe(
                                json -> {
                                    logsCallback.onMessageLogged(event
                                            + retrofitClientInstance.getCurrentRequestUrl()
                                            + " -> "
                                            + json.toString()
                                    );
                                },
                                throwable -> {
                                    if (throwable instanceof HttpException)
                                        logsCallback.onMessageLogged(
                                                getErrorMessage(
                                                        event
                                                                + retrofitClientInstance.getCurrentRequestUrl()
                                                                + " -> ",
                                                        ((HttpException) throwable)
                                                )
                                        );
                                    else
                                        logsCallback.onMessageLogged(
                                                event
                                                        + retrofitClientInstance.getCurrentRequestUrl()
                                                        + " -> "
                                                        + throwable.getMessage()
                                        );
                                }
                        )
        );
    }
}