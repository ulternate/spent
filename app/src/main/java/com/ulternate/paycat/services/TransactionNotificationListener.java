package com.ulternate.paycat.services;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.ulternate.paycat.activities.MainActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Notification Listener Service to listen to notifications from payment applications and store the
 * information from the Transaction.
 */
public class TransactionNotificationListener extends NotificationListenerService {

    // Listen for notifications from the following applications. Note: Android Pay notifications
    // come from GMS after a transaction.
    private static final String ANDROID_PAY_PACKAGE_NAME = "com.google.android.apps.walletnfcrel";
    private static final String ANDROID_GMS_PACKAGE_NAME = "com.google.android.gms";
    // TODO remove pushover after testing AndroidPay.
    private static final String PUSHOVER_PACKAGE_NAME = "net.superblock.pushover";

    // Regular Expression for matching the transaction amount.
    private static final String AMOUNT_PATTERN_REGEX = "\\$ ?(\\d+\\.\\d*)";
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(AMOUNT_PATTERN_REGEX);

    // Default transaction categories.
    private static final String DEFAULT_ANDROID_PAY_CATEGORY = "Android Pay";

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
     * Build and broadcast an intent containing the transaction information for Android Pay
     * @param sbn: The StatusBarNotification.
     *
     * The intent is only broadcast if it is a transaction notification (as Android Pay
     * could send multiple notifications, as well as notifications coming via GMS).
     */
    private void buildAndSendAndroidPayTransactionBroadcast(StatusBarNotification sbn) {
        // Get the Notification from the StatusBarNotification, so the content can be retreived.
        Notification notification = sbn.getNotification();

        if (notification != null) {
            Bundle extras = notification.extras;
            String title = extras.getString(Notification.EXTRA_TITLE);
            String content = extras.getString(Notification.EXTRA_TEXT);

            // Try and get an amount from the content, if there is a match then build and broadcast
            // an intent to the Application for recording.
            if (title != null && content != null) {
                Matcher matcher = AMOUNT_PATTERN.matcher(content);

                // If a match is found, then build and broadcast the intent.
                if (matcher.find()) {
                    Intent intent = new Intent(MainActivity.PACKAGE_NAME);

                    intent.putExtra("title", title);
                    intent.putExtra("amount", Float.parseFloat(matcher.group(1)));
                    intent.putExtra("category", DEFAULT_ANDROID_PAY_CATEGORY);
                    intent.putExtra("date", sbn.getPostTime());

                    sendBroadcast(intent);
                }
            }
        }
    }
}
