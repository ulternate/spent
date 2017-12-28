package com.ulternate.paycat.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ulternate.paycat.R;
import com.ulternate.paycat.data.Transaction;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * RecyclerView adapter to show transactions in the RecyclerView.
 */

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    // Dataset for the Adapter.
    private List<Transaction> mDataset;

    // Date form used to format Date objects as desired.
    private SimpleDateFormat mSimpleDate = new SimpleDateFormat("yyyy-MM-dd H:mm a");

    /**
     * Construct a ViewHolder to provide reference to the views for each data item.
     *
     * A Transaction item has an amount, date and description TextView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView transactionAmount;
        public TextView transactionDate;
        public TextView transactionDescription;

        public ViewHolder(View itemView) {
            super(itemView);

            this.transactionAmount = itemView.findViewById(R.id.transactionAmount);
            this.transactionDate = itemView.findViewById(R.id.transactionDate);
            this.transactionDescription = itemView.findViewById(R.id.transactionDescription);
        }
    }

    /**
     * Constructor to set the dataset for the Adapter.
     * @param transactions: A list of Transaction objects.
     */
    public TransactionAdapter(List<Transaction> transactions) {
        mDataset = transactions;
    }

    /**
     * Add a List of transactions to the dataset.
     * @param transactions: A list of Transaction objects.
     */
    public void addTransactions(List<Transaction> transactions) {
        this.mDataset.addAll(transactions);
        notifyDataSetChanged();
    }

    /**
     * Create new views for each transaction.
     * @param parent: The parent ViewGroup.
     * @param viewType: int representing the view type.
     * @return: a new ViewHolder with the transaction view.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view using the Transaction Item CardView layout.
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_item, parent, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view with data from the transaction at that
    // position in the dataset.

    /**
     * Replace the view contents with data from the Transaction at that position in the dataset.
     * @param holder: The ViewHolder.
     * @param position: The position in the dataset.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.transactionAmount.setText(String.valueOf(mDataset.get(position).amount));
        holder.transactionDate.setText(mSimpleDate.format(mDataset.get(position).date));
        holder.transactionDescription.setText(mDataset.get(position).description);
    }

    /**
     * Return the size of the dataset.
     * @return: Size of the dataset.
     */
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
