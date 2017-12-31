package com.ulternate.paycat.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.ulternate.paycat.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Detail activity for the application, showing information for a single transaction.
 */
public class DetailActivity extends AppCompatActivity {

    // The following widgets enable editing of the transaction.
    private EditText mAmount;
    private EditText mDescription;
    private EditText mDate;
    private EditText mCategoryOther;
    private Spinner mCategory;

    private List<EditText> mTextWidgets;
    private List<String> mCategories = new ArrayList<>();

    private boolean mEditingEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button in the toolbar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Get the widgets.
        mAmount = findViewById(R.id.transactionAmount);
        mDescription = findViewById(R.id.transactionDescription);
        mDate = findViewById(R.id.transactionDate);
        mCategory = findViewById(R.id.transactionCategorySpinner);
        mCategoryOther = findViewById(R.id.transactionCategoryOther);

        // Group similar widgets.
        mTextWidgets = Arrays.asList(mAmount, mDescription, mDate, mCategoryOther);

        // Get all the categories from the spinner.
        SpinnerAdapter categorySpinnerAdapter = mCategory.getAdapter();
        for(int i = 0; i < categorySpinnerAdapter.getCount(); i++) {
            mCategories.add((String) categorySpinnerAdapter.getItem(i));
        }

        // Set the initial values of all widgets.
        setInitialValues(getIntent());

        // Disable editing initially.
        disableEditing();

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEditingEnabled) {
                    // TODO handle the saving of the transaction.
                    // Disable the editing functionality and change the icon to the edit icon.
                    disableEditing();
                    fab.setImageResource(R.drawable.ic_edit_black_24dp);

                    // Show a message that the changes have been saved.
                    showSnackBar(view, getResources().getString(R.string.changes_saved),
                            Snackbar.LENGTH_SHORT, "Changes Saved");
                } else {
                    // Enable editing and change the icon to the save icon.
                    enableEditing();
                    fab.setImageResource(R.drawable.ic_save_black_24dp);

                    // Show a message mentioning that editing is enabled.
                    showSnackBar(view, getResources().getString(R.string.editing_in_progress),
                            Snackbar.LENGTH_SHORT, "Editing Enabled");
                }
            }
        });
    }

    /**
     * Set the values for all widgets from the Transaction information sent in the calling Intent.
     * @param intent: The intent used to start the activity, containing the Transaction information.
     */
    private void setInitialValues(Intent intent) {
        mAmount.setText(String.valueOf(intent.getFloatExtra("amount", (float) 0.0)));
        mDescription.setText(intent.getStringExtra("description"));
        mDate.setText(intent.getStringExtra("date"));

        // Set the category spinner, filling in the Category "Other" EditText if the category can't
        // be found in the list of values in the spinner.
        String initialCategory = intent.getStringExtra("category");
        if (mCategories.contains(initialCategory)) {
            mCategory.setSelection(mCategories.indexOf(initialCategory));
        } else {
            mCategory.setSelection(mCategories.indexOf(getResources().getString(R.string.other)));

            // Make mCategoryOther visible and set the text.
            mCategoryOther.setVisibility(View.VISIBLE);
            mCategoryOther.setText(initialCategory);
        }
    }

    /**
     * Disable the edit functionality of the widgets.
     */
    private void disableEditing() {
        // Disable editing of each EditText widget.
        for (EditText editText: mTextWidgets) {
            editText.setFocusable(false);
            editText.setBackgroundColor(Color.TRANSPARENT);
            editText.setTextIsSelectable(false);
            editText.setCursorVisible(false);
            editText.setInputType(InputType.TYPE_NULL);
            editText.setOnTouchListener(mDisabledOnTouchListener);
        }

        // Disable single line for the description widget. To show multi-line descriptions properly.
        mDescription.setSingleLine(false);

        // Disable selections in the spinner widget.
        mCategory.setClickable(false);
        mCategory.setOnTouchListener(mDisabledOnTouchListener);

        // Mark that editing was disabled.
        mEditingEnabled = false;
    }

    /**
     * Enable the edit functionality of all widgets.
     */
    private void enableEditing() {
        // Enable editing of the EditText widgets.
        for (EditText editText: mTextWidgets) {
            editText.setFocusable(true);
            editText.setTextIsSelectable(true);
            editText.setCursorVisible(true);
            editText.setOnTouchListener(mEnabledOnTouchListener);
        }

        // Set the input types for the fields.
        mAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
        mDescription.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        mDate.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mCategoryOther.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        // Disable single line for the description widget and change the enter key action to enable
        // multi-line descriptions to be edited properly.
        mDescription.setSingleLine(false);
        mDescription.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);

        // Enable selections in the spinner widget.
        mCategory.setClickable(true);
        mCategory.setOnTouchListener(mEnabledOnTouchListener);

        // Set custom item selected listener.
        mCategory.setOnItemSelectedListener(mOnItemSelectedListener);

        // Mark that editing was enabled.
        mEditingEnabled = true;
    }

    /**
     * Show a SnackBar.
     * @param v: The View.
     * @param msg: The mesage to show.
     * @param length: The length of time to show the message for.
     * @param tag: What to tag the action as.
     */
    private void showSnackBar(View v, String msg, int length, String tag) {
        Snackbar.make(v, msg, length).setAction(tag, null).show();
    }

    /**
     * OnTouchListener that warns the user that the editing is disabled until the edit button is
     * clicked. Assigned to the widgets when they are disabled.
     */
    private View.OnTouchListener mDisabledOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            showSnackBar(v, getResources().getString(R.string.editing_locked_warning),
                    Snackbar.LENGTH_SHORT, "Editing Disabled");
            return true;
        }
    };

    /**
     * OnTouchListener used when editing is enabled to replace the mDisabledOnTouchListener so the
     * user doesn't continue to be warned that editing is disabled.
     */
    private View.OnTouchListener mEnabledOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    };

    /**
     * OnItemSelectedListener for the Category spinner, showing or hiding the CategoryOther field as
     * required.
     */
    private AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // Get the selected item from the parents adapter.
            String selectedItem = parent.getAdapter().getItem(position).toString();

            // If the selected item is not "Other" then hide the other field.
            // Otherwise, make the other field visible and focus on it.
            if (!Objects.equals(selectedItem, getResources().getString(R.string.other))) {
                mCategoryOther.setVisibility(View.INVISIBLE);
            } else {
                mCategoryOther.setVisibility(View.VISIBLE);
                mCategoryOther.setHint(R.string.other_hint);
                mCategoryOther.requestFocus();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    };
}
