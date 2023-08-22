package com.devinotele.huaweidevinosdk.sdk;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.JsonObject;
import com.huawei.agconnect.AGConnectOptions;
import com.huawei.hms.aaid.HmsInstanceId;

import java.util.HashMap;

import io.reactivex.Observable;

/**
 * Main library class.
 * Init this class properly
 * <p>
 * Get instance of a this class via DevinoSdk.getInstance()
 */
@SuppressWarnings("unused")
public class DevinoSdk {

    private static DevinoSdk instance;
    private String applicationKey;
    private String applicationId;
    private String appVersion;
    private Boolean isInitedProperly;
    private HelpersPackage hp;
    private HmsInstanceId hmsInstanceId;
    private AGConnectOptions connectOptions;
    private Integer geoFrequency;
    private Integer geoMode;
    private DevinoLogsCallback logsCallback = getEmptyCallback();
    private Uri customSound;

    public static synchronized DevinoSdk getInstance() throws IllegalStateException {
        if (instance != null) return instance;
        else
            throw new IllegalStateException("Devino SDK was not initialized properly. Use DevinoSdk.Builder to init SDK.");
    }

    /**
     * Class builder
     */
    public static class Builder {

        Context ctx;
        String key, applicationId, appVersion;
        HmsInstanceId hmsInstanceId;
        AGConnectOptions connectOptions;

        public Builder(
                Context ctx,
                String key,
                String applicationId,
                String appVersion,
                HmsInstanceId hmsInstanceId,
                AGConnectOptions connectOptions
        ) {
            this.ctx = ctx;
            this.key = key;
            this.applicationId = applicationId;
            this.appVersion = appVersion;
            this.hmsInstanceId = hmsInstanceId;
            this.connectOptions = connectOptions;
            instance = new DevinoSdk();
        }

        public void build() {
            instance.applicationKey = key;
            instance.applicationId = applicationId;
            instance.appVersion = appVersion;
            instance.connectOptions = connectOptions;
            instance.hp = new HelpersPackage();
            instance.hp.setSharedPrefsHelper(new SharedPrefsHelper(
                    ctx.getSharedPreferences("", Context.MODE_PRIVATE)
            ));
            instance.hp.setNotificationsHelper(new NotificationsHelper(ctx));
            instance.hp.setDevinoLocationHelper(new DevinoLocationHelper(ctx));
            instance.hmsInstanceId = hmsInstanceId;
            instance.isInitedProperly = true;
            instance.hp.getSharedPrefsHelper().saveData(SharedPrefsHelper.KEY_API_SECRET, key);
            instance.hp.setNetworkRepository(new DevinoNetworkRepositoryImpl(
                            instance.applicationKey,
                            instance.applicationId,
                            instance.hp.getSharedPrefsHelper()
                                    .getString(SharedPrefsHelper.KEY_PUSH_TOKEN),
                            instance.logsCallback
                    )
            );
            instance.saveToken(ctx, instance.hmsInstanceId, instance.logsCallback);
        }

        /**
         * Set a callback to get library messages
         * callback may also be set up anytime via requestLogs() function
         * Use unsubscribeLogs() function to unsubscribe.
         */
        public Builder setLogsCallback(DevinoLogsCallback callback) {
            instance.logsCallback = callback;
            instance.logsCallback.onMessageLogged("Logs are enabled.");
            return this;
        }
    }

    /**
     * Register callback to get library messages
     *
     * @param callback messages are dispatched to this callback
     */
    public void requestLogs(DevinoLogsCallback callback) {
        getInstance().logsCallback = callback;
        instance.logsCallback.onMessageLogged("Logs are enabled.");
    }

    /**
     * Stop getting messages to previously registered callback
     */
    public void unsubscribeLogs() {
        logsCallback.onMessageLogged("Logs are disabled.");
        logsCallback = getEmptyCallback();
    }

    /**
     * Update user data (data will be bound to current push token)
     *
     * @param phone user phone
     * @param email user email
     */
    public void register(Context context, String phone, String email) {
        handleToken(instance.connectOptions, instance.hmsInstanceId, logsCallback, phone, email);
    }

