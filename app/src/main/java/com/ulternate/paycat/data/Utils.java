package com.ulternate.paycat.data;

import android.content.SharedPreferences;

import com.ulternate.paycat.activities.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Utilities class for working with Data from Database and SharedPreferences.
 */
public class Utils {

    /**
     * Get the updated list of categories, including the custom ones added using the "Other" field.
     * @param originalCategories: Array of strings representing the original categories from the
     *                          application resources.
     * @param mPrefs: SharedPreference object for the application.
     * @param extraCategories: Array of strings to be appended to the end of the final list of
     *                       categories, i.e. "Unknown" and "Other".
     * @return an ArrayList of Strings representing the default and custom categories.
     */
    public static List<String> getCategories(String[] originalCategories, SharedPreferences mPrefs,
                                       String[] extraCategories) {

        List<String> allCategories = new ArrayList<>(Arrays.asList(originalCategories));

        String added_categories_array = mPrefs.getString(MainActivity.PREFS_CUSTOM_CATEGORIES_ARRAY, "");

        // Add any custom categories.
        if (!added_categories_array.isEmpty()) {
            String[] added_categories = added_categories_array.split("\\|");
            for (String category: added_categories) {
                if (!allCategories.contains(category)) {
                    allCategories.add(category);
                }
            }
        }

        // Sort the categories.
        Collections.sort(allCategories, String.CASE_INSENSITIVE_ORDER);

        // Add any extra categories to the end of the list.
        allCategories.addAll(Arrays.asList(extraCategories));

        return allCategories;
    }

    /**
     * Get a String representation of a Date.
     * @param formatter: A SimpleDateFormatter object to format the Date object.
     * @param dateLong: The Date as a long number, generally timeInMilliSeconds.
     * @return String formatted date, or null if the formatting fails.
     */
    public static String getDateFromLong(SimpleDateFormat formatter, Long dateLong) {
        Calendar mCal = Calendar.getInstance();
        try {
            mCal.setTimeInMillis(dateLong);
            return formatter.format(mCal.getTime());
        } catch (Exception e) {
            return null;
        }
    }
}
