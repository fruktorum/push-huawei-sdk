package com.devinotele.huaweidevinosdk.sdk;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

class RetrofitClientInstance {

    private static Retrofit retrofit;
    private static volatile String BASE_URL = "https://integrationapi.net/push/sdk/";
    private static final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    public void setApiBaseUrl(String newApiBaseUrl) {
        BASE_URL = newApiBaseUrl;
        if (retrofit != null) retrofit = retrofit.newBuilder().baseUrl(BASE_URL).build();
    }

    static Retrofit getRetrofitInstance(final String apiKey) {
        if (retrofit == null) {

            String url = DevinoSdk.getInstance().getSavedBaseUrl();
            if (url != null && !url.isEmpty()) {
                BASE_URL = url;
            }

            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("x-api-key", apiKey)
                        .header("Content-Type", "application/json")
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            });

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}