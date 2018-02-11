package com.ulternate.paycat.adapters;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Custom ValueFormatter for the CategoryBreakdown LineChart to remove entry labels when they're
 * zero.
 */
public class CategoryBreakdownValueFormatter implements IValueFormatter {
    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        if (value > 0) {
            return String.valueOf(value);
        } else {
            return "";
        }
    }
}
