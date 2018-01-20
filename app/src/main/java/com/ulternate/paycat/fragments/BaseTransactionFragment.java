package com.ulternate.paycat.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.ulternate.paycat.activities.MainActivity;
import com.ulternate.paycat.data.Transaction;
import com.ulternate.paycat.data.TransactionViewModel;

import java.util.Date;
import java.util.List;

/**
 * Base Fragment to be used in the ViewPager to implement methods common across all Fragments added
 * to the ViewPager that show information relating to Transaction objects.
 */
public abstract class BaseTransactionFragment extends Fragment {

    public TransactionViewModel mTransactionViewModel;

    private SharedPreferences mPrefs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get an instance of the TransactionViewModel to be used for all subclasses.
        mTransactionViewModel = ViewModelProviders.of(this).get(
                TransactionViewModel.class);

        // Get the Preferences to see if the Transactions list should start filtered.
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    /**
     * Get the Transaction objects from the TransactionViewModel.
     *
     * If the user has applied a filter then use that date range, otherwise return all Transactions.
     */
    public void getTransactions() {
        if (mPrefs.getBoolean(MainActivity.PREFS_FILTERED_BOOLEAN_KEY, false)) {
            if (mPrefs.contains(MainActivity.PREFS_DATE_FROM_LONG_KEY) &&
                    mPrefs.contains(MainActivity.PREFS_DATE_TO_LONG_KEY)) {
                Date from = new Date(mPrefs.getLong(MainActivity.PREFS_DATE_FROM_LONG_KEY, 0));
                Date to = new Date(mPrefs.getLong(MainActivity.PREFS_DATE_TO_LONG_KEY, 0));

                // Get and observe a filtered Transactions list.
                mTransactionViewModel.getFilteredTransactionsList(from, to).observe(
                        getActivity(), mTransactionsListObserver);
            }
        } else {
            // Get and observe all Transactions for changes.
            mTransactionViewModel.getTransactionsList().observe(
                    getActivity(), mTransactionsListObserver);
        }
    }

    /**
     * Observer for changes to the Transactions List returned by the TransactionViewModel. On change
     * update the Adapter in the Fragment via the overridden updateAdapter function.
     */
    private Observer<List<Transaction>> mTransactionsListObserver = new Observer<List<Transaction>>() {
        @Override
        public void onChanged(@Nullable List<Transaction> transactions) {
            updateAdapter(transactions);
        }
    };

    /**
     * Update the Adapter with the most recent list of Transactions. Override in subclasses to
     * implement appropriate logic.
     * @param transactions: A List of Transaction objects returned by the filter.
     */
    public void updateAdapter(List<Transaction> transactions) {}
}
