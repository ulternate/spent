package com.ulternate.paycat.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.ulternate.paycat.data.AppDatabase;
import com.ulternate.paycat.data.Transaction;

/**
 * Private class to update a Transaction object in the database asynchronously.
 */
public class UpdateTransactionAsyncTask extends AsyncTask<Transaction, Void, Void> {

    // Instance of the app database.
    private AppDatabase mAppDatabase;

    /**
     * Construct the AsyncTask and get the AppDatabase instance.
     * @param context: The context from the service.
     */
    public UpdateTransactionAsyncTask(Context context) {
        mAppDatabase = AppDatabase.getAppDatabase(context);
    }

    @Override
    protected Void doInBackground(Transaction... transactions) {
        mAppDatabase.transactionDao().updateTransaction(transactions[0]);
        return null;
    }
}
