package com.ulternate.paycat.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.ulternate.paycat.R;
import com.ulternate.paycat.activities.MainActivity;
import com.ulternate.paycat.adapters.CategoryBreakdownAxisFormatter;
import com.ulternate.paycat.adapters.CategoryBreakdownDataSet;
import com.ulternate.paycat.adapters.CategoryBreakdownValueFormatter;
import com.ulternate.paycat.data.Transaction;
import com.ulternate.paycat.data.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.ganfra.materialspinner.MaterialSpinner;

/**
 * Fragment to show the breakdown for an individual category.
 */
public class CategoryBreakdownFragment extends BaseTransactionFragment implements OnChartValueSelectedListener {

    // View fields.
    private TextView mNoTransactions;
    private TextView mNoTransactionsForCategory;
    private CardView mChartCard;
    private CardView mCategoryCard;

    // Helper fields.
    private List<Transaction> mTransactions = new ArrayList<>();
    private String mChosenCategory;
    private SharedPreferences mPrefs;

    // Graph fields.
    private CategoryBreakdownDataSet mDataSet;
    private LineData mChartData;
    private LineChart mChart;

    public CategoryBreakdownFragment() {
        // Empty constructor.
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_category_breakdown, container, false);

        // Get the DefaultSharedPreferences.
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get the filtering views.
        mCategoryCard = mView.findViewById(R.id.categoryBreakdownSelectionCard);
        mNoTransactions = mView.findViewById(R.id.categoryBreakdownNoTransactions);
        mNoTransactionsForCategory = mView.findViewById(R.id.categoryBreakdownNoCategoryTransactions);
        MaterialSpinner mCategorySpinner = mView.findViewById(R.id.categoryBreakdownSpinner);

        // Find and configure the LineChart and it's DataSet.
        mChartCard = mView.findViewById(R.id.categoryBreakdownChartCard);
        mChart = mView.findViewById(R.id.categoryBreakdownChart);
        mDataSet = new CategoryBreakdownDataSet(new ArrayList<Entry>(), "Category breakdown", mPrefs);
        configureChart(mChart, mDataSet);

        // Set the DataSet holder and assign it to the LineChart.
        mChartData = new LineData(mDataSet);
        mChart.setData(mChartData);
        mChart.invalidate();

        // Get all the categories, both the default and custom categories.
        String[] originalCategories = getResources().getStringArray(R.array.default_categories);
        String[] extraCategories = {getResources().getString(R.string.category_unknown)};
        List<String> mCategories = Utils.getCategories(originalCategories, mPrefs, extraCategories);

        // Set the adapter for the spinner.
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mCategories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(spinnerAdapter);

        // Set custom item selected listener.
        mCategorySpinner.setOnItemSelectedListener(mOnItemSelectedListener);

        // Set the initial selection of the spinner if one has been saved previously.
        // Note, MaterialSpinner prepends the hint to the list so we must add 1 to the position for
        // selection to select the correct item.
        mChosenCategory = mPrefs.getString(MainActivity.PREFS_CHOSEN_CATEGORY_BREAKDOWN, "");
        if (mCategories.contains(mChosenCategory)) {
            mCategorySpinner.setSelection(mCategories.indexOf(mChosenCategory) + 1);
        }

        // Get the list of Transactions, using any saved date filter, or all Transactions.
        getTransactions();

