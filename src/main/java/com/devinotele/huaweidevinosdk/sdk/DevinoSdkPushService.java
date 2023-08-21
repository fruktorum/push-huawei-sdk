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
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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

            String pushId = data.get("pushId");

            if (pushId == null) return;

            String image = data.get("image");
            String icon = data.get("smallIcon");
            String iconColor = data.get("iconColor");
            String title = data.get("title");
            String body = data.get("body");
            String action = data.get("action");
            String buttonsJson = data.get("buttons");
            Type listType = new TypeToken<List<PushButton>>() {
            }.getType();
            List<PushButton> buttons = gson.fromJson(buttonsJson, listType);

            Uri sound = DevinoSdk.getInstance().getSound();

            boolean isSilent = "true".equalsIgnoreCase(data.get("silentPush"));
            if (!isSilent) {
                showSimpleNotification(title, body, icon, iconColor, image, buttons, true, sound, pushId, action);
            }

            DevinoSdk.getInstance().pushEvent(pushId, DevinoSdk.PushStatus.DELIVERED, null);
        }

    }

    @SuppressLint("NotificationTrampoline")
    public void showSimpleNotification(String title, String text, String smallIcon, String iconColor, String largeIcon, List<PushButton> buttons, Boolean bigPicture, Uri sound, String pushId, String action) {

        Intent broadcastIntent = new Intent(getApplicationContext(), DevinoPushReceiver.class);
        broadcastIntent.putExtra(DevinoPushReceiver.KEY_PUSH_ID, pushId);
        if (action != null) {
            broadcastIntent.putExtra(DevinoPushReceiver.KEY_DEEPLINK, action);
        } else
            broadcastIntent.putExtra(DevinoPushReceiver.KEY_DEEPLINK, DevinoPushReceiver.KEY_DEFAULT_ACTION);


        Intent deleteIntent = new Intent(getApplicationContext(), DevinoCancelReceiver.class);
        deleteIntent.putExtra(DevinoPushReceiver.KEY_PUSH_ID, pushId);

        PendingIntent defaultPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), broadcastIntent.hashCode(), broadcastIntent, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(getApplicationContext(), deleteIntent.hashCode(), deleteIntent, PendingIntent.FLAG_IMMUTABLE);

        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(defaultPendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setSound(null)
                .setChannelId(channelId)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (smallIcon != null) {
            builder.setSmallIcon(getIconDrawableId(getApplicationContext(), smallIcon));
        } else builder.setSmallIcon(defaultNotificationIcon);

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

        if (buttons != null && buttons.size() > 0) {
            for (PushButton button : buttons) {
                if (button.text != null) {
                    Intent intent = new Intent(this, DevinoPushReceiver.class);
                    intent.putExtra(DevinoPushReceiver.KEY_DEEPLINK, button.deeplink);
                    intent.putExtra(DevinoPushReceiver.KEY_PUSH_ID, pushId);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), button.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);
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
                        builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
                    builder.setLargeIcon(bitmap);
                    showNotification(builder);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    showNotification(builder);
                }
            });
        } else {
            playRingtone(sound);
            showNotification(builder);
        }

    }

    private void playRingtone(Uri customSound) {
        Uri notificationSound = customSound != null ? customSound : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationSound);
        if (ringtone != null) ringtone.play();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, "devino", importance);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationChannel.setSound(null, null);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private Integer getIconDrawableId(Context context, String name) {
        Resources resources = context.getResources();
        try {
            int resourceId = resources.getIdentifier(name, "drawable",
                    context.getPackageName());
            if (resourceId == RESOURCE_NOT_FOUND) return defaultNotificationIcon;
            else return resourceId;
        } catch (Exception ex) {
            return defaultNotificationIcon;
        }
    }

    private void showNotification(NotificationCompat.Builder builder) {
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

    protected class PushButton {

        @SerializedName("text")
        private String text;

        @SerializedName("deeplink")
        private String deeplink;

        @SerializedName("picture")
        private String pictureLink;

        PushButton(String text, String deeplink, String pictureLink) {
            this.text = text;
            this.deeplink = deeplink;
            this.pictureLink = pictureLink;
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

        String getPictureLink() {
            return pictureLink;
        }

        void setPictureLink(String pictureLink) {
            this.pictureLink = pictureLink;
        }
    }
}
