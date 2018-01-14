package com.ulternate.paycat.adapters;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Custom ValueFormatter for the BreakdownItem PieChart to remove entry labels.
 */
public class BreakdownValueFormatter implements IValueFormatter {

    // Format the value as desired. Currently set to "" to not show any labels in pie segments.
    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return "";
    }
}
