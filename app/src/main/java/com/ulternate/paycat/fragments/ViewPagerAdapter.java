package com.ulternate.paycat.fragments;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ulternate.paycat.data.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * FragmentPagerAdapter for the ViewPager.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    // Lists to store the Fragments and their titles.
    private final List<Fragment> mFragmentsList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    /**
     * Constructor for the FragmentPagerAdapter.
     * @param fm: The FragmentManager.
     */
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * Get the Fragment at the given index/position.
     * @param position: int, index of Fragment in the ViewPager.
     * @return the Fragment at the specified position.
     */
    @Override
    public Fragment getItem(int position) {
        return mFragmentsList.get(position);
    }

    /**
     * Get the total number of pages in the ViewPager.
     * @return the size of the mFragmentsList.
     */
    @Override
    public int getCount() {
        return mFragmentsList.size();
    }

    /**
     * Add a Fragment to the mFragmentsList.
     * @param fragment: The Fragment to add to the list.
     * @param title: The title used in the TabLayout.
     */
    public void addFragment(Fragment fragment, String title) {
        mFragmentsList.add(fragment);
        mFragmentTitleList.add(title);
    }

    /**
     * Get the page title for the given position.
     * @param position: int, index of page in the ViewPager.
     * @return the title of the specified Page.
     */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    /**
     * Update all Fragments with the new list of Transactions (i.e. from filtering Transactions).
     * @param transactions: The list of Transaction objects.
     */
    public void updateFragments(List<Transaction> transactions) {
        for (int i = 0; i < this.getCount(); i++) {
            BaseTransactionFragment fragment = (BaseTransactionFragment) this.getItem(i);
            if (fragment != null) {
                fragment.updateAdapter(transactions);
            }
        }
    }
}
