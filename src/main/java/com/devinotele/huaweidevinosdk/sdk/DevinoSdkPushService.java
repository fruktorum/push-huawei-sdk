package com.devinotele.huaweidevinosdk.sdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class DevinoSdkPushService extends HmsMessageService {

    Gson gson = new Gson();

    private static final String LOG_TAG = "DevinoPush";
    private final String channelId = "devino_push";
    @DrawableRes
    static Integer defaultNotificationIcon = R.drawable.ic_grey_circle;
    @ColorInt
    static Integer defaultNotificationIconColor = 0x333333;
    private static final int RESOURCE_NOT_FOUND = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (!remoteMessage.getDataOfMap().isEmpty()) {
            Map<String, String> data = remoteMessage.getDataOfMap();
            Log.d(LOG_TAG, "data = " + data);

            String pushId = data.get("pushId");

            if (pushId == null) {
                Log.e(LOG_TAG, "pushId is null");
                return;
            }

            String image = data.get("image");
            String smallIcon = data.get("smallIcon");
            String iconColor = data.get("iconColor");
            String title = data.get("title");
            String body = data.get("body");

            if (title == null || body == null) {
                Log.e(LOG_TAG, "Returning body or title null");
                return;
            }

            String badge = data.get("badge");
            int badgeNumber = 0;

            if (badge != null) {
                badgeNumber = Integer.parseInt(badge);
            }

            Log.d(LOG_TAG, "badgeNumber =  " + badgeNumber);

            String action = data.get("action");
            Log.d(LOG_TAG, "action =  " + action);

            String buttonsJson = data.get("buttons");
            Type listType = new TypeToken<List<PushButton>>() {
            }.getType();
            List<PushButton> buttons = gson.fromJson(buttonsJson, listType);

            String customDataString = data.get("customData");
            if (customDataString != null) {
                DevinoSdk.getInstance().saveCustomDataFromPushJson(customDataString);
                Log.d(LOG_TAG, "CustomDataString =  " + customDataString);
            }

            String sound = data.get("sound");
            Uri soundUri;
            if (sound != null) {
                soundUri = Uri.parse(sound);
            } else {
                soundUri = DevinoSdk.getInstance().getSound();
            }
            Log.d(LOG_TAG, "soundUri =  " + soundUri);

            boolean isSilent = "true".equalsIgnoreCase(data.get("silentPush"));
            if (!isSilent) {
                showSimpleNotification(
                        title,
                        body,
                        smallIcon,
                        iconColor,
                        image,
                        buttons,
                        true,
                        soundUri,//sound,
                        pushId,
                        action,
                        badgeNumber
                );
            } else {
                Log.e(LOG_TAG, "Returning is not silent");
            }

            DevinoSdk.getInstance().pushEvent(pushId, DevinoSdk.PushStatus.DELIVERED, null);
        } else {
            Log.e(LOG_TAG, "Returning data is empty");
        }
    }

    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        DevinoSdk.getInstance().updateToken(token);
    }

    @SuppressLint("NotificationTrampoline")
    public void showSimpleNotification(
            String title,
            String text,
            String smallIcon,
            String iconColor,
            String largeIcon,
            List<PushButton> buttons,
            Boolean bigPicture,
            Uri soundUri,
            String pushId,
            String action,
            Integer badgeNumber
    ) {
        Intent broadcastIntent = new Intent(getApplicationContext(), DevinoPushReceiver.class);
        broadcastIntent.putExtra(DevinoPushReceiver.KEY_PUSH_ID, pushId);
        if (action != null) {
            broadcastIntent.putExtra(DevinoPushReceiver.KEY_DEEPLINK, action);
        } else {
            broadcastIntent.putExtra(DevinoPushReceiver.KEY_DEEPLINK, DevinoPushReceiver.KEY_DEFAULT_ACTION);
        }
        broadcastIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent activityIntent = new Intent(getApplicationContext(), NotificationTrampolineActivity.class);
        activityIntent.putExtra(DevinoPushReceiver.KEY_PUSH_ID, pushId);
        if (action != null) {
            activityIntent.putExtra(DevinoPushReceiver.KEY_DEEPLINK, action);
        } else {
            activityIntent.putExtra(DevinoPushReceiver.KEY_DEEPLINK, DevinoPushReceiver.KEY_DEFAULT_ACTION);
        }
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent deleteIntent = new Intent(getApplicationContext(), DevinoCancelReceiver.class);
        deleteIntent.putExtra(DevinoPushReceiver.KEY_PUSH_ID, pushId);

        PendingIntent defaultPendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                Bundle options = ActivityOptions.makeBasic()
                        .setPendingIntentBackgroundActivityStartMode(
                                ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                        ).toBundle();

                defaultPendingIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        activityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE,
                        options
                );

            } else {
                defaultPendingIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        activityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
                );
            }

        } else {
            defaultPendingIntent = PendingIntent.getBroadcast(
                    getApplicationContext(),
                    broadcastIntent.hashCode(),
                    broadcastIntent,
                    PendingIntent.FLAG_IMMUTABLE
            );
        }

        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                deleteIntent.hashCode(),
                deleteIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        createNotificationChannel(soundUri);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(defaultPendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setChannelId(channelId)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        if (badgeNumber != null && badgeNumber > 0) {
            builder.setNumber(badgeNumber);
        }

        if (smallIcon != null) {
            builder.setSmallIcon(getIconDrawableId(getApplicationContext(), smallIcon));
        } else {
            builder.setSmallIcon(defaultNotificationIcon);
        }

        if (iconColor != null) {
            try {
                int color = Color.parseColor(iconColor);
                builder.setColor(color);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            builder.setColor(defaultNotificationIconColor);
        }

        if (buttons != null && !buttons.isEmpty()) {
            for (PushButton button : buttons) {
                if (button.text != null) {
                    Intent buttonActivityIntent = new Intent(this, NotificationTrampolineActivity.class);
                    buttonActivityIntent.putExtra(DevinoPushReceiver.KEY_DEEPLINK, button.deeplink);
                    buttonActivityIntent.putExtra(DevinoPushReceiver.KEY_PUSH_ID, pushId);
                    buttonActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    Intent buttonBroadcastIntent = new Intent(this, DevinoPushReceiver.class);
                    buttonBroadcastIntent.putExtra(DevinoPushReceiver.KEY_DEEPLINK, button.deeplink);
                    buttonBroadcastIntent.putExtra(DevinoPushReceiver.KEY_PUSH_ID, pushId);
                    buttonBroadcastIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    Log.d(LOG_TAG, "button.deeplink =  " + button.deeplink);
                    PendingIntent pendingIntent;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        pendingIntent = PendingIntent.getActivity(
                                getApplicationContext(),
                                button.hashCode(),
                                buttonActivityIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
                        );
                    } else {
                        pendingIntent = PendingIntent.getBroadcast(
                                getApplicationContext(),
                                button.hashCode(),
                                buttonBroadcastIntent,
                                PendingIntent.FLAG_IMMUTABLE
                        );
                    }

                    builder.addAction(R.drawable.ic_grey_circle, button.text, pendingIntent);
                }
            }
        }

        int EXPANDED_TEXT_LENGTH = 49;
        if (text.length() >= EXPANDED_TEXT_LENGTH) {

            builder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(text));

        }

        if (largeIcon != null) {
            Picasso.get().load(largeIcon).into(new ImageBitmapTarget() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    if (bigPicture)
                        builder.setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap)
                                .bigLargeIcon((Bitmap) null));
                    builder.setLargeIcon(bitmap);
                    showNotification(builder);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    showNotification(builder);
                }
            });
        } else {
            showNotification(builder);
        }
    }

    private void showNotification(NotificationCompat.Builder builder) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Костыль на проверку версий, странное поведение на андроидах ниже 13,
        // NotificationManagerCompat.from(context).areNotificationsEnabled() -
        // - может отбивать PERMISSION_DENIED при включенных уведомлениях
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
            ) {
                // This permission are guaranteed in the app
                // through the DevinoSdk.getInstance().requestNotificationPermission() method.
                Log.e(LOG_TAG, "Returning, permission error Android " + Build.VERSION.SDK_INT);
                return;
            }
        } else {
            // Нет return'а, см. пометку выше
            Log.e(LOG_TAG, "permission error Android " + Build.VERSION.SDK_INT);
        }

        notificationManager.notify(113, builder.build());
        Log.d(LOG_TAG, "notify");
    }

    private void createNotificationChannel(Uri sound) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel =
                    new NotificationChannel(channelId, "devino", importance);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(
                    new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400}
            );

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (sound != null) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                notificationChannel.setSound(sound, audioAttributes);
            }

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private Integer getIconDrawableId(Context context, String name) {
        Resources resources = context.getResources();
        try {
            int resourceId = resources.getIdentifier(
                    name,
                    "drawable",
                    context.getPackageName()
            );
            if (resourceId == RESOURCE_NOT_FOUND) {
                return defaultNotificationIcon;
            } else {
                return resourceId;
            }
        } catch (Exception ex) {
            return defaultNotificationIcon;
        }
    }

    protected static class PushButton {

        @SerializedName("text")
        private String text;

        @SerializedName("deeplink")
        private String deeplink;

        PushButton(String text, String deeplink, String pictureLink) {
            this.text = text;
            this.deeplink = deeplink;
        }

        String getText() {
            return text;
        }

        void setText(String text) {
            this.text = text;
        }

        String getDeeplink() {
            return deeplink;
        }

        void setDeeplink(String deeplink) {
            this.deeplink = deeplink;
        }
    }
}