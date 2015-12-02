package ng.codehaven.bambam.ui.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.utils.FileHelper;


public class ChooseArtworkFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private OnFragmentInteractionListener mListener;
    public static int FRAGMENT_ID = 0;

    private boolean hasImage;
    private Uri mURI;

    @InjectView(R.id.art)
    ImageView mArt;

    public static Fragment newInstance(Bundle b){
        Fragment f = new Fragment();
        f.setArguments(b);
        return f;
    }

    public ChooseArtworkFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            Bundle mBundle = getArguments();
            Log.w("URI",mBundle.getString("artURI"));
            mURI = Uri.parse(mBundle.getString("artURI"));
            hasImage = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_choose_artwork, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();


        mArt.setOnClickListener(this);
        mArt.setLongClickable(true);
        mArt.setOnLongClickListener(this);

        if (mListener != null) {
            mListener.hasImage(hasImage);
            mListener.onFragmentInteraction(FRAGMENT_ID);
        }

        if (hasImage){
            byte[] mArtFileBytes = FileHelper.getByteArrayFromFile(getActivity(), mURI);
            mArt.setImageBitmap(
                    FileHelper.resizeImageMaintainAspectRatio(
                            mArtFileBytes, FileHelper.SHORT_SIDE_TARGET
                    )
            );
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
    public void onClick(View view) {
        int id = view.getId();
        if (mListener != null && id == R.id.art && !hasImage)
            mListener.onImageClickInteraction();
    }

    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        if (mListener != null && id == R.id.art && hasImage){
            mListener.onImageLongClickInteraction();
            return true;
        }
        return false;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int FragmentId);
        void onImageClickInteraction();
        void onImageLongClickInteraction();
        void hasImage(Boolean hasImage);
    }

}
