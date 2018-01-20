package com.ulternate.paycat.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ulternate.paycat.R;
import com.ulternate.paycat.data.Transaction;
import com.ulternate.paycat.data.TransactionViewModel;
import com.ulternate.paycat.fragments.BreakdownFragment;
import com.ulternate.paycat.fragments.TransactionFragment;
import com.ulternate.paycat.fragments.ViewPagerAdapter;
import com.ulternate.paycat.settings.GeneralSettings;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Main activity for the application.
 */
public class MainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private static final int REQUEST_PERMISSIONS_LOCATION_CODE = 2;

    private LifecycleOwner mLifecycleOwner;
    private TransactionViewModel mTransactionViewModel;
    private View mView;
    private ViewPagerAdapter mViewPagerAdapter;

    private boolean mSelectingFrom = true;
    private Date mFromDate;
    private Date mToDate;

    // Date form used to format Date objects as desired.
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat TRANSACTION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd h:mm a");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the main layout view for showing snackbars.
        mView = findViewById(android.R.id.content);

        // Set the LifecycleOwner, used to set Observers in the DatePickerDialog.
        mLifecycleOwner = this;

        // Get the ViewPager, set the adapter and add the required fragments.
        ViewPager viewPager = findViewById(R.id.viewpager);
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        // Add Transactions list fragment.
        mViewPagerAdapter.addFragment(new TransactionFragment(), getResources().getString(R.string.tab_transactions));
        // Add BreakdownItem fragment.
        mViewPagerAdapter.addFragment(new BreakdownFragment(), getResources().getString(R.string.tab_breakdown));
        viewPager.setAdapter(mViewPagerAdapter);

        // Set up the TabLayout.
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        // Prompt the user to enable the notification listener service if they haven't.
        if (!isNotificationServiceEnabled()) {
            buildNotificationServiceAlertDialog().show();
        }

        // Check access to the location permission, requesting access if required.
        checkLocationPermission();

        // Get an instance of the TransactionViewModel to be used for all subclasses.
        mTransactionViewModel = ViewModelProviders.of(this).get(
                TransactionViewModel.class);
    }

    /**
     * Inflate the menu in the app toolbar.
     * @param menu: the menu resource.
     * @return true to display the menu, false to not show it.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handle menu item selections.
     * @param item: The MenuItem that was selected.
     * @return boolean Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                // Open the settings activity.
                Intent settingsIntent = new Intent(this, GeneralSettings.class);
                startActivity(settingsIntent);
                return true;
            case R.id.menu_date_filter:
                // Launch a date picker dialog to filter the Transactions between two dates.
                buildAndShowDatePickerDialog(true);
                return true;
            case R.id.menu_clear_date_filter:
                // Remove the filtered Transactions list observers and get all Transactions.
                clearTransactionFilter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Clear the filter on the Transactions list, removing any Observers of the filtered Transaction
     * list and observing changes to the full Transactions list from the ViewModel, updating all
     * Fragments in the ViewPager.
     */
    public void clearTransactionFilter() {
        // Remove any observers for the filtered Transactions list.
        mTransactionViewModel.getFilteredTransactionsList(null, null).removeObservers(this);

        // Get and observe the Transactions list for changes.
        mTransactionViewModel.getTransactionsList().observe(this,
                new Observer<List<Transaction>>() {
                    @Override
                    public void onChanged(@Nullable List<Transaction> transactions) {
                        mViewPagerAdapter.updateFragments(transactions);
                    }
                });
    }

    /**
     * Build and show a DatePickerDialog to get the dates to filter Transactions by.
     * @param isFirstPicker: If true, then this is the first dialog, representing the "From" date
     *                     in the filter range, otherwise the user is selecting the "To" date.
     */
    private void buildAndShowDatePickerDialog(boolean isFirstPicker) {
        Calendar mInitialCalendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                mDateSetListener,
                mInitialCalendar.get(Calendar.YEAR),
                mInitialCalendar.get(Calendar.MONTH),
                mInitialCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.autoDismiss(true);

        // Set the title of the DatePicker.
        if (isFirstPicker) {
            datePickerDialog.setTitle(getResources().getString(R.string.date_from));
        } else {
            datePickerDialog.setTitle(getResources().getString(R.string.date_to));
        }

        datePickerDialog.show(getFragmentManager(), "DatePickerDialog");
    }

    /**
     * Handle the selection of the dates in the DatePickerDialog. Upon selecting the "To" date the
     * Transactions list will be filtered in all Fragments in the ViewPager.
     */
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            Calendar mInitialCalendar = Calendar.getInstance();
            mInitialCalendar.set(year, monthOfYear, dayOfMonth);

            if (mSelectingFrom) {
                // Set the "From" date and show another DatePickerDialog to select the "To" date.
                mFromDate = mInitialCalendar.getTime();
                mSelectingFrom = false;
                buildAndShowDatePickerDialog(false);
            } else {
                // Select the "To" date and filter the Transactions list.
                mToDate = mInitialCalendar.getTime();
                mSelectingFrom = true;

                // Remove any observers on the full Transactions list and observe the filtered list.
                // Update all Fragments in the ViewPager with the filtered list.
                mTransactionViewModel.getTransactionsList().removeObservers(mLifecycleOwner);
                mTransactionViewModel.getFilteredTransactionsList(mFromDate, mToDate).observe(mLifecycleOwner, new Observer<List<Transaction>>() {
                    @Override
                    public void onChanged(@Nullable List<Transaction> transactions) {
                        mViewPagerAdapter.updateFragments(transactions);
                    }
                });
            }
        }
    };

    /**
     * Check if the notification service is enabled.
     * Verifies if the notification listener service is enabled.
     * @return True if eanbled, false otherwise.
     *
     * Example from: https://github.com/Chagall/notification-listener-service-example
     */
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     *
     * Example from: https://github.com/Chagall/notification-listener-service-example
     */
    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO warn the user of reduced functionality if dismissed before enabling.
                    }
                });
        return(alertDialogBuilder.create());
    }

    /**
     * Check if the location permissions have been granted, if not then determine if the permission
     * should be asked for using the shouldShowRequestPermissionRationale.
     *
     * Note, the location isn't access or recorded in this activity, it is recorded in the
     * TransactionNotificationListener service which also checks if the permission is granted. The
     * request can't be in the service as it is not shown to the user.
     */
    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Ask the user to grant the location permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.location_permission_title)
                        .setMessage(R.string.location_permission_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_PERMISSIONS_LOCATION_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSIONS_LOCATION_CODE);
            }
        }
    }

    /**
     * Handle the result from a permission request.
     * @param requestCode: The request code used to signify which permission was requested.
     * @param permissions: Array of permissions requested.
     * @param grantResults: Array of the results of each request for a permission.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_LOCATION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        // Notify the user that the permission was successfully granted.
                        Snackbar.make(mView,
                                getResources().getString(R.string.location_permission_granted),
                                Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    // Alert the user that the location of the transaction won't be saved when a
                    // notification is received.
                    Snackbar.make(mView,
                            getResources().getString(R.string.location_permission_denied),
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }
}
