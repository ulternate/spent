package com.ulternate.paycat.services;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.ulternate.paycat.data.AppDatabase;
import com.ulternate.paycat.data.Transaction;

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
                saveTransactionNotification(sbn);
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
     *
     * The Transaction object is only created if the notification is a valid payment notification.
     */
    private void saveTransactionNotification(StatusBarNotification sbn) {
        // Get the Notification from the StatusBarNotification, so the content can be retrieved.
        Notification notification = sbn.getNotification();

        if (notification != null) {
            Bundle extras = notification.extras;
            String title = extras.getString(Notification.EXTRA_TITLE);
            String content = extras.getString(Notification.EXTRA_TEXT);

            // Try and get an amount from the content, matching against a pattern for currency.
            if (title != null && content != null) {
                Matcher matcher = AMOUNT_PATTERN.matcher(content);

                // If a match is found, then create and save the Transaction object in the database.
                if (matcher.find()) {
                    Transaction transaction = new Transaction(
                            Float.parseFloat(matcher.group(1)),
                            title,
                            DEFAULT_ANDROID_PAY_CATEGORY,
                            new Date(sbn.getPostTime())
                    );
                    new AddTransactionAsyncTask(getApplicationContext()).execute(transaction);
                }
            }
        }
    }

    /**
     * Private class to insert a Transaction object into the database asynchronously.
     */
    private static class AddTransactionAsyncTask extends AsyncTask<Transaction, Void, Void> {

        // Instance of the app database.
        private AppDatabase mAppDatabase;

        /**
         * Construct the AsyncTask and get the AppDatabase instance.
         * @param context: The context from the service.
         */
        AddTransactionAsyncTask(Context context) {
            mAppDatabase = AppDatabase.getAppDatabase(context);
        }

        /**
         * Insert the transaction into the database in the background.
         * @param transactions: An array of transactions.
         * @return null.
         */
        @Override
        protected Void doInBackground(Transaction... transactions) {
            mAppDatabase.transactionDao().insertTransaction(transactions[0]);
            return null;
        }
    }
}
