package com.devinotele.devinosdk.sdk;


import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;


class PushEventUseCase extends BaseUC {

    private DevinoLogsCallback logsCallback;
    private String eventTemplate = "push event (%s, %s, %s) ";

    PushEventUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    void run(String pushId, String actionType, String actionId) {
        String token = sharedPrefsHelper.getString(SharedPrefsHelper.KEY_PUSH_TOKEN);
        if (token.length() > 0)
            trackSubscription(networkRepository.pushEvent(pushId, actionType, actionId)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            json -> {
                                logsCallback.onMessageLogged(String.format(eventTemplate, pushId, actionType, actionId) + json.toString());
                            },
                            throwable -> {
                                if (throwable instanceof HttpException)
                                    logsCallback.onMessageLogged(getErrorMessage(String.format(eventTemplate, pushId, actionType, actionId), ((HttpException) throwable)));
                                else
                                    logsCallback.onMessageLogged(String.format(eventTemplate, pushId, actionType, actionId) + throwable.getMessage());
                            }
                    )
            );

        else logsCallback.onMessageLogged("can't send push event -> token not registered");
    }

}
