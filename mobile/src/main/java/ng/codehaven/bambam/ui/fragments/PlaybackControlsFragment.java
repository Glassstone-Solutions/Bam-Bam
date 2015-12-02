package ng.codehaven.bambam.ui.fragments;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.models.Track;
import ng.codehaven.bambam.services.MusicService;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaybackControlsFragment extends Fragment implements ServiceConnection, MusicService.Callbacks {

    @InjectView(R.id.play_pause)
    ImageButton mPlayPause;
    @InjectView(R.id.album_art)
    ImageView mArtThumb;
    @InjectView(R.id.track_title)
    TextView mTrackTitle;
    @InjectView(R.id.artist)
    TextView mTrackArtist;
    private Track mTrack;

    private MusicService mService;
    private View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play_pause:
                    break;
            }
        }
    };


    public PlaybackControlsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_playback_controls, container, false);
        ButterKnife.inject(this, v);

        mPlayPause.setEnabled(true);
        mPlayPause.setOnClickListener(mButtonListener);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent i = new Intent(getActivity(), MusicService.class);
        getActivity().bindService(i, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mService != null) {
            mService.setCallbacks(null);
            getActivity().unbindService(this);
        }
    }

    @Override
    public void isPrepared(Boolean p) {

    }

    @Override
    public void getTrack(Track t) {

    }

    @Override
    public void getProgress(int progress) {

    }

    @Override
    public void playState(Track t, int state, int progress, int max) {
        switch (state) {
            case MusicService.TRACK_LOADING:
                mTrack = t;
                Picasso.with(getActivity()).load(mTrack.getArtUrl()).into(mArtThumb);
                mTrackTitle.setText(mTrack.getTitle());
                mTrackArtist.setText(mTrack.getArtistName());
                break;
            case MusicService.TRACK_PREPARED:
            case MusicService.TRACK_PLAYING:
                break;
            case MusicService.TRACK_DONE:
                break;
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mService = ((MusicService.LocalBinder) iBinder).getService();
        mService.setCallbacks(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mService = null;
    }
}
