package com.ulternate.paycat.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.ulternate.paycat.R;
import com.ulternate.paycat.adapters.BreakdownAdapter;
import com.ulternate.paycat.adapters.BreakdownDataSet;
import com.ulternate.paycat.adapters.BreakdownValueFormatter;
import com.ulternate.paycat.data.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to show the breakdown of all categories.
 */
public class BreakdownFragment extends BaseTransactionFragment {

    private static final String DATA_SET_LABEL = "Categories";

    private BreakdownAdapter mBreakdownAdapter;
    private PieChart mPieChart;
    private PieData mPieData;
    private BreakdownDataSet mDataSet;

    public BreakdownFragment() {
        // Empty constructor.
    }

    /**
     * Initialise the Adapters and DataSets and other fields required by the Fragment.
     * @param savedInstanceState: If the activity is being re-initialized after previously being
     *                          shut down then this Bundle contains the data it most recently
     *                          supplied in onSaveInstanceState. Note: Otherwise it is null.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the BreakdownAdapter.
        mBreakdownAdapter = new BreakdownAdapter(new ArrayList<Transaction>(), getContext());

        // Set the DataSet and PieData.
        mDataSet = new BreakdownDataSet(new ArrayList<PieEntry>(), "Categories");
        mPieData = new PieData(mDataSet);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_breakdown, container, false);

        // Find and configure the PieChart and it's DataSet.
        mPieChart = rootView.findViewById(R.id.breakdownChart);
        configureBreakdownChart(mPieChart, mDataSet);

        // Set our DataSet holder and assign it to the PieChart.
        mPieChart.setData(mPieData);
        mPieChart.invalidate();

        // Find and configure the RecyclerView showing the breakdown.
        RecyclerView mRecyclerView = rootView.findViewById(R.id.breakdownList);

        // Get the layout manager for the RecyclerView.
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Specify the adapter for the RecyclerView.
        mRecyclerView.setAdapter(mBreakdownAdapter);

        // Get the list of Transactions, using any saved date filter, or all Transactions.
        getTransactions();

        return rootView;
    }

    /**
     * Update this Fragments Breakdown Adapter with a list of transactions.
     * @param transactions: A List of Transaction objects (may or may not be filtered).
     */
    @Override
    public void updateAdapter(List<Transaction> transactions) {
        // Add the transactions to the DataSet and BreakdownAdapter.
        mDataSet.addTransactions(transactions);
        mBreakdownAdapter.addTransactions(transactions);
        // Notify the Data has changed and refresh the PieChart.
        mPieData.notifyDataChanged();
        mPieChart.notifyDataSetChanged();
        mPieChart.invalidate();
    }

    /**
     * Configure the PieChart to match desired styling.
     * @param pieChart: A PieChart object to be configured.
     * @param pieDataSet: A DataSet object to be configured.
     */
    private void configureBreakdownChart(PieChart pieChart, PieDataSet pieDataSet) {
        // Configure the PieChart.
        // Have no hole (i.e. not a Donut).
        pieChart.setHoleRadius(0f);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setTransparentCircleRadius(0f);
        // Hide the labels for each entry.
        pieChart.setDrawEntryLabels(false);
        // Disable rotation of the graph.
        pieChart.setRotationEnabled(false);
        // Disable highlighting of segments when tapped.
        pieChart.setHighlightPerTapEnabled(false);
        // Disable the automatic description and legend.
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);

        // Configure the PieDataSet.
        // Set a custom ValueFormatter to not show pie segment values.
        pieDataSet.setValueFormatter(new BreakdownValueFormatter());
        // Set colours.
        pieDataSet.setColors(getResources().getIntArray(R.array.material_colors_500));
        // Set space size between slices.
        pieDataSet.setSliceSpace(2f);
    }
}
