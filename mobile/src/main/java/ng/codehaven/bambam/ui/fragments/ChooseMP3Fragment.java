package ng.codehaven.bambam.ui.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.ui.adapters.TrackChooserAdapter;


public class ChooseMP3Fragment extends Fragment implements TrackChooserAdapter.OnTrackItemClickListener {

    @InjectView(R.id.tracks_recyclerView)
    RecyclerView mRecycler;
    private Bundle mBundle;
    private TrackChooserAdapter mAdapter;
    private Cursor c;
    private OnFragmentInteractionListener mListener;

    public ChooseMP3Fragment() {
        // Required empty public constructor
    }

    public static ChooseMP3Fragment newInstance(Bundle b) {
        ChooseMP3Fragment fragment = new ChooseMP3Fragment();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBundle = getArguments();
            Log.w("URI", mBundle.getString("artURI"));
        }

        String cols[] = {MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA};
        c = getActivity().managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols, null, null, MediaStore.Audio.Media.DISPLAY_NAME);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        c.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_choose_mp3, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        mListener.onFragmentInteraction(1);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(linearLayoutManager);
        mAdapter = new TrackChooserAdapter(getActivity(), c);
        mAdapter.setOnTrackItemClickListener(this);
        mRecycler.setAdapter(mAdapter);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
//            mListener.onTrackFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCommentsClick(View v, int position) {

    }

    @Override
    public void onTrackClick(View v, int position) {
        c.moveToPosition(position);
        Log.w("TRACK", c.getString(3));
        mListener.trackFilePath(c.getString(3), c.getString(0), c.getString(1), c.getString(2));
    }

    @Override
    public void onProfileClick(View v) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int FragmentId);
        void trackFilePath(String path, String title, String artist, String duration);
    }

}
