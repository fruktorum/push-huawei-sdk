package com.devinotele.huaweidevinosdk.sdk;

import android.util.SparseIntArray;

import com.google.gson.JsonObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

class DevinoNetworkRepositoryImpl implements DevinoNetworkRepository {

    private final RetrofitHelper retrofitHelper;
    private final DevinoLogsCallback callback;
    SparseIntArray retryMap = new SparseIntArray();

    DevinoNetworkRepositoryImpl(
            String apiKey,
            String applicationId,
            String token,
            DevinoLogsCallback callback
    ) {
        retrofitHelper = new RetrofitHelper(apiKey, applicationId, token);
        this.callback = callback;
    }

    private final long[] intervals = {1, 1, 5, 5, 10, 30};

    private <T> Observable<T> retryOnHttpError(Observable<T> source) {
        retryMap.put(source.hashCode(), 0);
        return source.retryWhen(errors ->
                errors.flatMap(error -> {
                    boolean retryCondition =
                            (error instanceof HttpException
                                    && codeToRepeat(((HttpException) error).code())
                            ) || error instanceof SocketTimeoutException
                            || error instanceof ConnectException
                            || error instanceof UnknownHostException;

                    int retryCount = retryMap.get(source.hashCode(), intervals.length);

                    if (retryCount < intervals.length && retryCondition) {
                        callback.onMessageLogged(
                                "Retrying to connect: try $retryCount Cause: ${error.message!!}"
                        );
                        retryMap.put(source.hashCode(), retryCount + 1);
                        return Observable.timer(
                                intervals[retryCount],
                                TimeUnit.SECONDS
                        );
                    }
                    return Observable.error(error);
                })
        );
    }

    private Boolean codeToRepeat(int errorCode) {
        return errorCode != 200 && !(errorCode >= 400 && errorCode <= 404);
    }

    private <T> Observable<T> retryOnHttpError(Single<T> source) {
        return retryOnHttpError(source.toObservable());
    }

    @Override
    public Observable<JsonObject> registerUser(
            String email,
            String phone,
            HashMap<String, Object> customData
    ) {
        return retryOnHttpError(retrofitHelper.registerUser(email, phone, customData));
    }

    @Override
    public Observable<JsonObject> changeSubscription(
            Boolean subscribed,
            HashMap<String, Object> customData
    ) {
        return retryOnHttpError(retrofitHelper.changeSubscription(subscribed, customData));
    }

    @Override
    public Observable<JsonObject> getSubscriptionStatus() {
        return retryOnHttpError(retrofitHelper.getSubscriptionStatus());
    }

    @Override
    public Observable<JsonObject> appStarted(
            String appVersion,
            Boolean subscribed,
            HashMap<String, Object> customData) {
        return retryOnHttpError(retrofitHelper.appStarted(subscribed, appVersion, customData));
    }

    @Override
    public Observable<JsonObject> customEvent(
            String eventName,
            HashMap<String, Object> eventData,
            HashMap<String, Object> customData) {
        return retryOnHttpError(retrofitHelper.customEvent(eventName, eventData, customData));
    }

    @Override
    public Single<JsonObject> geo(
            Double latitude,
            Double longitude,
            HashMap<String, Object> customData
    ) {
        return retrofitHelper.geo(latitude, longitude, customData)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<JsonObject> pushEvent(
            String pushId,
            String actionType,
            String actionId,
            HashMap<String, Object> customData
    ) {
        return retryOnHttpError(retrofitHelper.pushEvent(pushId, actionType, actionId, customData));
    }

    @Override
    public void updateToken(String token) {
        retrofitHelper.setToken(token);
    }
}