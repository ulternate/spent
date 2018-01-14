package com.ulternate.paycat.adapters;

import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.ulternate.paycat.data.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom PieDataSet to handle dynamically adding transaction data.
 */
public class BreakdownDataSet extends PieDataSet {

    // DataSet for the PieChart.
    private List<PieEntry> mPieEntries;
    private Map<String, Float> mAmountsMap = new HashMap<>();

    public BreakdownDataSet(List<PieEntry> yVals, String label) {
        super(yVals, label);
        this.mPieEntries = yVals;
    }

    /**
     * Add a List of transactions to the dataset.
     * @param transactions: A list of Transaction objects.
     */
    public void addTransactions(List<Transaction> transactions) {
        mPieEntries.clear();
        mAmountsMap.clear();

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

        // Update the PieEntries.
        for (Map.Entry<String, Float> entry : mAmountsMap.entrySet()) {
            mPieEntries.add(new PieEntry((float) entry.getValue(), entry.getKey()));
        }
    }
}
