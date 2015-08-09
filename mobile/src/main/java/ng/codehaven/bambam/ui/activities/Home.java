package ng.codehaven.bambam.ui.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import butterknife.InjectView;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.ui.BaseActivity;
import ng.codehaven.bambam.ui.views.SlidingTabLayout;
import ng.codehaven.bambam.ui.views.ViewPagerAdapter;

public class Home extends BaseActivity {

    ViewPagerAdapter adapter;
    CharSequence Titles[]={"Home","Popular", "Search", "Profile"};
    int Numboftabs =4;

    int tabIndicatorColors[] = {
            R.color.ColorPrimary,
            R.color.md_yellow_600,
            R.color.md_purple_600,
            R.color.md_light_green_600
    };

    @InjectView(R.id.toolbar) protected Toolbar mToolBar;
    @InjectView(R.id.pager) protected ViewPager pager;
    @InjectView(R.id.tabs) protected SlidingTabLayout tabs;

    @Override
    public int getActivityResourceId() {
        return R.layout.activity_home;
    }

    @Override
    public boolean hasToolBar() {
        return true;
    }

    @Override
    public Toolbar getToolBar() {
        return mToolBar;
    }

    @Override
    public int getMenuResourceId() {
        return R.menu.menu_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        adapter = new ViewPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs);

        pager.setAdapter(adapter);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width
        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                if (position <= tabIndicatorColors.length)
                    return getResources().getColor(tabIndicatorColors[position]);
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

    }

}
