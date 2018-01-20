package com.ulternate.paycat.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.ulternate.paycat.data.Transaction;
import com.ulternate.paycat.data.TransactionViewModel;

import java.util.List;

/**
 * Base Fragment to be used in the ViewPager to implement methods common across all Fragments added
 * to the ViewPager that show information relating to Transaction objects.
 */
public abstract class BaseTransactionFragment extends Fragment {

    public TransactionViewModel mTransactionViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get an instance of the TransactionViewModel to be used for all subclasses.
        mTransactionViewModel = ViewModelProviders.of(this).get(
                TransactionViewModel.class);
    }

    /**
     * Get all Transaction objects from the TransactionViewModel for the initial unfiltered view.
     */
    public void getAllTransactions() {
        // Get and observe the Transactions list for changes.
        mTransactionViewModel.getTransactionsList().observe(getActivity(),
                new Observer<List<Transaction>>() {
                    @Override
                    public void onChanged(@Nullable List<Transaction> transactions) {
                        updateAdapter(transactions);
                    }
                });
    }

    /**
     * Update the Adapter with the most recent list of Transactions. Override in subclasses to
     * implement appropriate logic.
     * @param transactions: A List of Transaction objects returned by the filter.
     */
    public void updateAdapter(List<Transaction> transactions) {}
}
