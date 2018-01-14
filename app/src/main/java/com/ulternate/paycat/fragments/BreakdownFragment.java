package com.ulternate.paycat.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.ulternate.paycat.R;
import com.ulternate.paycat.adapters.BreakdownDataSet;
import com.ulternate.paycat.adapters.BreakdownValueFormatter;
import com.ulternate.paycat.data.AppDatabase;
import com.ulternate.paycat.data.Transaction;
import com.ulternate.paycat.data.TransactionViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to show the breakdown of all categories.
 */
public class BreakdownFragment extends Fragment {

    private PieChart mPieChart;
    private PieData mPieData;
    private BreakdownDataSet mDataSet;
    private List<Transaction> mTransactions = new ArrayList<>();

    public BreakdownFragment() {
        // Empty constructor.
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // Set the TransactionViewModel which holds all the Transactions.
        TransactionViewModel mTransactionViewModel = ViewModelProviders.of(this).get(
                TransactionViewModel.class);

        // Get and observe the transactions list for future changes.
        mTransactionViewModel.getTransactionsList().observe(getActivity(),
                new Observer<List<Transaction>>() {
                    @Override
                    public void onChanged(@Nullable List<Transaction> transactions) {
                        // Add the transactions to the DataSet.
                        mDataSet.addTransactions(transactions);
                        // Notify the Data has changed and refresh the PieChart.
                        mPieData.notifyDataChanged();
                        mPieChart.notifyDataSetChanged();
                        mPieChart.invalidate();
                    }
                });

        return rootView;
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
