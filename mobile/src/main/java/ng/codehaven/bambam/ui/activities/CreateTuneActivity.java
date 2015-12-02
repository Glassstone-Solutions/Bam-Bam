package ng.codehaven.bambam.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import butterknife.InjectView;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.services.UploadTuneService;
import ng.codehaven.bambam.ui.BaseActivity;
import ng.codehaven.bambam.ui.fragments.ChooseArtworkFragment;
import ng.codehaven.bambam.ui.fragments.ChooseMP3Fragment;
import ng.codehaven.bambam.ui.fragments.TrackDetailsFragment;

public class CreateTuneActivity
        extends BaseActivity
        implements
        ChooseArtworkFragment.OnFragmentInteractionListener,
        ChooseMP3Fragment.OnFragmentInteractionListener,
        TrackDetailsFragment.OnDetailsFragmentInterface {

    public static final int PICK_ART_REQUEST = 0;
    public static final int PICK_MP3_REQUEST = 1;

    @InjectView(R.id.toolbar)
    protected Toolbar mToolBar;

    @InjectView(R.id.content)
    FrameLayout mContent;

    Bundle bundle;
    int f_id = 0;
    boolean mHasImage;

    @Override
    public int getActivityResourceId() {
        return R.layout.activity_create_tune;
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
        return R.menu.menu_create_tune;
    }

    @Override
    public boolean enableBack() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            bundle = new Bundle(savedInstanceState.getBundle("trackBundle"));
            f_id = bundle.getInt("cFrag", 0);
        } else {
            bundle = new Bundle();
        }



        if (f_id == 0) {
            StartFragment(new ChooseArtworkFragment(), 0);
        } else {
            switch (f_id) {
                case 1:
                    bundle.putString("artURI", savedInstanceState.getString("artURI"));
                    StartFragment(ChooseMP3Fragment.newInstance(bundle), 1);
                    break;
                case 2:
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case PICK_ART_REQUEST:
                    Log.w("DATA", data.getData().toString());
                    bundle.putString("artURI", data.getData().toString());
                    StartFragment(ChooseMP3Fragment.newInstance(bundle), 1);
                    break;
            }
        }
    }

    private void getImageUri() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, PICK_ART_REQUEST);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (bundle != null) {
            bundle.putInt("cFrag", f_id);
            outState.putString("artURI", bundle.getString("artURI"));
            outState.putBundle("trackBundle", bundle);
        }

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        bundle = new Bundle(savedInstanceState.getBundle("trackBundle"));
        f_id = bundle.getInt("cFrag");
        switch (f_id) {
            case 1:
                bundle.putString("artURI", savedInstanceState.getString("artURI"));
                StartFragment(ChooseMP3Fragment.newInstance(bundle), 1);
                break;
            case 2:
                break;
        }
    }

    private void StartFragment(Fragment f, int fragment_id) {
        fragmentTransaction = fragmentManager.beginTransaction();
        if (fragment_id != 0) {
            fragmentTransaction.replace(mContent.getId(), f).addToBackStack(null).commit();
        } else {
            fragmentTransaction.replace(mContent.getId(), f).commit();
        }
    }

    @Override
    public void onFragmentInteraction(int FragmentId) {
        f_id  = FragmentId;
    }

    @Override
    public void onImageClickInteraction() {
        getImageUri();
    }

    @Override
    public void onImageLongClickInteraction() {
        getImageUri();
    }

    @Override
    public void hasImage(Boolean hasImage) {
        mHasImage = hasImage;
    }

    @Override
    public void trackFilePath(String path, String title, String artist, String duration) {
        bundle.putString("trackPath", path);
        bundle.putString("trackTitle", title);
        bundle.putString("trackArtist", artist);
        bundle.putString("trackDuration", duration);

        StartFragment(TrackDetailsFragment.newInstance(bundle), 1);

    }

    @Override
    public void onTrackFragmentInteraction(int FragmentId) {
        f_id  = FragmentId;
    }

    @Override
    public void trackBundle(Bundle b) {
        Intent i = new Intent(CreateTuneActivity.this, UploadTuneService.class);
        Log.w("BUNDLE", bundle.toString());
        i.putExtra("trackBundle", bundle);
        startService(i);
        startActivity(new Intent(CreateTuneActivity.this, Home.class));
        finish();
    }

    @Override
    public void showSnackBar(String message) {
        Snackbar.make(mContent, message, Snackbar.LENGTH_LONG).show();
    }
}
