package com.ulternate.paycat.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.ContextCompat;

import com.ulternate.paycat.R;
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

    private static boolean isLocationPermissionGranted;
    private static boolean isMatchingCategoriesEnabled;

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

    // Default transaction categories and paymentApp values.
    private final String DEFAULT_CATEGORY = Resources.getSystem().getString(android.R.string.unknownName);
    private static final String ANDROID_PAY = "Android Pay";
    private static final String PAYPAL = "PayPal";

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
                // We have a valid notification to save, check if the location permission was
                // granted, if so then record the location when saving the Transaction.
                isLocationPermissionGranted = ContextCompat
                        .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

                // Check if the user has requested to attempt to match categories from past transactions.
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                isMatchingCategoriesEnabled = mPrefs.getBoolean(
                        getResources().getString(R.string.pref_match_category_key), false);

                boolean notificationHandled = false;

                switch (notification_code) {
                    case ANDROID_PAY_CODE:
                        notificationHandled = handleAndroidPayNotification(title, content, sbn.getPostTime());
                        break;
                    case PAYPAL_CODE:
                        notificationHandled = handlePaypalNotification(content, sbn.getPostTime());
                        break;
                }

                // Try and cancel the notification if the user has requested it in the settings.
                if (mPrefs.getBoolean(getResources().getString(R.string.pref_dismiss_notification_key), false)) {
                    if (notificationHandled) {
                        cancelNotification(sbn.getKey());
                    }
                }
            }
        }
    }

    /**
     * Get a Location object representing the last location from the LocationProvider.
     * @return a Location object with the Lat and Long of the phone when the notification was
     * received, or use the default location (which is 0.0).
     */
    @SuppressLint("MissingPermission")
    private Location getLastLocation() {
        String mLocationProvider = LocationManager.GPS_PROVIDER;
        Location lastLocation = null;

        if (isLocationPermissionGranted) {
            LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            if (mLocationManager != null) {
                // This can still be null if there hasn't been time to record a last known location
                // hence the check on the return statement below.
                lastLocation = mLocationManager.getLastKnownLocation(mLocationProvider);
            }
        }

        return lastLocation == null ? new Location(mLocationProvider) : lastLocation;
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
     * @return Boolean, true if the notification content could be parsed and a Transaction adding
     *         task was started, otherwise false.
     *
     * If the notification represents a transaction, then a Transaction object is created and added
     * to the database asynchronously.
     */
    private boolean handleAndroidPayNotification(String title, String content, Long postTime) {
        Pattern pattern = Pattern.compile(AMOUNT_PATTERN_REGEX);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            // Get the last location (will have LatLng 0:0 if location permission is disabled).
            Location transactionLocation = getLastLocation();

            // Build and save the Transaction.
            Transaction transaction = new Transaction(
                    Float.parseFloat(matcher.group(1)),
                    title,
                    title,
                    DEFAULT_CATEGORY,
                    ANDROID_PAY,
                    new Date(postTime),
                    (float) transactionLocation.getLatitude(),
                    (float) transactionLocation.getLongitude());
            new AddTransactionAsyncTask(getApplicationContext(), isMatchingCategoriesEnabled).execute(transaction);
            return true;
        }

        return false;
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
     * @return Boolean, true if the notification content could be parsed and a Transaction adding
     *         task was started, otherwise false.
     *
     * If the notification represents a transaction, then a Transaction object is created and added
     * to the database asynchronously.
     */
    private boolean handlePaypalNotification(String content, long postTime) {
        Pattern pattern = Pattern.compile(PAYPAL_PATTERN_REGEX);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            if (matcher.groupCount() == 2) {
                // Get the last location (will have LatLng 0:0 if location permission is disabled).
                Location transactionLocation = getLastLocation();

                // Build and save the Transaction.
                Transaction transaction = new Transaction(
                        Float.parseFloat(matcher.group(1)),
                        matcher.group(2),
                        matcher.group(2),
                        DEFAULT_CATEGORY,
                        PAYPAL,
                        new Date(postTime),
                        (float) transactionLocation.getLatitude(),
                        (float) transactionLocation.getLongitude());
                new AddTransactionAsyncTask(getApplicationContext(), isMatchingCategoriesEnabled).execute(transaction);

                return true;
            }
        }

        return false;
    }
}