    /**
     * Notify devino that an app has started
     */
    public void appStarted() {
        AppStartedUseCase useCase = new AppStartedUseCase(instance.hp, logsCallback);
        useCase.run(appVersion);
    }

    /**
     * Send any custom event
     *
     * @param eventName Event name
     * @param eventData Key-Value typed data
     */
    public void sendEvent(String eventName, HashMap<String, Object> eventData) {
        CustomEventUseCase useCase = new CustomEventUseCase(instance.hp, logsCallback);
        useCase.run(eventName, eventData);
    }

    /**
     * Send location data
     *
     * @param latitude  -
     * @param longitude -
     */
    public void sendGeo(Double latitude, Double longitude) {
        SendGeoUseCase useCase = new SendGeoUseCase(instance.hp, logsCallback);
        useCase.run(latitude, longitude);
    }

    /**
     * Send location data (lat, lng are taken from system.
     */
    public void sendCurrentGeo() {
        SendLocationUseCase useCase = new SendLocationUseCase(instance.hp, logsCallback);
        useCase.run();
    }

    /**
     * Report push event
     *
     * @param pushId     -
     * @param actionType Use DevinoSdk.PushStatus enum to send correct value
     * @param actionId   Any value or null
     */
    public void pushEvent(String pushId, String actionType, String actionId) {
        PushEventUseCase useCase = new PushEventUseCase(instance.hp, logsCallback);
        useCase.run(pushId, actionType, actionId);
    }

    /**
     * Report server if app is subscribed for push messages
     *
     * @param subscribed true or false
     */
    public void activateSubscription(Boolean subscribed) {
        ChangeSubscriptionUseCase useCase = new ChangeSubscriptionUseCase(instance.hp, logsCallback);
        useCase.run(subscribed);
    }

    /**
     * Check subscription status
     *
     * @return Observable<JsonObject> subscription status in success json { "result": boolean }
     */
    public Observable<JsonObject> checkSubscription() {
        SubscriptionStatusUseCase useCase = new SubscriptionStatusUseCase(instance.hp, logsCallback);
        return useCase.run();
    }

    /**
     * Shows UI dialog requesting user geo permission
     *
     * @param activity    Calling activity
     * @param requestCode specify code to handle result in onRequestPermissionsResult() method of your Activity
     */
    public void requestGeoPermission(Activity activity, int requestCode) {
        RequestGeoPermissionUseCase useCase = new RequestGeoPermissionUseCase(instance.hp, logsCallback);
        useCase.run(activity, requestCode);
    }

    /**
     * Sends user location repeatedly in given interval (minutes)
     * Updates stop on phone reboot (you need to call this function once again after reboot)
     * <p>
     * It is not guaranteed that location is sent in every case. Some Android OS versions may restrict that
     * or GPS may be disabled.
     * Also Android OS may reschedule this tasks. And if you set 10 minutes interval some messages
     * will be sent after 10 minutes, some after 12 (or whatever OS will decide)
     *
     * @param context         -
     * @param intervalMinutes -
     */
    public void subscribeGeo(Context context, int intervalMinutes) {
        unsubscribeGeo(context);
        SubscribeLocationsUseCase useCase = new SubscribeLocationsUseCase(instance.hp, logsCallback);
        useCase.run(context, intervalMinutes);
    }

    /**
     * Stops sending locations
     *
     * @param context -
     */
    public void unsubscribeGeo(Context context) {
        UnsubscribeLocationsUseCase useCase = new UnsubscribeLocationsUseCase(instance.hp, logsCallback);
        useCase.run(context);
    }

    /**
     * Sets custom sound
     * Might not work (or work unexpectedly) on some Android versions
     *
     * @param sound -
     */
    public void setCustomSound(Uri sound) {
        instance.customSound = sound;
    }

    /**
     * Use default sound for notifications
     */
    public void useDefaultSound() {
        customSound = null;
    }

