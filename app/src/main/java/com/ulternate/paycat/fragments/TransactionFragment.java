package com.ulternate.paycat.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ulternate.paycat.R;
import com.ulternate.paycat.activities.DetailActivity;
import com.ulternate.paycat.adapters.TransactionAdapter;
import com.ulternate.paycat.adapters.TransactionDividerItemDecoration;
import com.ulternate.paycat.adapters.TransactionOnClickListener;
import com.ulternate.paycat.data.Transaction;
import com.ulternate.paycat.data.TransactionViewModel;
import com.ulternate.paycat.tasks.AddTransactionAsyncTask;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment to show all transactions.
 */
public class TransactionFragment extends Fragment {

    private static final int DETAIL_ACTIVITY_CODE = 1;

    private TransactionViewModel mTransactionViewModel;
    private TransactionAdapter mRecyclerViewAdapter;
    private Transaction mDeletedTransaction;
    private View mView;

    private boolean mSelectingFrom = true;
    private Date mFromDate;
    private Date mToDate;

    public TransactionFragment() {
        // Empty constructor.
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow handling of option menu clicks in the fragment.
        setHasOptionsMenu(true);
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

        // Set the TransactionViewModel.
        mTransactionViewModel = ViewModelProviders.of(this).get(
                TransactionViewModel.class);

        // Get all the Transactions.
        getAllTransactions();

        return mView;
    }

    /**
     * Observe the list of Transactions from the TransactionViewModel for changes initially adding
     * all Transactions.
     */
    private void getAllTransactions() {
        // Remove any observers for the filtered Transactions list.
        mTransactionViewModel.getFilteredTransactionsList(mFromDate, mToDate).removeObservers(getActivity());

        // Get and observe the Transactions list for changes.
        mTransactionViewModel.getTransactionsList().observe(getActivity(),
                new Observer<List<Transaction>>() {
                    @Override
                    public void onChanged(@Nullable List<Transaction> transactions) {
                        mRecyclerViewAdapter.addTransactions(transactions);
                    }
                });
    }

    /**
     * Handle the selection of particular MenuItems in the options menu.
     * @param item: The MenuItem selected.
     * @return boolean Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_date_filter:
                // Launch a date picker dialog to filter the Transactions.
                buildAndShowDatePickerDialog(true);
                return true;
            case R.id.menu_clear_date_filter:
                // Remove the filtered Transactions list observers and get all Transactions.
                getAllTransactions();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Build and show a DatePickerDialog to get the dates to filter Transactions by.
     * @param isFirstPicker: If true, then this is the first dialog, representing the "From" date
     *                     in the filter range, otherwise the user is selecting the "To" date.
     */
    private void buildAndShowDatePickerDialog(boolean isFirstPicker) {
        Calendar mInitialCalendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                mDateSetListener,
                mInitialCalendar.get(Calendar.YEAR),
                mInitialCalendar.get(Calendar.MONTH),
                mInitialCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.autoDismiss(true);

        // Set the title of the DatePicker.
        if (isFirstPicker) {
            datePickerDialog.setTitle(getResources().getString(R.string.date_from));
        } else {
            datePickerDialog.setTitle(getResources().getString(R.string.date_to));
        }

        datePickerDialog.show(getActivity().getFragmentManager(), "DatePickerDialog");
    }

    /**
     * Handle the selection of the date in the DatePickerDialog.
     */
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            Calendar mInitialCalendar = Calendar.getInstance();
            mInitialCalendar.set(year, monthOfYear, dayOfMonth);

            if (mSelectingFrom) {
                mFromDate = mInitialCalendar.getTime();
                mSelectingFrom = false;
                buildAndShowDatePickerDialog(false);
            } else {
                mToDate = mInitialCalendar.getTime();
                mSelectingFrom = true;

                // Remove any observers on the full Transactions list and observe the filtered list.
                // This applies the filter to the RecyclerView.
                mTransactionViewModel.getTransactionsList().removeObservers(getActivity());
                mTransactionViewModel.getFilteredTransactionsList(mFromDate, mToDate).observe(getActivity(), new Observer<List<Transaction>>() {
                    @Override
                    public void onChanged(@Nullable List<Transaction> transactions) {
                        mRecyclerViewAdapter.addTransactions(transactions);
                    }
                });
            }
        }
    };

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
                deletedSnackbar.setAction(getResources().getString(R.string.undo), mUndoDeletionListener).show();
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
