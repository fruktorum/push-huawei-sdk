package com.devinotele.huaweidevinosdk.sdk;

import com.google.gson.JsonObject;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.Single;

interface DevinoNetworkRepository {

    Observable<JsonObject> registerUser(
            String email,
            String phone,
            HashMap<String, Object> customData
    );

    Observable<JsonObject> changeSubscription(
            Boolean subscribed,
            HashMap<String, Object> customData);

    Observable<JsonObject> getSubscriptionStatus();

    Observable<JsonObject> appStarted(
            String appVersion,
            Boolean subscribed,
            HashMap<String, Object> customData);

    Observable<JsonObject> customEvent(
            String eventName,
            HashMap<String, Object> eventData,
            HashMap<String, Object> customData);

    Single<JsonObject> geo(
            Double latitude,
            Double longitude,
            HashMap<String, Object> customData);

    Observable<JsonObject> pushEvent(
            String pushId,
            String actionType,
            String actionId,
            HashMap<String, Object> customData);

    void updateToken(String token);
}