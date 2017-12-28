package com.ulternate.paycat.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * ViewModel for Transaction records.
 *
 * This class handles the CRUD methods for Transaction records asynchronously.
 */

public class TransactionViewModel extends AndroidViewModel {

    // LiveData list of all transactions.
    private final LiveData<List<Transaction>> mTransactionsList;

    private AppDatabase mAppDatabase;

    /**
     * Construct the TransactionViewModel with the application context.
     * @param application: The application.
     */
    public TransactionViewModel(@NonNull Application application) {
        super(application);

        // Get an instance of the database.
        mAppDatabase = AppDatabase.getAppDatabase(application.getApplicationContext());

        // Get all the transactions, as LiveData (i.e. will update when changed).
        mTransactionsList = mAppDatabase.transactionDao().getTransactions();
    }

    /**
     * Get the list of all transactions.
     * @return A LiveData list of all transactions.
     */
    public LiveData<List<Transaction>> getTransactionsList() {
        return mTransactionsList;
    }

    /**
     * Add a Transaction to the database asynchronously.
     * @param transaction: A single Transaction to be added.
     */
    public void addTransaction(Transaction transaction) {
        new AddTransactionAsyncTask(mAppDatabase).execute(transaction);
    }

    /**
     * Add an array of transactions to the database asynchronously.
     * @param transactions: An array of transactions to be added.
     */
    public void addTransactions(Transaction[] transactions) {
        new AddTransactionAsyncTask(mAppDatabase).execute(transactions);
    }

    /**
     * Private class to add a Transaction to the database asynchronously.
     */
    private static class AddTransactionAsyncTask extends AsyncTask<Transaction, Void, Void> {

        // The database instance.
        private AppDatabase mAppDatabase;

        /**
         * Construct the AddTransactionAsyncTask.
         * @param appDatabase: The database instance.
         */
        AddTransactionAsyncTask(AppDatabase appDatabase) {
            mAppDatabase = appDatabase;
        }

        /**
         * Insert an array of transactions using the TransactionDao.
         * @param transactions: An array of Transactions.
         * @return null.
         */
        @Override
        protected Void doInBackground(Transaction... transactions) {
            mAppDatabase.transactionDao().insertTransactions(transactions);
            return null;
        }
    }
}
