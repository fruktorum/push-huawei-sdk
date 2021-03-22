package com.devinotele.devinosdk.sdk;

import com.google.gson.JsonObject;

import io.reactivex.Observable;
import retrofit2.HttpException;

class SubscriptionStatusUseCase extends BaseUC {

    private DevinoLogsCallback logsCallback;
    private String eventTemplate = "subscription status ";

    SubscriptionStatusUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    Observable<JsonObject> run() {
        String token = sharedPrefsHelper.getString(SharedPrefsHelper.KEY_PUSH_TOKEN);

        if (token.length() > 0) {
            return networkRepository.getSubscriptionStatus()
                    .doOnNext(json -> logsCallback.onMessageLogged(eventTemplate + " -> " + json.toString()))
                    .doOnError(throwable -> {
                        if (throwable instanceof HttpException)
                            logsCallback.onMessageLogged(getErrorMessage(eventTemplate, ((HttpException) throwable)));
                        else
                            logsCallback.onMessageLogged(eventTemplate + " -> " + throwable.getMessage());

                    });
        }

        String errorMessage = "can't get subscription status -> token not registered";
        logsCallback.onMessageLogged(errorMessage);
        return Observable.error(new IllegalArgumentException(errorMessage));
    }
}