        return mView;
    }

    /**
     * Update this Fragments Adapter with a list of transactions.
     * @param transactions: A List of Transaction objects (may or may not be filtered).
     */
    @Override
    public void updateAdapter(List<Transaction> transactions) {
        // Store the transactions for use when selecting the filter by the dropdown.
        mTransactions = transactions;

        if (transactions.size() > 0) {
            // There are transactions, show the category selector.
            mCategoryCard.setVisibility(View.VISIBLE);
            mNoTransactions.setVisibility(View.GONE);

            // Update the LineChart to show all Transactions matching the Users chosen Category.
            if (!mChosenCategory.isEmpty() && !Objects.equals(mChosenCategory, "")) {
                mChartCard.setVisibility(View.VISIBLE);

                mDataSet.addTransactions(transactions, mChosenCategory);

                // The chosen category may have no Transactions.
                if (mDataSet.getEntryCount() > 0) {
                    // Show the LineChart card and refresh the chart.
                    mChart.setVisibility(View.VISIBLE);
                    mNoTransactionsForCategory.setVisibility(View.GONE);

                    updateDataSets(true);
                } else {
                    // The Category has no Transactions, hide the chart but show the helper text.
                    mChart.setVisibility(View.GONE);

                    // Set the text resource depending on if a date filter is being used or not.
                    if (mPrefs.getBoolean(MainActivity.PREFS_FILTERED_BOOLEAN_KEY, false)) {
                        mNoTransactionsForCategory.setText(getResources().getString(
                                R.string.category_breakdown_no_transactions_for_category_filtered));
                    } else {
                        mNoTransactionsForCategory.setText(getResources().getString(
                                R.string.category_breakdown_no_transactions_for_category));
                    }
                    mNoTransactionsForCategory.setVisibility(View.VISIBLE);

                    updateDataSets(false);
                }
            } else {
                updateDataSets(false);

                mChartCard.setVisibility(View.GONE);
            }
        } else {
            // Hide the Category and LineChart cards and show the no Transactions helper text.
            mNoTransactions.setVisibility(View.VISIBLE);
            mCategoryCard.setVisibility(View.GONE);
            mChartCard.setVisibility(View.GONE);
        }
    }

    /**
     * Update the DataSets, or clear if update is set to false.
     * @param update: Boolean, pass false to clear the DataSet for the LineChart, otherwise the
     *              DataSets will be notified that there were changes and the LineChart will be
     *              invalidated to redraw.
     */
    private void updateDataSets(boolean update) {
        if (update) {
            // The DataSet may have been cleared.
            if (mChartData.getDataSetCount() > 0) {
                mChartData.notifyDataChanged();
            } else {
                mChartData.addDataSet(mDataSet);
            }

            // Set the granularity of the x axis to match the difference between records to avoid
            // duplicate date values in the x axis labels. This is done after updating to better
            // handle live updating of the Category breakdown.
            XAxis xAxis = mChart.getXAxis();
            float valueSeparation = 1f;
            if (mDataSet.getEntryCount() > 0) {
                valueSeparation = (mDataSet.getXMax() - mDataSet.getXMin()) / mDataSet.getEntryCount();
            }
            xAxis.setGranularity(valueSeparation);

            // Update the chart data and invalidate to redraw.
            mChart.notifyDataSetChanged();
            mChart.invalidate();
        } else {
            mChart.getData().clearValues();
        }
    }

    /**
     * Configure the LineChart to match desired styling.
     * @param chart: A LineChart object to be configured.
     * @param dataSet: A CategoryBreakdownDataSet object to be configured.
     */
    private void configureChart(LineChart chart, CategoryBreakdownDataSet dataSet) {
        // Disable the description, legend, grid background and the right axis.
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setDrawGridBackground(false);

        // Enable touch events and set the listener to show the SnackBar when a value is touched.
        chart.setTouchEnabled(true);
        chart.setOnChartValueSelectedListener(this);

        // Configure the LineChart X Axis, moving to the bottom, adding padding for the labels and
        // changing the format of the values from floats to Date Strings.
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setYOffset(10f);
        xAxis.setValueFormatter(new CategoryBreakdownAxisFormatter());

        // Configure the LineChart Y Axis.
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMinimum(0f);

        // Configure the CategoryBreakdownDataSet
        int plotColour = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
        mDataSet.setColor(plotColour);
        mDataSet.setLineWidth(2.5f);
        mDataSet.setCircleColor(plotColour);
        mDataSet.setDrawFilled(true);
        mDataSet.setFillColor(plotColour);
        mDataSet.setValueFormatter(new CategoryBreakdownValueFormatter());
        mDataSet.setValueTextSize(12f);
    }

    /**
     * OnItemSelectedListener for the Category spinner, saving the chosen Category and updating the
     * Adapter for the LineChart when a selection is made.
     */
    private AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position >= 0) {
                // Get the selected item from the parents adapter.
                mChosenCategory = parent.getAdapter().getItem(position).toString();

                // Save the selected Category as the current filter.
                mPrefs.edit().putString(MainActivity.PREFS_CHOSEN_CATEGORY_BREAKDOWN, mChosenCategory).apply();

                // Update the LineChart's adapter to show the plot for the chosen Category.
                updateAdapter(mTransactions);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Do Nothing.
        }
    };

    /**
     * Called when a value has been selected inside the chart.
     *
     * @param e: The selected Entry.
     * @param h: The corresponding highlight object that contains information
     *          about the highlighted position such as dataSetIndex, ...
     */
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e.getY() > 0) {
            String dateString = Utils.getDateFromLong(MainActivity.DATE_FORMAT_NO_TIME, (long) e.getX());
            if (dateString != null) {
                String msg = getResources().getString(R.string.category_selected, e.getY(), dateString);
                Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Called when nothing has been selected or an "un-select" has been made.
     */
    @Override
    public void onNothingSelected() {

    }
}
