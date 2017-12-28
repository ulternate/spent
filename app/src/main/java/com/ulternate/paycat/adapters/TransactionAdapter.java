package com.ulternate.paycat.adapters;

import android.annotation.SuppressLint;
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
    private List<Transaction> mTransactionsList;

    // Date form used to format Date objects as desired.
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat mSimpleDate = new SimpleDateFormat("yyyy-MM-dd H:mm a");

    /**
     * Constructor to set the dataset for the Adapter.
     * @param transactions: A list of Transaction objects.
     */
    public TransactionAdapter(List<Transaction> transactions) {
        this.mTransactionsList = transactions;
    }

    /**
     * Add a List of transactions to the dataset.
     * @param transactions: A list of Transaction objects.
     */
    public void addTransactions(List<Transaction> transactions) {
        this.mTransactionsList.clear();
        this.mTransactionsList.addAll(transactions);
        notifyDataSetChanged();
    }

    /**
     * Create new views for each transaction.
     * @param parent: The parent ViewGroup.
     * @param viewType: int representing the view type.
     * @return a new ViewHolder with the transaction view.
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
        holder.transactionAmount.setText(String.valueOf(mTransactionsList.get(position).amount));
        holder.transactionDate.setText(mSimpleDate.format(mTransactionsList.get(position).date));
        holder.transactionDescription.setText(mTransactionsList.get(position).description);
    }

    /**
     * Return the size of the dataset.
     * @return Size of the dataset.
     */
    @Override
    public int getItemCount() {
        return mTransactionsList.size();
    }

    /**
     * Construct a ViewHolder to provide reference to the views for each data item.
     *
     * A Transaction item has an amount, date and description TextView.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView transactionAmount;
        TextView transactionDate;
        TextView transactionDescription;

        ViewHolder(View itemView) {
            super(itemView);

            this.transactionAmount = itemView.findViewById(R.id.transactionAmount);
            this.transactionDate = itemView.findViewById(R.id.transactionDate);
            this.transactionDescription = itemView.findViewById(R.id.transactionDescription);
        }
    }
}
