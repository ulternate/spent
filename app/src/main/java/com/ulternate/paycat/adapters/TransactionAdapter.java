package com.ulternate.paycat.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ulternate.paycat.R;
import com.ulternate.paycat.activities.MainActivity;
import com.ulternate.paycat.data.Transaction;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter to show transactions in the RecyclerView.
 */

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    // Dataset for the Adapter.
    private List<Transaction> mTransactionsList;

    // Listener to handle item clicks.
    private TransactionOnClickListener mListener;

    /**
     * Constructor to set the dataset for the Adapter.
     * @param transactions: A list of Transaction objects.
     * @param listener: A TransactionOnClickListener.
     */
    public TransactionAdapter(List<Transaction> transactions, TransactionOnClickListener listener) {
        this.mTransactionsList = transactions;
        this.mListener = listener;
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

        return new ViewHolder(v, mListener);
    }

    /**
     * Replace the view contents with data from the Transaction at that position in the dataset.
     * @param holder: The ViewHolder.
     * @param position: The position in the dataset.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Format the amount as a currency based upon the locale.
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        String qualifiedAmount = numberFormat.format(mTransactionsList.get(position).amount);

        holder.transactionAmount.setText(qualifiedAmount);
        holder.transactionDate.setText(MainActivity.TRANSACTION_DATE_FORMAT.format(mTransactionsList.get(position).date));
        holder.transactionDescription.setText(mTransactionsList.get(position).description);
        holder.transactionCategory.setText(mTransactionsList.get(position).category);
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
     * Get the Transaction at the specified position in the list.
     * @param position: Int representing the position in the transactions list.
     * @return a Transaction object from the transaction list at that position.
     */
    public Transaction getTransaction(int position) {
        return mTransactionsList.get(position);
    }

    /**
     * Construct a ViewHolder to provide reference to the views for each data item.
     *
     * A Transaction item has an amount, date and description TextView.
     */
    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TransactionOnClickListener mListener;

        TextView transactionAmount;
        TextView transactionDate;
        TextView transactionDescription;
        TextView transactionCategory;

        /**
         * Constructor for the ViewHolder.
         * @param itemView: The View that has been inflated by the adapter.
         * @param listener: The OnClickListener for the view.
         */
        ViewHolder(View itemView, TransactionOnClickListener listener) {
            super(itemView);
            mListener = listener;

            this.transactionAmount = itemView.findViewById(R.id.transactionAmount);
            this.transactionDate = itemView.findViewById(R.id.transactionDate);
            this.transactionDescription = itemView.findViewById(R.id.transactionDescription);
            this.transactionCategory = itemView.findViewById(R.id.transactionCategory);

            itemView.setOnClickListener(this);
        }

        /**
         * Handle the onClick action on the view.
         * @param v: The View that was clicked.
         */
        @Override
        public void onClick(View v) {
            mListener.onClick(v, getAdapterPosition());
        }
    }
}
