package ng.codehaven.bambam.ui.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.ui.views.CustomTextView;
import ng.codehaven.bambam.utils.FileHelper;
import ng.codehaven.bambam.utils.UIUtils;

public class TrackDetailsFragment extends Fragment implements View.OnClickListener {

    @InjectView(R.id.art)
    ImageView mArt;
    @InjectView(R.id.trackTitle)
    CustomTextView mTitle;
    @InjectView(R.id.trackArtist) CustomTextView mArtist;
    @InjectView(R.id.trackDuration) CustomTextView mDuration;
    @InjectView(R.id.chooseMp3)Button mUploadBtn;
    @InjectView(R.id.title)
    EditText tEV;
    @InjectView(R.id.desc)
    EditText dEV;

    private OnDetailsFragmentInterface mListener;
    private Bundle mBundle;

    public TrackDetailsFragment() {
        // Required empty public constructor
    }

    public static TrackDetailsFragment newInstance(Bundle b) {
        TrackDetailsFragment fragment = new TrackDetailsFragment();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBundle = getArguments();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_track_details, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        mListener.onTrackFragmentInteraction(2);

        mTitle.setText(mBundle.getString("trackTitle"));
        mArtist.setText(mBundle.getString("trackArtist"));
        mDuration.setText(UIUtils.getTime(mBundle.getString("trackDuration")));

        Uri uri = Uri.parse(mBundle.getString("artURI"));
        byte[] mArtFileBytes = FileHelper.getByteArrayFromFile(getActivity(), uri);
        mArt.setImageBitmap(
                FileHelper.resizeImageMaintainAspectRatio(
                        mArtFileBytes, FileHelper.SHORT_SIDE_TARGET
                )
        );

        mUploadBtn.setOnClickListener(this);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDetailsFragmentInterface) activity;
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
    public void onClick(View view) {
        int id = view.getId();

        String title = tEV.getText().toString().trim();
        String desc = dEV.getText().toString().trim();

        if (id == R.id.chooseMp3){
            if (!title.isEmpty()){
                mBundle.putString("fTrackTitle", title);
                mBundle.putString("fTrackDesc", desc);
                mListener.trackBundle(mBundle);
            } else {
                mListener.showSnackBar("Title cannot be blank.");
            }

        }
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
    public interface OnDetailsFragmentInterface {
        void onTrackFragmentInteraction(int FragmentId);
        void trackBundle(Bundle b);
        void showSnackBar(String message);
    }

}
