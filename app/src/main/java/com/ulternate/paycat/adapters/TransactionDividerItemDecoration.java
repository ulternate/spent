package com.ulternate.paycat.adapters;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ulternate.paycat.R;

/**
 * Custom DividerItemDecoration
 */
public class TransactionDividerItemDecoration extends RecyclerView.ItemDecoration {

    private final int decorationHeight;

    /**
     * Construct the custom DividerItemDecoration and set the decoration height from the resources.
     * @param context: The ApplicationContext.
     */
    public TransactionDividerItemDecoration(Context context) {
        decorationHeight = context.getResources().getDimensionPixelSize(R.dimen.list_margin);
    }

    /**
     * Get the item offsets for each item.
     * @param outRect: Rect, the outer rectangle.
     * @param view: The view.
     * @param parent: The RecyclerView parent.
     * @param state: The RecyclerView.State.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        // Set the offsets.
        if (parent != null) {
            int itemPosition = parent.getChildAdapterPosition(view);

            // Set the offsets for the left, right and bottom for all items.
            outRect.left = decorationHeight;
            outRect.right = decorationHeight;
            outRect.bottom = decorationHeight;

            // Only the top item needs a top offset, all others get it via the bottom offset of the
            // one above it in the list.
            if (itemPosition == 0) {
                outRect.top = decorationHeight;
            }
        }

    }
}
