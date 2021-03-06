package com.ulternate.paycat.activities;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ulternate.paycat.R;
import com.ulternate.paycat.data.Transaction;
import com.ulternate.paycat.data.Utils;
import com.ulternate.paycat.tasks.DeleteTransactionAsyncTask;
import com.ulternate.paycat.tasks.UpdateTransactionAsyncTask;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import fr.ganfra.materialspinner.MaterialSpinner;

/**
 * Detail activity for the application, showing information for a single transaction.
 */
public class DetailActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, OnMapReadyCallback {

    // The Transaction being viewed/edited.
    private Transaction mTransaction;

    private FloatingActionButton mEditActionButton;

    // The following widgets enable editing of the Transaction.
    private TextInputEditText mAmount;
    private TextInputEditText mDescription;
    private TextInputEditText mCategoryOther;
    private TextInputLayout mCategoryOtherLayout;
    private TextView mDate;
    private MaterialSpinner mCategory;

    // Fields for widgets and widget values.
    private List<TextInputEditText> mTextWidgets;
    private List<String> mCategories = new ArrayList<>();
    private Float mAmountVal;
    private String mDescriptionVal;
    private String mCategorySelection;
    private String mCategoryOtherVal;
    private Date mDateVal;

    // Fields for editing status.
    private boolean mEditingEnabled = false;

    // Fields for the date and time pickers.
    private FragmentManager mFragmentManager;
    private Calendar mInitialCalendar;
    private Calendar mChosenCalendar;

    // InputMethodManager used to force the keyboard to show on focus.
    private InputMethodManager mInputMethodManager;

    private SharedPreferences mPrefs;

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
        mCategoryOtherLayout = findViewById(R.id.transactionCategoryOtherLayout);

        // Group similar widgets.
        mTextWidgets = Arrays.asList(mAmount, mDescription, mCategoryOther);

        // Get the DefaultSharedPreferences.
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Get a Calendar instance to store the initial Transaction date.
        mInitialCalendar = Calendar.getInstance();

        // Get all the categories, both the default and custom categories.
        String[] originalCategories = getResources().getStringArray(R.array.default_categories);
        String[] extraCategories = {
                getResources().getString(R.string.category_unknown),
                getResources().getString(R.string.category_other)};
        mCategories = Utils.getCategories(originalCategories, mPrefs, extraCategories);

        // Set the adapter for the spinner.
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mCategories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategory.setAdapter(spinnerAdapter);

        // Set the initial values of all widgets.
        Intent mCallingIntent = getIntent();
        mTransaction = (Transaction) mCallingIntent.getSerializableExtra("transaction");
        if (mTransaction != null) {
            setInitialValues(mTransaction);
        }

        // Get the inputMethodManager.
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Disable editing initially.
        disableEditing();

        // Get the FloatingActionButtons and set the OnClickListeners.
        mEditActionButton = findViewById(R.id.fab);
        mEditActionButton.setOnClickListener(mEditFabOnClickListener);
        FloatingActionButton mDeleteActionButton = findViewById(R.id.deleteFab);
        mDeleteActionButton.setOnClickListener(mDeleteFabOnClickListener);

        // The following are used by the date and time pickers.
        mFragmentManager = getFragmentManager();

