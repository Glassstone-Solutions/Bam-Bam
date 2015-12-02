package ng.codehaven.bambam.ui.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.parse.ParseUser;

import ng.codehaven.bambam.ui.fragments.PopularFragment;
import ng.codehaven.bambam.ui.fragments.ProfileFragment;
import ng.codehaven.bambam.ui.fragments.SearchFragment;
import ng.codehaven.bambam.ui.fragments.TuneListFragment;

/**
 * Created by Thompson on 28/07/2015.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created

    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {

        Fragment f = null;

        switch (position){
            case 0:
                f = new TuneListFragment();
                break;
            case 1:
                f = new PopularFragment();
                break;
            case 2:
                f = new SearchFragment();
                break;
            case 3:
                Bundle b = new Bundle();
                b.putString("userId", ParseUser.getCurrentUser().getObjectId());
                b.putBoolean("isLocal", true);
                f = ProfileFragment.newInstance(b);
                break;
        }

        return f;
    }

    /**
     * This method may be called by the ViewPager to obtain a title string
     * to describe the specified page. This method may return null
     * indicating no title for this page. The default implementation returns
     * null.
     *
     * @param position The position of the title requested
     * @return A title for the requested page
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return Titles.length;
    }
}
