package com.ulternate.paycat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ulternate.paycat.R;
import com.ulternate.paycat.activities.DetailActivity;
import com.ulternate.paycat.activities.MainActivity;
import com.ulternate.paycat.adapters.TransactionAdapter;
import com.ulternate.paycat.adapters.TransactionDividerItemDecoration;
import com.ulternate.paycat.adapters.TransactionOnClickListener;
import com.ulternate.paycat.data.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to show all transactions.
 */
public class TransactionFragment extends BaseTransactionFragment {

    private TransactionAdapter mRecyclerViewAdapter;
    private View mView;

    public TransactionFragment() {
        // Empty constructor.
    }

    /**
     * Initialise the Adapters and DataSets and other fields required by the Fragment.
     * @param savedInstanceState: If the activity is being re-initialized after previously being
     *                          shut down then this Bundle contains the data it most recently
     *                          supplied in onSaveInstanceState. Note: Otherwise it is null.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRecyclerViewAdapter = new TransactionAdapter(new ArrayList<Transaction>(), mTransactionOnClickListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_transaction, container, false);

        // Get the Transactions RecyclerView.
        RecyclerView mRecyclerView = mView.findViewById(R.id.transactionsList);
        // Set the size to fixed as changes in layout don't change the size of
        // the RecyclerView.
        mRecyclerView.setHasFixedSize(true);

        // Get the layout manager for the RecyclerView.
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Specify the adapter for the RecyclerView.
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        // Assign a custom DividerItemDecoration to set the margins between list items in the
        // RecyclerView.
        TransactionDividerItemDecoration dividerItemDecoration = new TransactionDividerItemDecoration(getActivity());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        // Get the list of Transactions, using any saved date filter, or all Transactions.
        getTransactions();

        return mView;
    }

    /**
     * Update this Fragments RecyclerView Adapter with a list of transactions.
     * @param transactions: A List of Transaction objects (may or may not be filtered).
     */
    @Override
    public void updateAdapter(List<Transaction> transactions) {
        mRecyclerViewAdapter.addTransactions(transactions);
    }

    /**
     * OnClickListener to start the DetailActivity when a Transaction is clicked on.
     */
    private TransactionOnClickListener mTransactionOnClickListener = new TransactionOnClickListener() {
        @Override
        public void onClick(View view, int position) {
            Intent detailIntent = new Intent(getContext(), DetailActivity.class);

            // Send the Transaction object to the activity.
            Transaction clickedTransaction = mRecyclerViewAdapter.getTransaction(position);
            detailIntent.putExtra("transaction", clickedTransaction);

            // Start the detail activity for a result, enabling us to undo the deletion action that
            // finishes this activity.
            getActivity().startActivityForResult(detailIntent, MainActivity.DETAIL_ACTIVITY_CODE);
        }
    };
}
