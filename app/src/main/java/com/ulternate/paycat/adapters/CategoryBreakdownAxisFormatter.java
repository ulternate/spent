package com.ulternate.paycat.adapters;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.ulternate.paycat.activities.MainActivity;
import com.ulternate.paycat.data.Utils;

/**
 * Custom AxisFormatter for the CategoryBreakdown Fragment to show long dates as Strings in the
 * xAxis.
 */
public class CategoryBreakdownAxisFormatter implements IAxisValueFormatter{

    /**
     * Called when a value from an axis is to be formatted
     * before being drawn. For performance reasons, avoid excessive calculations
     * and memory allocations inside this method.
     *
     * @param value the value to be formatted.
     * @param axis  the axis the value belongs to.
     * @return the formatted Date String, otherwise an empty String if the formatting failed.
     */
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        String label = Utils.getDateFromLong(MainActivity.DATE_FORMAT_NO_TIME, (long) value);

        return label != null ? label : "";
    }
}
