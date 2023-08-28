package com.devinotele.huaweidevinosdk.sdk;

import java.util.HashMap;

import retrofit2.HttpException;

class SendLocationUseCase extends BaseUC {

    private final DevinoLogsCallback logsCallback;
    private final String event = "Send geo: ";

    SendLocationUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    void run() {
        HashMap<String, Object> customData =
                sharedPrefsHelper.getHashMap(SharedPrefsHelper.KEY_CUSTOM_DATA);
        trackSubscription(
                devinoLocationHelper.getNewLocation()
                        .flatMap(location -> networkRepository.geo(
                                location.getLatitude(),
                                location.getLongitude(),
                                customData)
                        )
                        .subscribe(
                                json -> {
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