package com.ulternate.paycat.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.ulternate.paycat.data.AppDatabase;
import com.ulternate.paycat.data.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Asynchronous task to insert a Transaction object into the database.
 */
public class AddTransactionAsyncTask extends AsyncTask<Transaction, Void, Void> {

    // Instance of the app database.
    private AppDatabase mAppDatabase;
    private boolean mMatchCategories;

    /**
     * Construct the AsyncTask and get the AppDatabase instance.
     * @param context: The context from the service.
     * @param matchCategories: boolean, whether to attempt to match categories or not.
     */
    public AddTransactionAsyncTask(Context context, boolean matchCategories) {
        mAppDatabase = AppDatabase.getAppDatabase(context);
        mMatchCategories = matchCategories;
    }

    /**
     * Insert the transaction into the database in the background.
     * @param transactions: An array of transactions.
     * @return null.
     */
    @Override
    protected Void doInBackground(Transaction... transactions) {
        Transaction newTransaction = transactions[0];

        // The user has chosen to try and automatically match categories.
        if (mMatchCategories) {
            // Get the transactions matching the original description from the new transaction.
            List<Transaction> transactionsList = mAppDatabase.transactionDao().getCategoriesForMerchant(
                    newTransaction.originalDescription);
            if (!transactionsList.isEmpty()) {
                if (transactionsList.size() == 1) {
                    // If there's only one transaction matching that originalDescription, then use that.
                    newTransaction.category = transactionsList.get(0).category;
                } else {
                    // Otherwise, get all the categories and count the most frequent.
                    List<String> categories = new ArrayList<>();
                    for (Transaction transaction: transactionsList) {
                        categories.add(transaction.category);
                    }

                    // Map the categories, counting the number of times they occur.
                    Map<String,Integer> map = new HashMap<>();
                    for(int i = 0; i < categories.size(); i++) {
                        Integer count = map.get(categories.get(i));
                        map.put(categories.get(i), count == null ? 1 : count + 1);
                    }

                    // Get the most common entry from the map. Currently this doesn't account for
                    // entries with the same number of occurances.
                    Map.Entry<String, Integer> mostCommon = null;
                    for (Map.Entry<String, Integer> entry: map.entrySet()) {
                        if (mostCommon == null || mostCommon.getValue() < entry.getValue()) {
                            mostCommon = entry;
                        }
                    }

                    // Set the newTransaction's category to the most common category from past
                    // matching transactions.
                    if (mostCommon != null) {
                        newTransaction.category = mostCommon.getKey();
                    }
                }
            }
        }

        // Add the new Transaction to the database.
        mAppDatabase.transactionDao().insertTransaction(newTransaction);
        return null;
    }
}