        // Show the mapFragment and use the Transaction location if the permission was granted and
        // the location is not set to the default 0.0.
        boolean hasLocationAccess = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (hasLocationAccess && mTransaction.latitude != 0.0 && mTransaction.longitude != 0.0) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapView);
            mapFragment.getMapAsync(this);
        } else {
            // Hide the mapFragment and other UI elements.
            findViewById(R.id.mapImage).setVisibility(View.GONE);
            findViewById(R.id.mapView).setVisibility(View.GONE);
            findViewById(R.id.mapLabel).setVisibility(View.GONE);
        }
    }

    /**
     * Handle the Date selection from the DatePickerDialog.
     *
     * The TimePickerDialog is shown after a date is selected.
     *
     * Store the selected date in a Calendar object to be retrieved later to update the Transaction.
     *
     * @param view: The DatePickerDialog view.
     * @param year: int, representing the selected year.
     * @param monthOfYear: int, representing the selected month of the year (note, this is 0 based).
     * @param dayOfMonth: int, representing the selected day of the month.
     */
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        // Set the date on the Calendar instance.
        mChosenCalendar = Calendar.getInstance();
        mChosenCalendar.set(Calendar.YEAR, year);
        mChosenCalendar.set(Calendar.MONTH, monthOfYear);
        mChosenCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        // Build and show the TimePickerDialog.
        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                this,
                mInitialCalendar.get(Calendar.HOUR_OF_DAY),
                mInitialCalendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show(mFragmentManager, "TimePickerDialog");
    }

    /**
     * Handle the Time selection from the TimePickerDialog.
     *
     * Update the mDate TextView with the chosen Date and Time (stored in a Calendar instance).
     *
     * @param view: The TimePickerDialog view.
     * @param hourOfDay: int, representing the selected hour of the day.
     * @param minute: int, representing the selected minute.
     * @param second: int, representing the selected second.
     */
    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        // Set the time on the Calendar instance.
        mChosenCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mChosenCalendar.set(Calendar.MINUTE, minute);
        mChosenCalendar.set(Calendar.SECOND, second);

        // Update the text field with the chosen date and time.
        mDate.setText(MainActivity.TRANSACTION_DATE_FORMAT.format(mChosenCalendar.getTime()));
    }

    /**
     * Set the values for all widgets from the Transaction information sent in the calling Intent.
     * @param transaction: The transaction sent by the calling Intent.
     */
    private void setInitialValues(Transaction transaction) {
        mAmount.setText(String.valueOf(transaction.amount));
        mDescription.setText(transaction.description);
        mDate.setText(MainActivity.TRANSACTION_DATE_FORMAT.format(transaction.date));
        // Set the initial Calendar instance time to the Transaction date.
        mInitialCalendar.setTime(transaction.date);

        // Set the category spinner, filling in the Category "Other" EditText if the category
        // can't be found in the list of values in the spinner.
        String initialCategory = transaction.category;
        // Note, MaterialSpinner prepends the hint to the list so we must add 1 to the position for
        // selection to select the correct item.
        if (mCategories.contains(initialCategory)) {
            mCategory.setSelection(mCategories.indexOf(initialCategory) + 1);
        } else {
            mCategory.setSelection(mCategories.indexOf(getResources().getString(R.string.category_other)) + 1);

            // Make mCategoryOther visible and set the text.
            mCategoryOtherLayout.setVisibility(View.VISIBLE);
            mCategoryOther.setText(initialCategory);
        }
    }

    /**
     * Validate the fields prior to saving the values.
     * The values are stored so we can save them to the Transaction object if they are valid.
     * If they are not valid, then an error is added to the field that failed validation.
     * @return true if all checks are valid, else false.
     */
    private boolean areChangesValid() {
        // Get the entered values.
        try {
            mAmountVal = Float.parseFloat(mAmount.getText().toString());
        } catch (NumberFormatException e) {
            mAmountVal = (float) 0.0;
        }
        mDescriptionVal = mDescription.getText().toString();
        mCategoryOtherVal = mCategoryOther.getText().toString();

        // Any field that is not valid will set this to false.
        boolean isValid = true;

        // The Transaction amount will be 0.0 if the EditText was empty when the user hit save.
        if (mAmountVal == 0.0) {
            mAmount.setError(getResources().getString(R.string.amount_error));
            isValid = false;
        }

        // Description field cannot be empty.
        if (mDescriptionVal.isEmpty()) {
            mDescription.setError(getResources().getString(R.string.description_error));
            isValid = false;
        }

        // Category spinner has a hint that could be selected, this returns a NullPointerException
        // with the MaterialSpinner library as the hint is at index -1.
        try {
            mCategorySelection = mCategory.getSelectedItem().toString();
        } catch (NullPointerException e) {
            mCategory.setError(getResources().getString(R.string.category_error));
            mCategorySelection = "";
            isValid = false;
        }

        // Category Other field cannot be empty if the selected category is "Other". Note, the
        // spinner doesn't allow empty selections.
        if (Objects.equals(mCategorySelection, getResources().getString(R.string.category_other))) {
            if (mCategoryOtherVal.isEmpty()) {
                mCategoryOther.setError(getResources().getString(R.string.category_other_error));
                isValid = false;
            }
        }

        // Try and parse the date, if it fails, then it's not a valid selection.
        try {
            mDateVal = MainActivity.TRANSACTION_DATE_FORMAT.parse(mDate.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
            mDate.setError(getResources().getString(R.string.date_error));
            isValid = false;
        }

        return isValid;
    }

    /**
     * Update the Transaction object with the latest valid values. This is called when the
     * FloatingActionButton is clicked and all fields are valid.
     */
    private void updateTransactionValues() {
        mTransaction.amount = mAmountVal;
        mTransaction.description = mDescriptionVal;

        if (Objects.equals(mCategorySelection, getResources().getString(R.string.category_other))) {
            mTransaction.category = mCategoryOtherVal;
            // Update the list of categories.
            updateCategories(mCategoryOtherVal);
        } else {
            mTransaction.category = mCategorySelection;
        }

        // Update the date and Calendar instance.
        mTransaction.date = mDateVal;
        mInitialCalendar.setTime(mDateVal);

        // Update the transaction in the database.
        new UpdateTransactionAsyncTask(getApplicationContext()).execute(mTransaction);
    }

    /**
     * Add the new custom category to the preferences.
     * @param newCategory: The custom category saved by the user.
     */
    private void updateCategories(String newCategory) {

        // Get the existing custom_categories.
        String custom_categories_array = mPrefs.getString(MainActivity.PREFS_CUSTOM_CATEGORIES_ARRAY, "");

        if (!custom_categories_array.isEmpty()) {
            String[] custom_categories = custom_categories_array.split("\\|");
            ArrayList<String> temp = new ArrayList<>(Arrays.asList(custom_categories));
            if (!temp.contains(newCategory)) {
                custom_categories_array = custom_categories_array + "|" + newCategory;
            }
        } else {
            custom_categories_array = newCategory;
        }

        mPrefs.edit().putString(MainActivity.PREFS_CUSTOM_CATEGORIES_ARRAY, custom_categories_array).apply();
    }

    /**
     * Disable the edit functionality of the widgets.
     */
    private void disableEditing() {
        // Disable editing of each EditText widget.
        for (TextInputEditText editText: mTextWidgets) {
            editText.setFocusable(false);
            editText.setTextIsSelectable(false);
            editText.setCursorVisible(false);
            editText.setInputType(InputType.TYPE_NULL);
            editText.setOnClickListener(mDisabledOnClickListener);
        }

        // Disable single line for the description widget. To show multi-line descriptions properly.
        mDescription.setSingleLine(false);

        // Show a warning when the user clicks on the date field.
        mDate.setOnClickListener(mDisabledOnClickListener);

        // Disable selections in the spinner widget.
        mCategory.setClickable(false);
        mCategory.setOnTouchListener(mDisabledOnTouchListener);

        // Hide the keyboard.
        mInputMethodManager.hideSoftInputFromWindow(mAmount.getWindowToken(), 0);

        // Mark that editing was disabled.
        mEditingEnabled = false;
    }

    /**
     * Enable the edit functionality of all widgets.
     */
    private void enableEditing() {
        // Enable editing of the EditText widgets.
        for (TextInputEditText editText: mTextWidgets) {
            editText.setFocusable(true);
            editText.setTextIsSelectable(true);
            editText.setCursorVisible(true);
            editText.setOnClickListener(mEnabledOnClickListener);
            editText.setOnFocusChangeListener(mEnabledOnFocusChangeListener);
        }

        // Set the input types for the fields.
        mAmount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mDescription.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        mCategoryOther.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        // Set a different onClickListener for the date field to use the date and time pickers.
        mDate.setOnClickListener(mEnabledDateOnClickListener);

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
     * OnClickListener for the FloatingActionButton.
     *
     * Handles changing the FAB icon and content description based on the action next to be
     * performed (i.e. change to "Save" when editing).
     *
     * Clicking when editing is enabled will save the changes, checking for errors and updating the
     * transaction if it is valid.
     *
     * Clicking when editing is disabled will unlock the fields for editing.
     */
    private View.OnClickListener mEditFabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mEditingEnabled) {
                // Check if the edit was valid, if it is, save the changes.
                if (areChangesValid()) {
                    // Disable the editing functionality and change the icon to the edit icon.
                    disableEditing();
                    mEditActionButton.setImageResource(R.drawable.ic_edit_black_24dp);
                    mEditActionButton.setContentDescription(getResources().getString(R.string.edit));

                    // Update the Transaction values.
                    updateTransactionValues();

                    // Show a message that the changes have been saved.
                    showSnackBar(v, getResources().getString(R.string.changes_saved),
                            Snackbar.LENGTH_SHORT, "Changes Saved");
                }
            } else {
                // Enable editing and change the icon to the save icon.
                enableEditing();
                mEditActionButton.setImageResource(R.drawable.ic_save_black_24dp);
                mEditActionButton.setContentDescription(getResources().getString(R.string.save));

                // Show a message mentioning that editing is enabled.
                showSnackBar(v, getResources().getString(R.string.editing_in_progress),
                        Snackbar.LENGTH_SHORT, "Editing Enabled");
            }
        }
    };

    /**
     * OnClickListener for the delete Floating Action Button, builds and shows an AlertDialog to
     * warn the user and seek confirmation prior to deleting the Transaction.
     */
    private View.OnClickListener mDeleteFabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            buildDeleteTransactionAlertDialog().show();
        }
    };

    /**
     * OnClickListener that warns the user that the editing is disabled until the edit button is
     * clicked. Assigned to the widgets when they are disabled.
     */
    private View.OnClickListener mDisabledOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showSnackBar(v, getResources().getString(R.string.editing_locked_warning),
                    Snackbar.LENGTH_SHORT, "Editing Disabled");
        }
    };

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
     * OnClickListener used when editing is enabled to replace the mDisabledOnTouchListener so the
     * user doesn't continue to be warned that editing is disabled.
     */
    private View.OnClickListener mEnabledOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Do Nothing.
        }
    };

    /**
     * Force the Soft keyboard to show on focus changed for the EditText fields.
     */
    private View.OnFocusChangeListener mEnabledOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            mInputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        }
    };

    /**
     * OnClickListener used by the Date TextView to show the DatePickerDialog.
     */
    private View.OnClickListener mEnabledDateOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Hide any open soft keyboards.
            mInputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            // Build and show the DatePickerDialog, dismissing on selection. The selected date uses
            // the initial Transaction date.
            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                    DetailActivity.this,
                    mInitialCalendar.get(Calendar.YEAR),
                    mInitialCalendar.get(Calendar.MONTH),
                    mInitialCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.autoDismiss(true);
            datePickerDialog.show(mFragmentManager, "DatePickerDialog");
        }
    };

    /**
     * OnTouchListener used when editing is enabled to replace the mDisabledOnTouchListener so the
     * user doesn't continue to be warned that editing is disabled. Used by the Spinner.
     */
    private View.OnTouchListener mEnabledOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Hide any open soft keyboards.
            mInputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
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
            // The hint is added to the list, selecting it returns a position of -1.
            if (position >= 0) {
                // Get the selected item from the parents adapter.
                String selectedItem = parent.getAdapter().getItem(position).toString();

                // If the selected item is not "Other" then hide the other layout.
                // Otherwise, make the other field visible and focus on the input field within it.
                if (!Objects.equals(selectedItem, getResources().getString(R.string.category_other))) {
                    mCategoryOtherLayout.setVisibility(View.GONE);
                    // Hide any soft inputs shown.
                    mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                } else {
                    mCategoryOtherLayout.setVisibility(View.VISIBLE);
                    mCategoryOther.requestFocus();
                }
            } else {
                // Ensure the "Other" field is not visible.
                mCategoryOtherLayout.setVisibility(View.GONE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    };

    /**
     * Build and return an AlertDialog that warns and enables the user to delete the Transaction.
     * @return an AlertDialog. Positive action will delete the Transaction and finish the activity.
     *    Negative action will dismiss the dialog.
     */
    private AlertDialog buildDeleteTransactionAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.delete_transaction);
        alertDialogBuilder.setMessage(R.string.delete_transaction_message);
        alertDialogBuilder.setPositiveButton(R.string.delete,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handle click of "Delete" button.
                        if (mTransaction != null) {
                            // Create the return intent and send the original transaction back with
                            // it to enable the user to undo deleting the transaction.
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("transaction", mTransaction);
                            setResult(RESULT_OK, returnIntent);

                            // Delete the transaction and finish the activity.
                            new DeleteTransactionAsyncTask(getApplicationContext()).execute(mTransaction);
                            finish();
                        } else {
                            dialog.dismiss();
                        }
                    }
                });
        alertDialogBuilder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return(alertDialogBuilder.create());
    }

    /**
     * Callback for when the map is ready.
     * @param googleMap: The GoogleMap object.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Set the location from the Transaction and move the camera to it.
        LatLng location = new LatLng(mTransaction.latitude, mTransaction.longitude);
        googleMap.addMarker(new MarkerOptions().position(location)
                .title(mTransaction.description));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));

        // Disable controls for the Map.
        googleMap.getUiSettings().setAllGesturesEnabled(false);
    }
}
