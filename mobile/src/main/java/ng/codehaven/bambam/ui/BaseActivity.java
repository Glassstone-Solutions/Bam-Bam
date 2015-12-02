package ng.codehaven.bambam.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.models.Track;
import ng.codehaven.bambam.ui.activities.DispatchActivity;
import ng.codehaven.bambam.ui.fragments.PlaybackControlsFragment;
import ng.codehaven.bambam.utils.FontCache;
import ng.codehaven.bambam.utils.IntentUtil;
import ng.codehaven.bambam.utils.LogHelper;
import ng.codehaven.bambam.utils.Logger;
import ng.codehaven.bambam.utils.NetworkHelper;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.makeLogTag(BaseActivity.class);

    protected ParseUser mCurrentUser;
    protected List<ParseUser> pUsers;
    protected Logger mLogger;
    protected SharedPreferences sharedPref;
    protected SharedPreferences.Editor editor;
    protected TextView mToolBarTitle;
    protected ProgressBar mProgressBar;


    protected FragmentManager fragmentManager;
    protected FragmentTransaction fragmentTransaction;
    protected Fragment f;

    private PlaybackControlsFragment mControlsFragment;

    public abstract int getActivityResourceId();

    public abstract boolean hasToolBar();

    public abstract Toolbar getToolBar();

    public abstract int getMenuResourceId();

    public abstract boolean enableBack();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getActivityResourceId());

        ButterKnife.inject(this);

        if (hasToolBar()) {
            setupToolBar(getToolBar());
            mToolBarTitle = (TextView) getToolBar().findViewById(R.id.toolbar_title);
            mToolBarTitle.setTypeface(FontCache.get("fonts/GrandHotel-Regular.otf", this));
            mToolBarTitle.setText(getString(R.string.app_name));
            mToolBarTitle.setTextColor(getResources().getColor(R.color.ColorPrimary));

            mProgressBar = (ProgressBar) getToolBar().findViewById(R.id.progress_spinner);
        }

        // Set current user
        mCurrentUser = ParseUser.getCurrentUser();

        // Setup Logger

        // Setup Shared Pref

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        // Fragment Init

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mControlsFragment = (PlaybackControlsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_playback_controls);
        if (mControlsFragment == null) {
            throw new IllegalStateException("Mising fragment with id 'controls'. Cannot continue.");
        }

        hidePlaybackControls();

    }

    protected void hidePlaybackControls() {
        Log.e(TAG, "hidePlaybackControls");
        getSupportFragmentManager().beginTransaction()
                .hide(mControlsFragment)
                .commit();
    }

    protected void showPlaybackControls() {
        Log.e(TAG, "showPlaybackControls");
        if (NetworkHelper.isOnline(this)) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.fade_in, R.anim.fade_out,
                            R.anim.fade_in, R.anim.fade_out)
                    .show(mControlsFragment)
                    .commit();
        } else {
            Snackbar.make(mToolBarTitle, "Error", Snackbar.LENGTH_LONG).show();
        }

    }

    private void setupToolBar(Toolbar toolBar) {
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(enableBack());
    }

    public List<ParseUser> checkUserName(String username) throws ParseException {
        ParseQuery<ParseUser> u = ParseUser.getQuery();
        u.whereEqualTo("username", username);
        return u.find();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(getMenuResourceId(), menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //TODO: Remember to add cases of each menu item created
//        if (id == R.id.action_settings) {
//            return true;
//        }
        switch (id) {
            case R.id.action_signout:
                signout();
                finish();
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signout() {
        ParseUser.logOut();
        mCurrentUser = null;

        IntentUtil iUtil = new IntentUtil(this);

        SharedPreferences sp = getSharedPreferences("tunes", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();

        Realm realm = Realm.getInstance(this);

        RealmResults<Track> t = realm.where(Track.class).findAll();

        realm.beginTransaction();
        t.clear();
        realm.commitTransaction();

        Intent i = new Intent(this, DispatchActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        iUtil.goToActivity(i);

    }
}
