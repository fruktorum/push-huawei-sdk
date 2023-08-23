package com.devinotele.huaweidevinosdk.sdk;

import com.google.gson.JsonObject;

import java.util.HashMap;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface DevinoApi {

    @PUT("users/{pushToken}/data")
    Single<JsonObject> registerUser(
            @Path("pushToken") String pushToken,
            @Body HashMap<String, Object> body
    );

    @POST("users/{pushToken}/app-start")
    Single<JsonObject> appStart(
            @Path("pushToken") String pushToken,
            @Body HashMap<String, Object> body
    );

    @POST("users/{pushToken}/event")
    Single<JsonObject> event(
            @Path("pushToken") String pushToken,
            @Body HashMap<String, Object> body
    );

    @POST("users/{pushToken}/geo")
    Single<JsonObject> geo(
            @Path("pushToken") String pushToken,
            @Body HashMap<String, Object> body
    );

    @POST("users/{pushToken}/subscription")
    Single<JsonObject> subscription(
            @Path("pushToken") String pushToken,
            @Body HashMap<String, Object> body
    );

    @GET("users/{pushToken}/subscription/status")
    Single<JsonObject> getSubscriptionStatus(
            @Path("pushToken") String token,
            @Query("applicationId") String applicationId
    );

    @POST("messages/events")
    Single<JsonObject> pushEvent(@Body HashMap<String, Object> body);
}