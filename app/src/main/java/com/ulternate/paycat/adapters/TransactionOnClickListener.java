package com.ulternate.paycat.adapters;

import android.view.View;

/**
 * Interface to enable OnClickListener for the Transaction RecyclerView Adapter.
 */

public interface TransactionOnClickListener {

    void onClick(View view, int position);
}
