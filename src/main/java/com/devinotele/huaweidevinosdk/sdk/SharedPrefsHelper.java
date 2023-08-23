package com.devinotele.huaweidevinosdk.sdk;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

class SharedPrefsHelper {

    private static SharedPreferences sharedPreferences;
    static final String KEY_TOKEN_REGISTERED = "TokenRegistered";
    static final String KEY_PUSH_TOKEN = "PushToken";
    static final String KEY_SUBSCRIBED = "Subscribed";
    static final String KEY_API_SECRET = "ApiSecret";
    static final String KEY_GPS_INTERVAL = "GpsInterval";
    static final String KEY_API_BASE_URL = "ApiBaseUrl";
    static final String KEY_CUSTOM_DATA = "CustomData";
    static final String KEY_GPS_SUBSCRIPTION_ACTIVE = "GpsSubscriptionActive";

    public SharedPrefsHelper(SharedPreferences sp) {
        sharedPreferences = sp;
    }

    void saveData(String key, String data) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(key, data);
        editor.commit();
    }

    void saveData(String key, Integer data) {
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(key, data);
        editor.commit();
    }

    void saveData(String key, Boolean data) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(key, data);
        editor.commit();
    }

    void saveData(String key, Float data) {
        SharedPreferences.Editor editor = getEditor();
        editor.putFloat(key, data);
        editor.commit();
    }

    void saveData (String key, HashMap<String, Object> hashMap) {
        //convert to string using gson
        Gson gson = new Gson();
        String hashMapString = gson.toJson(hashMap);
        //save in shared prefs
        saveData(key, hashMapString);
    }

    String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    Integer getInteger(String key) {
        return sharedPreferences.getInt(key, -31);
    }

    Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, true);
    }

    Float getFloat(String key) {
        return sharedPreferences.getFloat(key, -0.31f);
    }

    HashMap<String, Object> getHashMap (String key) {
        //get from shared prefs
        String storedHashMapString = getString(key);
        Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
        Gson gson = new Gson();
        return gson.fromJson(storedHashMapString, type);
    }

    private SharedPreferences.Editor getEditor() {
        return sharedPreferences.edit();
    }
}