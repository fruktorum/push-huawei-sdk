package com.devinotele.huaweidevinosdk.sdk;

import android.annotation.SuppressLint;
import android.os.Build;

import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import io.reactivex.Single;

class RetrofitHelper {

    private DevinoApi devinoApi;
    private final String applicationId;
    private String token;
    static final String PLATFORM_KEY = "HUAWEI";

    private final String apiKey;

    RetrofitHelper(String apiKey, String applicationId, String token) {
        this.apiKey = apiKey;

        createDevinoApi();
        this.applicationId = applicationId;
        this.token = token;
    }

    private void createDevinoApi() {
        devinoApi = RetrofitClientInstance.getRetrofitInstance(apiKey).create(DevinoApi.class);
    }

    void updateBaseUrl() {
        createDevinoApi();
    }

    void setToken(String token) {
        this.token = token;
    }

    Single<JsonObject> registerUser(
            String email,
            String phone,
            HashMap<String, Object> customData
    ) {
        HashMap<String, Object> body = getGenericBody();
        body.put("email", email);
        body.put("phone", phone);
        body.put("platform", PLATFORM_KEY);
        if (customData != null) {
            body.put("customData", customData);
        }
        return devinoApi.registerUser(token, body);
    }

    Single<JsonObject> changeSubscription(Boolean subscribed, HashMap<String, Object> customData) {
        HashMap<String, Object> body = getGenericBody();
        body.put("subscribed", subscribed);
        body.put("platform", PLATFORM_KEY);
        if (customData != null) {
            body.put("customData", customData);
        }
        return devinoApi.subscription(token, body);
    }

    Single<JsonObject> getSubscriptionStatus() {
        return devinoApi.getSubscriptionStatus(token, applicationId);
    }

    Single<JsonObject> appStarted(
            Boolean subscribed,
            String appVersion,
            HashMap<String, Object> customData
    ) {
        HashMap<String, Object> body = getGenericBody();
        body = addCustomData(body);
        body.put("appVersion", appVersion);
        body.put("subscribed", subscribed);
        if (customData != null) {
            body.put("customData", customData);
        }
        return devinoApi.appStart(token, body);
    }

    Single<JsonObject> customEvent(
            String eventName,
            HashMap<String, Object> eventData,
            HashMap<String, Object> customData
    ) {
        HashMap<String, Object> body = getGenericBody();
        body.put("eventName", eventName);
        body.put("eventData", eventData);
        body.put("platform", PLATFORM_KEY);
        if (customData != null) {
            body.put("customData", customData);
        }
        return devinoApi.event(token, body);
    }

    Single<JsonObject> geo(Double latitude, Double longitude, HashMap<String, Object> customData) {
        HashMap<String, Object> body = getGenericBody();
        body.put("latitude", latitude);
        body.put("longitude", longitude);
        body.put("platform", PLATFORM_KEY);
        if (customData != null) {
            body.put("customData", customData);
        }
        return devinoApi.geo(token, body);
    }

    Single<JsonObject> pushEvent(
            String pushId,
            String actionType,
            String actionId,
            HashMap<String, Object> customData
    ) {
        HashMap<String, Object> body = getGenericBody();
        body.put("pushToken", token);
        body.put("pushId", pushId);
        body.put("actionType", actionType);
        body.put("actionId", actionId);
        body.put("platform", PLATFORM_KEY);
        if (customData != null) {
            body.put("customData", customData);
        }
        return devinoApi.pushEvent(body);
    }

    @SuppressLint("SimpleDateFormat")
    private String getTimestamp() {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }

    HashMap<String, Object> getGenericBody() {
        HashMap<String, Object> body = new HashMap<>();
        body.put("reportedDateTimeUtc", getTimestamp());
        body.put("applicationId", applicationId);
        return body;
    }

    HashMap<String, Object> addCustomData(HashMap<String, Object> body) {
        body.put("platform", PLATFORM_KEY);
        body.put("osVersion", String.valueOf(Build.VERSION.SDK_INT));
        body.put("language", Locale.getDefault().getISO3Language().substring(0, 2));
        return body;
    }
}