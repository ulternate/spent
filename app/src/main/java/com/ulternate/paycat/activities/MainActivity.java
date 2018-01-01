package com.ulternate.paycat.activities;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.ulternate.paycat.R;
import com.ulternate.paycat.adapters.TransactionAdapter;
import com.ulternate.paycat.adapters.TransactionOnClickListener;
import com.ulternate.paycat.data.Transaction;
import com.ulternate.paycat.data.TransactionViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Main activity for the application.
 */
public class MainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private TransactionAdapter mRecyclerViewAdapter;

    // Date form used to format Date objects as desired.
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat TRANSACTION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd h:mm a");

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
        mRecyclerViewAdapter = new TransactionAdapter(new ArrayList<Transaction>(), mTransactionOnClickListener);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        // Set the TransactionViewModel.
        TransactionViewModel mTransactionViewModel = ViewModelProviders.of(this).get(
                TransactionViewModel.class);

        // Get and observe the transactions list for changes.
        mTransactionViewModel.getTransactionsList().observe(MainActivity.this,
                new Observer<List<Transaction>>() {
            @Override
            public void onChanged(@Nullable List<Transaction> transactions) {
                mRecyclerViewAdapter.addTransactions(transactions);
            }
        });

        // Prompt the user to enable the notification listener service if they haven't.
        if (!isNotificationServiceEnabled()) {
            buildNotificationServiceAlertDialog().show();
        }
    }

    /**
     * OnClickListener to start the DetailActivity when a Transaction is clicked on.
     */
    private TransactionOnClickListener mTransactionOnClickListener = new TransactionOnClickListener() {
        @Override
        public void onClick(View view, int position) {
            Intent detailIntent = new Intent(getApplicationContext(), DetailActivity.class);

            // Send the Transaction object to the activity.
            Transaction clickedTransaction = mRecyclerViewAdapter.getTransaction(position);
            detailIntent.putExtra("transaction", clickedTransaction);

            startActivity(detailIntent);
        }
    };

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
}
