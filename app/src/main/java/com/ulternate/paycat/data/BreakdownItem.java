package com.ulternate.paycat.data;

import android.graphics.Color;

/**
 * Object to hold an item from the BreakdownItem chart.
 */
public class BreakdownItem {

    public int breakdownColour;

    public String breakdownCategory;

    public float breakdownTotal;

    /**
     * Constructor to create a BreakdownItem for the Breakdown view.
     * @param colour: int, the colour of the item in the PieChart.
     * @param category: String, the category for the BreakdownItem.
     * @param total: Float, the total of all Transactions under the given Category.
     */
    public BreakdownItem(int colour, String category, float total) {
        this.breakdownColour = colour;
        this.breakdownCategory = category;
        this.breakdownTotal = total;
    }

    /**
     * Constructor to create a BreakdownItem for the Breakdown view. Default colour set to BLUE.
     * @param category: String, the category for the BreakdownItem.
     * @param total: Float, the total of all Transactions under the given Category.
     */
    public BreakdownItem(String category, float total) {
        this.breakdownColour = Color.BLUE;
        this.breakdownCategory = category;
        this.breakdownTotal = total;
    }
}
