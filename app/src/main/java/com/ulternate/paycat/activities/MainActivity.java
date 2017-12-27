package com.ulternate.paycat.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.ulternate.paycat.R;
import com.ulternate.paycat.adapters.TransactionAdapter;
import com.ulternate.paycat.data.Transaction;
import com.ulternate.paycat.data.TransactionViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Main activity for the application.
 */
public class MainActivity extends AppCompatActivity {

    private TransactionViewModel mTransactionViewModel;

    private RecyclerView mRecyclerView;
    private TransactionAdapter mRecyclerViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the Transactions RecyclerView.
        mRecyclerView = (RecyclerView) findViewById(R.id.transactionsList);
        // Set the size to fixed as changes in layout don't change the size of
        // the RecyclerView.
        mRecyclerView.setHasFixedSize(true);

        // Get the layout manager for the RecyclerView.
        mLayoutManager = new LinearLayoutManager(this);
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

        // Add some fake transactions each time the app is opened.
        Random random = new Random();
        String dollars = String.valueOf(random.nextInt(100));
        String cents = String.valueOf(random.nextInt(99));
        String amount = dollars + "." + cents;
        mTransactionViewModel.addTransaction(new Transaction(
                Float.parseFloat(amount), "New Transaction of " + amount,
                "Video Games", new Date(System.currentTimeMillis())));
    }

}
