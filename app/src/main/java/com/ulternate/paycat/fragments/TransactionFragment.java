package com.ulternate.paycat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ulternate.paycat.R;
import com.ulternate.paycat.activities.DetailActivity;
import com.ulternate.paycat.adapters.TransactionAdapter;
import com.ulternate.paycat.adapters.TransactionDividerItemDecoration;
import com.ulternate.paycat.adapters.TransactionOnClickListener;
import com.ulternate.paycat.data.Transaction;
import com.ulternate.paycat.data.Utils;
import com.ulternate.paycat.tasks.AddTransactionAsyncTask;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment to show all transactions.
 */
public class TransactionFragment extends BaseTransactionFragment {

    private static final int DETAIL_ACTIVITY_CODE = 1;

    private TransactionAdapter mRecyclerViewAdapter;
    private Transaction mDeletedTransaction;
    private View mView;

    public TransactionFragment() {
        // Empty constructor.
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
        mRecyclerViewAdapter = new TransactionAdapter(new ArrayList<Transaction>(), mTransactionOnClickListener);
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
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.addTransactions(transactions);
        }
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
            startActivityForResult(detailIntent, DETAIL_ACTIVITY_CODE);
        }
    };

    /**
     * Handle the result from any activity started for a result.
     * @param requestCode: The request code used when the activity was started, int.
     * @param resultCode: The result code set by the activity prior to being finished, int.
     * @param data: The return Intent containing any data sent back by the activity.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Handle the return from the DetailActivity after deleting a Transaction.
        if (requestCode == DETAIL_ACTIVITY_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the transaction that was deleted.
                mDeletedTransaction = (Transaction) data.getSerializableExtra("transaction");
                // Show a Snackbar, mentioning which Transaction was deleted and providing the
                // option to undo the deletion of the Transaction.
                String msg = getResources().getString(
                        R.string.delete_transaction_success_message, mDeletedTransaction.description);
                Snackbar deletedSnackbar = Snackbar.make(mView, msg, Snackbar.LENGTH_LONG);
                deletedSnackbar.setAction(getResources().getString(R.string.undo), mUndoDeletionListener);
                Utils.showSnackbarAboveBottomNavMenu(deletedSnackbar);
            }
        }
    }

    /**
     * OnClickListener used by the Snackbar shown when a Transaction is deleted to enable the user
     * to undo the deletion of that Transaction.
     */
    private View.OnClickListener mUndoDeletionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Re-add the deleted transaction back to the database.
            new AddTransactionAsyncTask(getContext(), false).execute(mDeletedTransaction);
        }
    };
}
