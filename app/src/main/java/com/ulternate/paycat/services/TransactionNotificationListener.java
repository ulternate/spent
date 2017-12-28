package com.ulternate.paycat.services;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.ulternate.paycat.activities.MainActivity;

/**
 * Notification Listener Service to listen to notifications from payment applications and store the
 * information from the Transaction.
 */
public class TransactionNotificationListener extends NotificationListenerService {

    // Listen for notifications from the following applications.
    private static final String ANDROID_PAY_PACKAGE_NAME = "com.google.android.apps.walletnfcrel";
    private static final String ANDROID_GMS_PACKAGE_NAME = "com.google.android.gms";
    // TODO remove pushover after testing AndroidPay.
    private static final String PUSHOVER_PACKAGE_NAME = "net.superblock.pushover";

    /**
     * Handle the notification and send a broadcast to the main app if it's from a watched app.
     * @param sbn: The StatusBarNotification.
     */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        switch (sbn.getPackageName()) {
            case ANDROID_PAY_PACKAGE_NAME:
            case ANDROID_GMS_PACKAGE_NAME:
            case PUSHOVER_PACKAGE_NAME:
                buildAndSendAndroidPayTransactionBroadcast(sbn);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    /**
     * Build the Intent used to send the payment information back to the application.
     * @param sbn: The StatusBarNotification.
     *
     * TODO handle logic here (i.e. only send the Broadcast if it's a payment notification, not a
     * different one from a valid app.
     */
    private void buildAndSendAndroidPayTransactionBroadcast(StatusBarNotification sbn) {
        Intent intent = new Intent(MainActivity.PACKAGE_NAME);

        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;
        intent.putExtra("title", extras.getString(Notification.EXTRA_TITLE));
        intent.putExtra("content", extras.getString(Notification.EXTRA_TEXT));
        intent.putExtra("date", sbn.getPostTime());

        sendBroadcast(intent);
    }
}
