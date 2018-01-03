package com.ulternate.paycat.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.ulternate.paycat.data.AppDatabase;
import com.ulternate.paycat.data.Transaction;

/**
 * Asynchronous task to delete a Transaction from the database.
 */
public class DeleteTransactionAsyncTask extends AsyncTask<Transaction, Void, Void> {

    // Instance of the app database.
    private AppDatabase mAppDatabase;

    /**
     * Construct the AsyncTask and get the AppDatabase instance.
     * @param context: The context from the service.
     */
    public DeleteTransactionAsyncTask(Context context) {
        mAppDatabase = AppDatabase.getAppDatabase(context);
    }

    @Override
    protected Void doInBackground(Transaction... transactions) {
        mAppDatabase.transactionDao().deleteTransaction(transactions[0]);
        return null;
    }
}
