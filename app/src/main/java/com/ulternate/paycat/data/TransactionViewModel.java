package com.ulternate.paycat.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

/**
 * ViewModel for Transaction records.
 *
 * This class prepares and manages data for Transactions for Activities/Fragments that utilise it.
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
     * Get a filtered list of all transactions.
     * @param from: Date object to filter from.
     * @param to: Date object to filter to.
     * @return A LiveData list of all transactions made between the provided dates.
     */
    public LiveData<List<Transaction>> getFilteredTransactionsList(Date from, Date to) {
        return mAppDatabase.transactionDao().getTransactionsBetweenDates(from, to);
    }
}
