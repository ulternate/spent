package com.ulternate.paycat.services;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.ulternate.paycat.data.Transaction;
import com.ulternate.paycat.tasks.AddTransactionAsyncTask;

import java.util.Date;
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
    private static final String PAYPAL_PACKAGE_NAME = "com.paypal.android.p2pmobile";

    // Codes used to specify the notification parsing logic.
    private static final int ANDROID_PAY_CODE = 1;
    private static final int PAYPAL_CODE = 2;

    // Regular Expressions for matching the transaction information.
    private static final String AMOUNT_PATTERN_REGEX = "\\$ ?(\\d+\\.\\d*)";
    private static final String PAYPAL_PATTERN_REGEX = AMOUNT_PATTERN_REGEX + "[\\s\\w]*to (.*)";

    // Default transaction categories.
    private static final String DEFAULT_ANDROID_PAY_CATEGORY = "Android Pay";
    private static final String DEFAULT_PAYPAL_CATEGORY = "PayPal";

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
                saveTransactionNotification(sbn, ANDROID_PAY_CODE);
                break;
            case PAYPAL_PACKAGE_NAME:
                saveTransactionNotification(sbn, PAYPAL_CODE);
                break;
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
     * Create and save in the database a Transaction object from the StatusBarNotification.
     * @param sbn: The StatusBarNotification.
     * @param notification_code: int representing the application that sent the notification,
     *                         determine the logic for building and adding the transaction.
     *
     * The Transaction object is only created if the notification is a valid payment notification.
     *
     * Logic for creating a Transaction object differs depending on the format of the notification
     * sent by the payment application.
     */
    private void saveTransactionNotification(StatusBarNotification sbn, int notification_code) {
        // Get the Notification from the StatusBarNotification, so the content can be retrieved.
        Notification notification = sbn.getNotification();

        if (notification != null) {
            Bundle extras = notification.extras;
            String title = extras.getString(Notification.EXTRA_TITLE);
            String content = extras.getString(Notification.EXTRA_TEXT);

            // If the notification had the appropriate content, try and match a transaction using
            // the appropriate logic.
            if (title != null && content != null) {
                switch (notification_code) {
                    case ANDROID_PAY_CODE:
                        handleAndroidPayNotification(title, content, sbn.getPostTime());
                        break;
                    case PAYPAL_CODE:
                        handlePaypalNotification(content, sbn.getPostTime());
                        break;
                }
            }
        }
    }

    /**
     * Handle a notification coming from AndroidPay.
     *
     * @param title: The Title of the notification. Android Pay uses the Title to represent the
     *             merchant, i.e. "MERCHANT NUMBER 1".
     * @param content: The Content of the notification. Android Pay uses this to represent the
     *               amount paid and which card was used, i.e. "$5.21 paid using Visa ****". Regex
     *               matching is used to get the transaction amount.
     * @param postTime: long, represents the post time from the StatusBarNotification, used to set
     *                the transaction date.
     *
     * If the notification represents a transaction, then a Transaction object is created and added
     * to the database asynchronously.
     */
    private void handleAndroidPayNotification(String title, String content, Long postTime) {
        Pattern pattern = Pattern.compile(AMOUNT_PATTERN_REGEX);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            Transaction transaction = new Transaction(
                    Float.parseFloat(matcher.group(1)),
                    title,
                    DEFAULT_ANDROID_PAY_CATEGORY,
                    new Date(postTime)
            );
            new AddTransactionAsyncTask(getApplicationContext()).execute(transaction);
        }
    }

    /**
     * Handle a notification coming from PayPal.
     *
     * @param content: The content of the notification. PayPal uses this to represent the amount
     *               paid and to which merchant you sent money,
     *               i.e. "You've paid $5.01 to MERCHANT NAME". Regex matching is used to get the
     *               transaction amount and merchant name.
     * @param postTime: long, represents the post time from the StatusBarNotification, used to set
     *                the transaction date.
     *
     * If the notification represents a transaction, then a Transaction object is created and added
     * to the database asynchronously.
     */
    private void handlePaypalNotification(String content, long postTime) {
        Pattern pattern = Pattern.compile(PAYPAL_PATTERN_REGEX);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            if (matcher.groupCount() == 2) {
                Transaction transaction = new Transaction(
                        Float.parseFloat(matcher.group(1)),
                        matcher.group(2),
                        DEFAULT_PAYPAL_CATEGORY,
                        new Date(postTime)
                );
                new AddTransactionAsyncTask(getApplicationContext()).execute(transaction);
            }
        }
    }
}
