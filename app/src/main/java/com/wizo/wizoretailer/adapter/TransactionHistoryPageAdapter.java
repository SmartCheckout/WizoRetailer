package com.wizo.wizoretailer.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.wizo.wizoretailer.activity.PastTransactionFragment;
import com.wizo.wizoretailer.activity.PendingTransactionFragment;

/**
 * Created by Yesh on 2/25/2018.
 */

public class TransactionHistoryPageAdapter extends FragmentStatePagerAdapter{

    int mNumOfTabs;

    public TransactionHistoryPageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new PendingTransactionFragment();
            case 1:
                return new PastTransactionFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return "Paid";
            case 1:
                return "Approved";
            default:
                return null;
        }
    }

}
