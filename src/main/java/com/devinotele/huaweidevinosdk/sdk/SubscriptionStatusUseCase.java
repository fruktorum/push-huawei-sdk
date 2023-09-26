package com.devinotele.huaweidevinosdk.sdk;

import com.google.gson.JsonObject;

import io.reactivex.Observable;
import retrofit2.HttpException;

class SubscriptionStatusUseCase extends BaseUC {

    private final DevinoLogsCallback logsCallback;
    private final String eventTemplate = "Subscription status: ";
    private final RetrofitClientInstance retrofitClientInstance;

    SubscriptionStatusUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
        retrofitClientInstance = new RetrofitClientInstance();
    }

    Observable<JsonObject> run() {
        String token = sharedPrefsHelper.getString(SharedPrefsHelper.KEY_PUSH_TOKEN);

        if (token.length() > 0) {
            return networkRepository.getSubscriptionStatus()
                    .doOnNext(
                            json ->
                                    logsCallback.onMessageLogged(
                                            eventTemplate
                                                    + retrofitClientInstance.getCurrentRequestUrl()
                                                    + " -> "
                                                    + json.toString()
                                    )
                    )
                    .doOnError(throwable -> {
                        if (throwable instanceof HttpException)
                            logsCallback.onMessageLogged(
                                    getErrorMessage(
                                            eventTemplate
                                                    + retrofitClientInstance.getCurrentRequestUrl(),
                                            ((HttpException) throwable)
                                    )
                            );
                        else
                            logsCallback.onMessageLogged(
                                    eventTemplate
                                            + retrofitClientInstance.getCurrentRequestUrl()
                                            + " -> "
                                            + throwable.getMessage()
                            );
                    });
        }
        String errorMessage = "Can't get subscription status -> token not registered";
        logsCallback.onMessageLogged("The token was requested again.");
        DevinoSdk.getInstance().saveToken();
        return Observable.error(new IllegalArgumentException(errorMessage));
    }
}