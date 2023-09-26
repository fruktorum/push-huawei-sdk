package com.devinotele.huaweidevinosdk.sdk;

import android.util.Log;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

class SendGeoUseCase extends BaseUC {

    private final DevinoLogsCallback logsCallback;
    private final String eventTemplate = "Geo (%s, %s): ";
    private final RetrofitClientInstance retrofitClientInstance;

    SendGeoUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
        retrofitClientInstance = new RetrofitClientInstance();
    }

    void run(Double latitude, Double longitude) {
        Log.d("DevinoPush", "SendGeoUseCase latitude=" + latitude);
        Log.d("DevinoPush", "SendGeoUseCase longitude=" + longitude);
        String token = sharedPrefsHelper.getString(SharedPrefsHelper.KEY_PUSH_TOKEN);
        HashMap<String, Object> customData =
                sharedPrefsHelper.getHashMap(SharedPrefsHelper.KEY_CUSTOM_DATA);
        if (token.length() > 0) {
            trackSubscription(networkRepository.geo(latitude, longitude, customData)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            json -> {
                                logsCallback.onMessageLogged(
                                        String.format(
                                                eventTemplate
                                                        + retrofitClientInstance.getCurrentRequestUrl(),
                                                latitude,
                                                longitude
                                        )
                                                + " -> "
                                                + json.toString()
                                );
                                Log.d("DevinoPush", "SendGeoUseCase json="+json);
                            },
                            throwable -> {
                                if (throwable instanceof HttpException)
                                    logsCallback.onMessageLogged(
                                            getErrorMessage(
                                                    String.format(
                                                            eventTemplate
                                                                    + retrofitClientInstance.getCurrentRequestUrl(),
                                                            latitude,
                                                            longitude
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
                                                    latitude,
                                                    longitude
                                            )
                                                    + " -> "
                                                    + throwable.getMessage()
                                    );
                            }
                    )
            );
        }
        else {
            logsCallback.onMessageLogged("Can't send geo -> token not registered");
            logsCallback.onMessageLogged("The token was requested again.");
            DevinoSdk.getInstance().saveToken();
        }
    }
}