    /**
     * Cancel all unfinished requests
     * Some network requests are retried few times when failed.
     * It is highly recommended that you call this function when activity (fragment) is destroyed (paused)
     */
    public void stop() {
        BaseUC.unsubscribeAll();
    }

    /**
     * Set default deep link for notification
     */
    public void setDefaultDeepLinkAction(String scheme, String host) {
        DevinoPushReceiver.KEY_DEFAULT_ACTION = scheme + "://" + host;
    }

    /**
     * Set default small icon for notification
     */
    public void setDefaultNotificationIcon(int icon) {
        DevinoSdkPushService.defaultNotificationIcon = icon;
    }

    /**
     * Set default color of notification small icon
     *
     * @param color color of icon
     */
    public void setDefaultNotificationIconColor(@ColorInt int color) {
        DevinoSdkPushService.defaultNotificationIconColor = color;
    }

    /**
     * Shows UI dialog requesting user notification permission
     *
     * @param activity    Calling activity
     * @param requestCode specify code to handle result in onRequestPermissionsResult() method of your Activity
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public void requestNotificationPermission(Activity activity, Integer requestCode) {
        RequestNotificationPermissionUseCase useCase =
                new RequestNotificationPermissionUseCase(instance.hp, logsCallback);
        useCase.run(activity, requestCode);
    }

    /**
     * Shows UI dialog requesting user geo and notification permissions
     *
     * @param activity    Calling activity
     * @param requestCode specify code to handle result in onRequestPermissionsResult() method of your Activity
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public void requestGeoAndNotificationPermissions(Activity activity, int requestCode) {
        RequestGeoAndNotificationPermissionUseCase useCase =
                new RequestGeoAndNotificationPermissionUseCase(instance.hp, logsCallback);
        useCase.run(activity, requestCode);
    }

    /**
     * Update base api url
     *
     * @param newBaseApiUrl New base api url
     */
    public void updateBaseApiUrl(@NonNull String newBaseApiUrl, Context ctx) {
        UpdateApiBaseUrlUseCase useCase = new UpdateApiBaseUrlUseCase(instance.hp, logsCallback);
        useCase.run(newBaseApiUrl, ctx);

    }

    public AGConnectOptions getAGConnectOptionsInstance() {
        return instance.connectOptions;
    }

    public void updateToken(String pushToken) {
        SaveTokenUseCase useCase = new SaveTokenUseCase(instance.hp, logsCallback);
        useCase.run(pushToken);
    }

    protected String getSavedBaseUrl() {
        return instance.hp.getSharedPrefsHelper().getString(SharedPrefsHelper.KEY_API_BASE_URL);
    }

    void hideNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }

    Uri getSound() {
        return instance.customSound;
    }

    private static void handleToken(AGConnectOptions connectOptions, HmsInstanceId hmsInstanceId,
                                    DevinoLogsCallback callback, String phone, String email) {
        HandleTokenUseCase useCase = new HandleTokenUseCase(instance.hp, callback, phone, email);
        useCase.run(connectOptions, hmsInstanceId);
    }

    private boolean isRegistered() {
        return instance.hp.getSharedPrefsHelper().getBoolean(SharedPrefsHelper.KEY_TOKEN_REGISTERED);
    }

    private void saveToken(Context context, HmsInstanceId hmsInstanceId, DevinoLogsCallback callback) {
        SaveTokenUseCase useCase = new SaveTokenUseCase(instance.hp, callback);
        useCase.run(connectOptions, hmsInstanceId);
    }

    private DevinoLogsCallback getEmptyCallback() {
        return message -> System.out.println("Devino SDK event (logs are disabled).");
    }

    protected void saveCustomDataFromPushJson(@NonNull String customData) {
        SaveCustomDataHashMapUseCase useCase = new SaveCustomDataHashMapUseCase(instance.hp, logsCallback);
        useCase.run(customData);
    }

    static class PushStatus {
        static final String DELIVERED = "DELIVERED";
        static final String OPENED = "OPENED";
        static final String CANCELED = "CANCELED";
    }
}