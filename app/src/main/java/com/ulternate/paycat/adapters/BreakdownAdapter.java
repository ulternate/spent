package com.ulternate.paycat.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ulternate.paycat.R;
import com.ulternate.paycat.data.BreakdownItem;
import com.ulternate.paycat.data.Transaction;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * RecyclerView Adapter to show the category totals in the BreakdownItem view.
 */
public class BreakdownAdapter extends RecyclerView.Adapter<BreakdownAdapter.ViewHolder> {

    // Dataset for the Adapter.
    private List<BreakdownItem> mBreakdownList = new ArrayList<>();
    private Map<String, Float> mAmountsMap = new HashMap<>();

    // Context used to get colour resources.
    private Context mContext;

    /**
     * Constructor to set the dataset for the Adapter.
     * @param transactions: A list of Transaction objects.
     */
    public BreakdownAdapter(List<Transaction> transactions, Context context) {
        this.mContext = context;

        updateCategoryAmounts(transactions);
    }

    /**
     * Add a List of transactions to the dataset.
     * @param transactions: A list of Transaction objects.
     */
    public void addTransactions(List<Transaction> transactions) {
        this.mBreakdownList.clear();
        this.mAmountsMap.clear();

        updateCategoryAmounts(transactions);

        notifyDataSetChanged();
    }

    /**
     * Update the amounts for each category from the updated transactions list.
     * @param transactions: A List of Transaction objects from the ViewModel.
     */
    private void updateCategoryAmounts(List<Transaction> transactions) {
        // Calculate the amounts per category.
        for (Transaction transaction : transactions) {
            String category = transaction.category;
            float amount = transaction.amount;

            if (mAmountsMap.containsKey(category)) {
                mAmountsMap.put(category, mAmountsMap.get(category) + amount);
            } else {
                mAmountsMap.put(category, amount);
            }
        }

        // Update the Breakdown list.
        for (Map.Entry<String, Float> entry : mAmountsMap.entrySet()) {
            mBreakdownList.add(new BreakdownItem(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Create new views for each category.
     * @param parent: The parent ViewGroup.
     * @param viewType: int representing the view type.
     * @return a new ViewHolder with the category view.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mCategoryItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.breakdown_item, parent, false);
        return new ViewHolder(mCategoryItem);
    }

    /**
     * Replace the view contents with data from the Category BreakdownItem at that position in the data.
     * @param holder: The ViewHolder.
     * @param position: The position in the dataset.
     */
    @Override
    public void onBindViewHolder(BreakdownAdapter.ViewHolder holder, int position) {
        // Get the currency from the current Locale and prepend the symbol to the amount.
        Currency currency = Currency.getInstance(Locale.getDefault());
        String qualifiedAmount = currency.getSymbol() + String.valueOf(mBreakdownList.get(position).breakdownTotal);

        int[] colours = mContext.getResources().getIntArray(R.array.material_colors_500);
        if (position < colours.length) {
            holder.mBreakdownColour.setColorFilter(colours[position]);
        }
        holder.mBreakdownCategory.setText(mBreakdownList.get(position).breakdownCategory);
        holder.mBreakdownTotal.setText(qualifiedAmount);
    }

    /**
     * Return the size of the dataset.
     * @return Size of the dataset.
     */
    @Override
    public int getItemCount() {
        return mBreakdownList.size();
    }

    /**
     * Construct a ViewHolder to provide reference to the views for each data item.
     *
     * A Breakdown item has a colour, category and total.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mBreakdownColour;
        TextView mBreakdownCategory;
        TextView mBreakdownTotal;

        /**
         * Constructor for the ViewHolder.
         * @param itemView: The View that has been inflated by the adapter.
         */
        ViewHolder(View itemView) {
            super(itemView);

            this.mBreakdownColour = itemView.findViewById(R.id.breakdownColour);
            this.mBreakdownCategory = itemView.findViewById(R.id.breakdownCategory);
            this.mBreakdownTotal = itemView.findViewById(R.id.breakdownTotal);
        }
    }
}
