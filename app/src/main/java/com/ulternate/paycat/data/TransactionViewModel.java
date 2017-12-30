package com.ulternate.paycat.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
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

    /**
     * Construct the TransactionViewModel with the application context.
     * @param application: The application.
     */
    public TransactionViewModel(@NonNull Application application) {
        super(application);

        // Get an instance of the database.
        AppDatabase mAppDatabase = AppDatabase.getAppDatabase(application.getApplicationContext());

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
}
