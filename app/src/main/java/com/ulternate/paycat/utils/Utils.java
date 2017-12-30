package com.ulternate.paycat.utils;

import android.content.Intent;

import com.ulternate.paycat.data.Transaction;

import java.util.Date;

/**
 * Utility class containing methods/functions used across activities and services.
 */

public class Utils {

    /**
     * Build a Transaction based on information captured from a notification using the Notification
     * Listener.
     * @param intent: The Intent sent by the Notification Listener when a notification from a
     *              payment application was received.
     * @return a Transaction object with details matching the recently captured notification.
     */
    public static Transaction buildTransactionFromNotification(Intent intent) {
        return new Transaction(
                intent.getFloatExtra("amount", (float) 0.0),
                intent.getStringExtra("title"),
                intent.getStringExtra("category"),
                new Date(intent.getLongExtra("date", System.currentTimeMillis()))
        );
    }
}
