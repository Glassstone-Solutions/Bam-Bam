package ng.codehaven.bambam.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.InjectView;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.ui.BaseActivity;
import ng.codehaven.bambam.ui.views.SlidingTabLayout;
import ng.codehaven.bambam.ui.views.ViewPagerAdapter;

public class Home extends BaseActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    @InjectView(R.id.toolbar)
    protected Toolbar mToolBar;
    @InjectView(R.id.pager)
    protected ViewPager pager;
    @InjectView(R.id.tabs)
    protected SlidingTabLayout tabs;
    ViewPagerAdapter adapter;
    CharSequence Titles[]={"Home","Popular", "Search", "Profile"};
    int Numboftabs =4;
    int currentTab = 0;
    int tabIndicatorColors[] = {
            R.color.ColorPrimary,
            R.color.md_yellow_600,
            R.color.md_purple_600,
            R.color.md_light_green_600
    };
    @InjectView(R.id.fab)
    FloatingActionButton mFab;

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
    public boolean enableBack() {
        return false;
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

        pager.addOnPageChangeListener(this);
        mFab.setOnClickListener(this);

    }

    /**
     * This method will be invoked when the current page is scrolled, either as part
     * of a programmatically initiated smooth scroll or a user initiated touch scroll.
     *
     * @param position             Position index of the first page currently being displayed.
     *                             Page position+1 will be visible if positionOffset is nonzero.
     * @param positionOffset       Value from [0, 1) indicating the offset from the page at position.
     * @param positionOffsetPixels Value in pixels indicating the offset from position.
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * This method will be invoked when a new page becomes selected. Animation is not
     * necessarily complete.
     *
     * @param position Position index of the new selected page.
     */
    @Override
    public void onPageSelected(int position) {

        currentTab = position;

        switch (position) {
            case 3:
                mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_edit_white));
                break;
            default:
                mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_white));
                break;
        }

    }

    /**
     * Called when the scroll state changes. Useful for discovering when the user
     * begins dragging, when the pager is automatically settling to the current page,
     * or when it is fully stopped/idle.
     *
     * @param state The new scroll state.
     * @see ViewPager#SCROLL_STATE_IDLE
     * @see ViewPager#SCROLL_STATE_DRAGGING
     * @see ViewPager#SCROLL_STATE_SETTLING
     */
    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.fab:
                handleFabClick();
                break;
        }
    }

    private void handleFabClick() {
        switch (currentTab) {
            case 3:
                startActivity(new Intent(this, EditProfile.class));
                break;
        }
    }
}
