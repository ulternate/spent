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

    private BreakdownAdapter mBreakdownAdapter;
    private PieChart mPieChart;
    private PieData mPieData;
    private BreakdownDataSet mDataSet;

    public BreakdownFragment() {
        // Empty constructor.
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_breakdown, container, false);

        // Find and configure the PieChart and it's DataSet.
        mPieChart = rootView.findViewById(R.id.breakdownChart);
        mDataSet = new BreakdownDataSet(new ArrayList<PieEntry>(), "Categories");
        configureBreakdownChart(mPieChart, mDataSet);

        // Set our DataSet holder and assign it to the PieChart.
        mPieData = new PieData(mDataSet);
        mPieChart.setData(mPieData);
        mPieChart.invalidate();

        // Find and configure the RecyclerView showing the breakdown.
        RecyclerView mRecyclerView = rootView.findViewById(R.id.breakdownList);

        // Get the layout manager for the RecyclerView.
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Specify the adapter for the RecyclerView.
        mBreakdownAdapter = new BreakdownAdapter(new ArrayList<Transaction>(), getContext());
        mRecyclerView.setAdapter(mBreakdownAdapter);

        // Get all Transactions initially.
        getAllTransactions();

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
