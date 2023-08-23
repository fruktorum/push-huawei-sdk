package com.devinotele.huaweidevinosdk.sdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class DevinoSdkPushService extends HmsMessageService {

    Gson gson = new Gson();
    private final String channelId = "devino_push";
    @DrawableRes
    static Integer defaultNotificationIcon = R.drawable.ic_grey_circle;
    @ColorInt
    static Integer defaultNotificationIconColor = 0x333333;
    private static final int RESOURCE_NOT_FOUND = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getDataOfMap().size() > 0) {

            Map<String, String> data = remoteMessage.getDataOfMap();
            Log.d("DevinoPush", "data = " + data);

            String pushId = data.get("pushId");
            if (pushId == null) return;

            String image = data.get("image");
            String smallIcon = data.get("smallIcon");
            String iconColor = data.get("iconColor");
            String title = data.get("title");
            String body = data.get("body");

            if (title == null || body == null) return;

            String badge = data.get("badge");
            int badgeNumber = 0;
            if (badge != null) {
                badgeNumber = Integer.parseInt(badge);
            }
            Log.d("DevinoPush", "badgeNumber =  " + badgeNumber);

            String action = data.get("action");
            Log.d("DevinoPush", "action =  " + action);

            String buttonsJson = data.get("buttons");
            Type listType = new TypeToken<List<PushButton>>() {
            }.getType();
            List<PushButton> buttons = gson.fromJson(buttonsJson, listType);

            String customDataString = data.get("customData");
            if (customDataString != null) {
                DevinoSdk.getInstance().saveCustomDataFromPushJson(customDataString);
                Log.d("DevinoPush", "CustomDataString =  " + customDataString);
            }

            //Uri sound = DevinoSdk.getInstance().getSound();
            String sound = data.get("sound");
            Uri soundUri;
            if (sound != null) {
                soundUri = Uri.parse(sound);
            } else {
                soundUri = DevinoSdk.getInstance().getSound();
            }
            Log.d("DevinoPush", "soundUri =  " + soundUri);

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
                        badgeNumber);
            }

            DevinoSdk.getInstance().pushEvent(pushId, DevinoSdk.PushStatus.DELIVERED, null);
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
            defaultPendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    0,
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );
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

        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(defaultPendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setSound(null)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setChannelId(channelId)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

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
                Integer color = Integer.getInteger(iconColor);
                if (color != null) builder.setColor(color);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            builder.setColor(defaultNotificationIconColor);
        }

        int EXPANDED_TEXT_LENGTH = 49;
        if (text.length() >= EXPANDED_TEXT_LENGTH) {
            builder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(text));
        }

        if (largeIcon != null) {
            Bitmap bitmap = ImageDownloader.getBitmapFromURL(largeIcon);
            if (bigPicture) {
                builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
            }
            builder.setLargeIcon(bitmap);
        }

        if (soundUri != null) {
            playRingtone(soundUri);
        }

        if (buttons != null && buttons.size() > 0) {
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

                    Log.d("DevinoPush", "button.deeplink =  " + button.deeplink);
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

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            // This permission are guaranteed in the app
            // through the DevinoSdk.getInstance().requestNotificationPermission() method.
            return;
        }
        notificationManager.notify(113, builder.build());

    }

    private void playRingtone(Uri customSound) {
        Uri notificationSound =
                customSound != null
                        ? customSound
                        : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationSound);
        if (ringtone != null) {
            ringtone.play();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel =
                    new NotificationChannel(channelId, "devino", importance);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(
                    new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400}
            );
            notificationChannel.setSound(null, null);
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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