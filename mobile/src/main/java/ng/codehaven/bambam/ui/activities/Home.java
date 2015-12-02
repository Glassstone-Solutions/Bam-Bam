package ng.codehaven.bambam.ui.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import butterknife.InjectView;
import io.realm.Realm;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.models.Track;
import ng.codehaven.bambam.services.MusicService;
import ng.codehaven.bambam.services.PlaybackService;
import ng.codehaven.bambam.services.UploadTuneService;
import ng.codehaven.bambam.ui.BaseActivity;
import ng.codehaven.bambam.ui.fragments.TuneListFragment;
import ng.codehaven.bambam.ui.views.CustomTextView;
import ng.codehaven.bambam.ui.views.SlidingTabLayout;
import ng.codehaven.bambam.ui.views.ViewPagerAdapter;

public class Home extends BaseActivity implements ViewPager.OnPageChangeListener,
        View.OnClickListener, TuneListFragment.FragmentInteraction, ServiceConnection{

    @InjectView(R.id.toolbar)
    protected Toolbar mToolBar;
    @InjectView(R.id.pager)
    protected ViewPager pager;
    @InjectView(R.id.tabs)
    protected SlidingTabLayout tabs;
    @InjectView(R.id.trackControl)
    LinearLayout mTrackControl;
    @InjectView(R.id.artThumb)
    ImageView mArtThumb;
    @InjectView(R.id.trackTitle)
    CustomTextView mTrackTitle;
    @InjectView(R.id.trackArtist)
    CustomTextView mTrackArtist;
    @InjectView(R.id.trackSeek)
    SeekBar mSeekBar;

    @InjectView(R.id.play_pause)ImageButton mPlayPause;

    AccelerateDecelerateInterpolator sInterpolator = new AccelerateDecelerateInterpolator();
    ViewPagerAdapter adapter;
    CharSequence Titles[] = {"Home", "Popular", "Search", "Profile"};
    int Numboftabs = 4;
    int currentTab = 0;
    int tabIndicatorColors[] = {
            R.color.ColorPrimary,
            R.color.md_yellow_600,
            R.color.md_purple_600,
            R.color.md_light_green_600
    };
    boolean isFabClicked, mDoneTrackSaveBroadcastReceiver, mTrackProgressBroadcastReceiver, mStartPlayReceiverRegistered;
    @InjectView(R.id.fab)
    FloatingActionButton mFab;
    @InjectView(R.id.addTuneFab)
    FloatingActionButton mAddTuneFab;
    @InjectView(R.id.editFab)
    FloatingActionButton mEditFab;
    private PlaybackService mService;
    private Track mTrack;
    private Realm realm;
    private boolean isPrepared;
    private BroadcastReceiver mStartPlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MusicService.ACTION_START)) {
//                mTrack = realm.where(Track.class).equalTo("parseId", intent.getStringExtra("trackId")).findFirst();
//                showTrackControl();
            }
        }
    };
    private BroadcastReceiver mStopPlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MusicService.ACTION_END)) {
                mTrackControl.setVisibility(View.INVISIBLE);
                mSeekBar.setVisibility(View.INVISIBLE);
            }
        }
    };
    private BroadcastReceiver UploadProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setUploadStatus(true);
        }
    };
    private BroadcastReceiver UploadDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setUploadStatus(false);
        }
    };

    private void showTrackControl() {
        loadTrack();
        animateControlBar();
    }

    private void loadTrack() {
        if (mTrack != null) {
            Picasso.with(this).load(mTrack.getArtUrl()).into(mArtThumb);
            mTrackTitle.setText(mTrack.getTitle());
            mTrackArtist.setText(mTrack.getArtistName());
        }
    }

    private void animateControlBar() {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1f);
        fadeIn.setDuration(300);
        fadeIn.setInterpolator(new DecelerateInterpolator(2.0f));

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTrackControl.setVisibility(View.VISIBLE);
                mSeekBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mTrackControl.startAnimation(fadeIn);
        mSeekBar.startAnimation(fadeIn);

    }

    private void setUploadStatus(boolean b) {
        ProgressBar pb = (ProgressBar) mToolBar.findViewById(R.id.progress_spinner);
        if (b) {
            if (pb.getVisibility() == View.INVISIBLE) pb.setVisibility(View.VISIBLE);
        } else {
            if (pb.getVisibility() == View.VISIBLE) pb.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mTrackProgressBroadcastReceiver) {
            registerReceiver(UploadProgressReceiver, new IntentFilter(UploadTuneService.S3_KEY_PROGRESS_ACTION));
            mTrackProgressBroadcastReceiver = true;
        }
        if (!mDoneTrackSaveBroadcastReceiver) {
            registerReceiver(UploadDoneReceiver, new IntentFilter(UploadTuneService.S3_KEY_DONE_ACTION));
            mDoneTrackSaveBroadcastReceiver = true;
        }

        if (!mStartPlayReceiverRegistered) {
            registerReceiver(mStartPlayReceiver, new IntentFilter(MusicService.ACTION_START));
        }

        registerReceiver(mStopPlayReceiver, new IntentFilter(MusicService.ACTION_END));

        Intent i = new Intent(this, PlaybackService.class);
        bindService(i, this, BIND_AUTO_CREATE);


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTrackProgressBroadcastReceiver) {
            unregisterReceiver(UploadProgressReceiver);
            mTrackProgressBroadcastReceiver = false;
        }
        if (mDoneTrackSaveBroadcastReceiver) {
            unregisterReceiver(UploadDoneReceiver);
            mDoneTrackSaveBroadcastReceiver = false;
        }

        if (mStartPlayReceiverRegistered) {
            unregisterReceiver(mStartPlayReceiver);
        }

        unregisterReceiver(mStopPlayReceiver);

        if (mService != null) {
//            mService.setCallbacks(null);
            unbindService(this);
        }

    }

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

        realm = Realm.getInstance(this);

        sharedPref = getSharedPreferences("tunes", Context.MODE_PRIVATE);
        boolean checkedForTunes = sharedPref.getBoolean("checkedForTunes", false);

        adapter = new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);


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

        animateFabOut();

        isFabClicked = false;

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
                mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_settings_white));
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
            case R.id.addTuneFab:
                startActivity(new Intent(this, CreateTuneActivity.class));
                break;
            case R.id.editFab:
                startActivity(new Intent(this, EditProfile.class));
                break;
        }
    }

    private void handleFabClick() {
        switch (currentTab) {
            case 3:
                if (!isFabClicked) {
                    animateFadeIn();
                } else {
                    animateFabOut();
                }
                break;
            default:
                startActivity(new Intent(this, CreateTuneActivity.class));
                break;
        }
    }

    private void outAnimateFab(FloatingActionButton v, int d) {
        if (v.getVisibility() == View.VISIBLE && v.getId() != R.id.fab) {
            AlphaAnimation aa = new AlphaAnimation(1, 0);
            aa.setInterpolator(sInterpolator);
            aa.setDuration(d);
            aa.setFillAfter(true);
            v.startAnimation(aa);
            v.setVisibility(View.INVISIBLE);
            v.setOnClickListener(null);
        }
    }

    private void inAnimateFAB(FloatingActionButton v, int d) {
        if (v.getVisibility() == View.INVISIBLE && v.getId() != R.id.fab) {
            AlphaAnimation aa = new AlphaAnimation(0, 1);
            aa.setInterpolator(sInterpolator);
            aa.setDuration(d);
            aa.setFillAfter(true);
            v.startAnimation(aa);
            v.setVisibility(View.VISIBLE);
            v.setOnClickListener(this);
        }
    }

    private void animateFadeIn() {
        inAnimateFAB(mAddTuneFab, 300);
        inAnimateFAB(mEditFab, 400);
        isFabClicked = true;
    }

    private void animateFabOut() {
        outAnimateFab(mEditFab, 300);
        outAnimateFab(mAddTuneFab, 400);
        isFabClicked = false;
    }

    /**
     * Save all appropriate fragment state.
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentPage", currentTab);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentTab = savedInstanceState.getInt("currentPage");
        if (currentTab != 3) {
            mAddTuneFab.setVisibility(View.INVISIBLE);
            mEditFab.setVisibility(View.INVISIBLE);
        }
        pager.setCurrentItem(currentTab);
    }

    @Override
    public void commentClicked(View v, Track t) {
        Intent i = new Intent(this, CommentsActivity.class);
        i.putExtra("objectId", t.getParseId());

//        int[] startingLocation = new int[2];
//        v.getLocationOnScreen(startingLocation);
//        i.putExtra(CommentsActivity.ARG_DRAWING_START_LOCATION, startingLocation[1]);
        startActivity(i);
//        overridePendingTransition(0, 0);
    }

    @Override
    public void onCenterIVClick(Track t) {
        if (mService != null) {
            try {
                mService.playTrack(t);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onScroll(int dx, int dy) {

    }

    public FloatingActionButton getFAB() {
        return mFab;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//        mService = ((MusicService.LocalBinder) iBinder).getService();
//        mService.setCallbacks(this);
        mService = ((PlaybackService.MyBinder) iBinder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mService = null;
    }

//    @Override
//    public void isPrepared(Boolean p) {
//        isPrepared = p;
//    }
//
//    @Override
//    public void getTrack(Track t) {
//        if (t != null) {
//            mTrack = t;
//        }
//    }

//    @Override
//    public void getProgress(int progress) {
//
//        do {
//            updateSeekBar(progress);
//        } while (isPrepared);
//    }

//    @Override
//    public void playState(Track t, int state, int progress, int max) {
//
//        switch (state){
//            case MusicService.TRACK_LOADING:
//                mTrack = t;
////                showTrackControl();
//                if (mTrack != null) {
//                    showPlaybackControls();
//                }
//                break;
//            case MusicService.TRACK_PREPARED:
//            case MusicService.TRACK_PLAYING:
//                mPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_pause));
//                mSeekBar.setProgress(progress);
//                mSeekBar.setMax(max);
//                break;
//            case MusicService.TRACK_DONE:
//                mTrackControl.setVisibility(View.INVISIBLE);
//                mSeekBar.setVisibility(View.INVISIBLE);
//                break;
//        }
//    }

    private void updateSeekBar(int progress) {
        mSeekBar.setProgress(progress);
    }
}
