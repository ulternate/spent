package com.ulternate.paycat.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.ulternate.paycat.data.AppDatabase;
import com.ulternate.paycat.data.Transaction;

/**
 * Asynchronous task to insert a Transaction object into the database.
 */
public class AddTransactionAsyncTask extends AsyncTask<Transaction, Void, Void> {

    // Instance of the app database.
    private AppDatabase mAppDatabase;

    /**
     * Construct the AsyncTask and get the AppDatabase instance.
     * @param context: The context from the service.
     */
    public AddTransactionAsyncTask(Context context) {
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
