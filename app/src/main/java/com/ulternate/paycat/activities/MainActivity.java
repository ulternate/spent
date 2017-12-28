package com.ulternate.paycat.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.ulternate.paycat.R;
import com.ulternate.paycat.adapters.TransactionAdapter;
import com.ulternate.paycat.data.Transaction;
import com.ulternate.paycat.data.TransactionViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main activity for the application.
 */
public class MainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private static final String AMOUNT_PATTERN_REGEX = "\\$ ?(\\d+\\.\\d*)";
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(AMOUNT_PATTERN_REGEX);

    // Package name for this application.
    public static final String PACKAGE_NAME = "com.ulternate.paycat";

    private TransactionViewModel mTransactionViewModel;
    private TransactionAdapter mRecyclerViewAdapter;

    private PaymentNotificationBroadcastReceiver mBroadCastReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the Transactions RecyclerView.
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.transactionsList);
        // Set the size to fixed as changes in layout don't change the size of
        // the RecyclerView.
        mRecyclerView.setHasFixedSize(true);

        // Get the layout manager for the RecyclerView.
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Specify the adapter for the RecyclerView.
        mRecyclerViewAdapter = new TransactionAdapter(new ArrayList<Transaction>());
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        // Set the TransactionViewModel.
        mTransactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);

        // Get and observe the transactions list for changes.
        mTransactionViewModel.getTransactionsList().observe(MainActivity.this, new Observer<List<Transaction>>() {
            @Override
            public void onChanged(@Nullable List<Transaction> transactions) {
                mRecyclerViewAdapter.addTransactions(transactions);
            }
        });

        // Prompt the user to enable the notification listener service if they haven't.
        if (!isNotificationServiceEnabled()) {
            buildNotificationServiceAlertDialog().show();
        }

        // Register a BroadcastReceiver to handle notifications received by the listener.
        mBroadCastReceiver = new PaymentNotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PACKAGE_NAME);
        registerReceiver(mBroadCastReceiver, intentFilter);
    }

    /**
     * Unregister the BroadcastReceiver when destroying the application activity.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadCastReceiver);
    }

    /**
     * Add Transaction based on information captured from a notification using the Notification
     * Listener.
     * @param intent: The Intent sent by the Notification Listener when a notification from a
     *              payment application was received.
     */
    private void addPaymentTransactionFromNotification(Intent intent) {
        // Get values from the intent.
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        Date date = new Date(intent.getLongExtra("date", System.currentTimeMillis()));

        // TODO handle categories and check if a transaction with the same date, title and content
        // is in the database (i.e. don't duplicate).
        if (!title.isEmpty() && !content.isEmpty()) {
            // Parse the amount from the content.
            // Only add a transaction if there is an amount in the notification.
            Matcher matcher = AMOUNT_PATTERN.matcher(content);
            if (matcher.find()) {
                Transaction transaction = new Transaction(
                        Float.parseFloat(matcher.group(1)),
                        title,
                        "Test Transaction",
                        date
                );
                mTransactionViewModel.addTransaction(transaction);
            }
        }
    }

    /**
     * Check if the notification service is enabled.
     * Verifies if the notification listener service is enabled.
     * @return True if eanbled, false otherwise.
     *
     * Example from: https://github.com/Chagall/notification-listener-service-example
     */
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     *
     * Example from: https://github.com/Chagall/notification-listener-service-example
     */
    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO warn the user of reduced functionality if dismissed before enabling.
                    }
                });
        return(alertDialogBuilder.create());
    }

    /**
     * BroadcastReceiver for intents sent by the TransactionNotificationListener service.
     */
    public class PaymentNotificationBroadcastReceiver extends BroadcastReceiver {

        /**
         * Record the transaction information, adding it to the database.
         * @param context: Context.
         * @param intent: The Intent sent by the TransactionNotificationListener.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            addPaymentTransactionFromNotification(intent);
        }
    }
}
