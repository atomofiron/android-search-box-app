package ru.atomofiron.regextool;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        showNotificationForUpdate();
    }

    private void showNotificationForUpdate() {
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=app.atomofiron.searchboxapp"));
        Intent forpdaIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://4pda.ru/forum/index.php?showtopic=1000070"));
        boolean canOpenMarket = marketIntent.resolveActivity(getPackageManager()) != null;
        boolean canOpenBrowser = forpdaIntent.resolveActivity(getPackageManager()) != null;

        if (!canOpenMarket && !canOpenBrowser) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(Const.NOTIFICATION_CHANNEL_UPDATE_ID);
            if (channel == null) {
                channel = new NotificationChannel(
                        Const.NOTIFICATION_CHANNEL_UPDATE_ID,
                        getString(R.string.channel_name_updates),
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationManager.createNotificationChannel(channel);
            }
        }
        PendingIntent notificationIntent = PendingIntent
                .getActivity(this, ru.atomofiron.regextool.Const.REQUEST_CODE_MARKET_UPDATE, marketIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent actionIntent = PendingIntent
                .getActivity(this, ru.atomofiron.regextool.Const.REQUEST_CODE_MARKET_UPDATE, marketIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, Const.NOTIFICATION_CHANNEL_UPDATE_ID)
                .setTicker(getString(R.string.update_available))
                .setContentTitle(getString(R.string.update_available))
                .setSmallIcon(R.drawable.ic_notification_update)
                .setContentIntent(notificationIntent)
                .addAction(0, getString(R.string.get_update), actionIntent)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimaryLight))
                .build();

        notificationManager.notify(Const.NOTIFICATION_ID_UPDATE, notification);
    }
}
