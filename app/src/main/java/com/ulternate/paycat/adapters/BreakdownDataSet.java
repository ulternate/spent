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

    private List<PieEntry> mPieEntries;
    private Map<String, Float> mAmountsMap = new HashMap<>();

    public BreakdownDataSet(List<PieEntry> yVals, String label) {
        super(yVals, label);
        this.mPieEntries = yVals;
    }

    public void addTransactions(List<Transaction> transactions) {
        mPieEntries.clear();
        mAmountsMap.clear();

        updateCategoryAmounts(transactions);

        notifyDataSetChanged();
    }

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
