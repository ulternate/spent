package com.ulternate.paycat.adapters;

import android.content.SharedPreferences;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.ulternate.paycat.activities.MainActivity;
import com.ulternate.paycat.data.Transaction;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Custom BarDataSet to handle dynamically filtering transaction data.
 */
public class CategoryBreakdownDataSet extends LineDataSet {

    // DataSet for the LineChart.
    private List<Entry> mEntries;

    // Map of Date Strings and the total of all Transactions for that day.
    private Map<String, Float> mTransactionDateMap = new HashMap<>();

    // SharedPreferences object used to add missing days when viewing a filtered list.
    private SharedPreferences mPrefs;

    // List of Transactions matching the chosen Category;
    private List<Transaction> mMatchingTransactions = new ArrayList<>();

    /**
     * Construct the CategoryBreakdownDataSet with a list of values.
     * @param yVals: List of yVals to be plotted on the LineChart.
     * @param label: String, the label for the LineChart.
     * @param prefs: SharedPreferences object used to get the Filter range.
     */
    public CategoryBreakdownDataSet(List<Entry> yVals, String label, SharedPreferences prefs) {
        super(yVals, label);
        this.mEntries = yVals;

        this.mPrefs = prefs;
    }

    /**
     * Update the LineChart using the most recent list of Transaction objects.
     * @param transactions: A list of Transaction objects.
     * @param chosenCategory: String, the Category chosen to filter the Transactions by.
     */
    public void addTransactions(List<Transaction> transactions, String chosenCategory) {
        mEntries.clear();
        mMatchingTransactions.clear();
        mTransactionDateMap.clear();

        updateCategoryBySelection(transactions, chosenCategory);

        notifyDataSetChanged();
    }

    /**
     * Update the chart data set to only show Transactions being filtered by the user, setting empty
     * values for dates with no Transactions and adding Transactions from the same day.
     * @param transactions: A list of Transaction objects.
     */
    private void updateCategoryBySelection(List<Transaction> transactions, String chosenCategory) {
        // Get only those Transactions matching the chosen Category.
        for (Transaction transaction : transactions) {
            if (Objects.equals(transaction.category, chosenCategory)) {
                mMatchingTransactions.add(transaction);

                // Store the amount per day for matching Transactions.
                String dateKey = transaction.getDateStringMinusTime();
                if (mTransactionDateMap.containsKey(dateKey)) {
                    mTransactionDateMap.put(dateKey, mTransactionDateMap.get(dateKey) + transaction.amount);
                } else {
                    mTransactionDateMap.put(dateKey, transaction.amount);
                }
            }
        }

        // Don't bother doing the rest if there are no matching transactions.
        if (mMatchingTransactions.size() <= 0) {
            return;
        }

        // Sort the matching Transactions by date (oldest to newest).
        Collections.sort(mMatchingTransactions, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction o1, Transaction o2) {
                return o1.date.compareTo(o2.date);
            }
        });

        // Set the start and end range to match the earliest and latest Transactions.
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        if (mMatchingTransactions.size() > 0) {
            startTime.setTimeInMillis(mMatchingTransactions.get(0).date.getTime());
            endTime.setTimeInMillis(mMatchingTransactions.get(mMatchingTransactions.size() - 1).date.getTime());
        }

        // If there's a filter applied, try and override the start and end time using the filter.
        if (mPrefs.getBoolean(MainActivity.PREFS_FILTERED_BOOLEAN_KEY, false)) {
            if (mPrefs.contains(MainActivity.PREFS_DATE_FROM_LONG_KEY) && mPrefs.contains(MainActivity.PREFS_DATE_TO_LONG_KEY)) {
                startTime.setTimeInMillis(mPrefs.getLong(MainActivity.PREFS_DATE_FROM_LONG_KEY, 0));
                endTime.setTimeInMillis(mPrefs.getLong(MainActivity.PREFS_DATE_TO_LONG_KEY, 0));
            }
        }

        // Create empty entries for the DateRange, if no Transaction history exists for that day.
        for (Date date = startTime.getTime(); startTime.before(endTime); startTime.add(Calendar.DATE, 1), date = startTime.getTime()) {
            String dateKey = MainActivity.DATE_FORMAT_NO_TIME.format(date);
            if (!mTransactionDateMap.containsKey(dateKey)) {
                mTransactionDateMap.put(dateKey, 0f);
            }
        }

        // Add the Transactions from the Map to the BarEntries list.
        for (Map.Entry<String, Float> entry : mTransactionDateMap.entrySet()) {
            try {
                Date entryDate = MainActivity.DATE_FORMAT_NO_TIME.parse(entry.getKey());
                mEntries.add(new Entry(entryDate.getTime(), entry.getValue()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Sort the Entries in ascending order.
        Collections.sort(mEntries, new Comparator<Entry>() {
            @Override
            public int compare(Entry o1, Entry o2) {
                return Float.valueOf(o1.getX()).compareTo(o2.getX());
            }
        });
    }
}
