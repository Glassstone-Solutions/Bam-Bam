package ng.codehaven.bambam.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.models.Track;
import ng.codehaven.bambam.ui.views.CustomTextView;
import ng.codehaven.bambam.ui.views.SquaredFrameLayout;
import ng.codehaven.bambam.utils.UIUtils;

/**
 * Created by Thompson on 16/08/2015.
 */
public class TrackChooserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private Context mContext;
    private List<Track> mTracks;
    private Cursor mCursor;
    private int count;
    private boolean fromCursor;

    private OnTrackItemClickListener onTrackItemClickListener;

    public TrackChooserAdapter(Context c, Cursor cursor) {
        this.mContext = c;
        this.mCursor = cursor;
        this.fromCursor = true;
    }

    public TrackChooserAdapter(Context c, List<Track> tracks) {
        this.mContext = c;
        this.mTracks = tracks;
        this.fromCursor = false;
    }

    public void setOnTrackItemClickListener(OnTrackItemClickListener onTrackItemClickListener) {
        this.onTrackItemClickListener = onTrackItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.item_track, parent, false);
        final CellTrackViewHolder cellTrackViewHolder = new CellTrackViewHolder(view);
        cellTrackViewHolder.mRoot.setOnClickListener(this);
        return cellTrackViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CellTrackViewHolder vh = (CellTrackViewHolder) holder;
        if (fromCursor){
            bindFromCursor(vh, position);
        } else {
            bindFromTracks(vh, position);
        }
    }

    public void bindFromTracks(CellTrackViewHolder vh, int position) {

    }

    public void bindFromCursor(CellTrackViewHolder vh, int position) {
        vh.mArtWrap.setVisibility(View.GONE);
        mCursor.moveToPosition(position);
        vh.mTitle.setText(mCursor.getString(0));
        vh.mArtist.setText(mCursor.getString(1));
        vh.mDuration.setText(UIUtils.getTime(mCursor.getString(2)));
        vh.mRoot.setTag(position);
    }




    @Override
    public int getItemCount() {
        if (mTracks == null || mTracks.size() == 0) {
            return mCursor.getCount();
        } else {
            return mTracks.size();
        }
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        onTrackItemClickListener.onTrackClick(v, position);
    }

    public interface OnTrackItemClickListener {
        void onCommentsClick(View v, int position);

        void onTrackClick(View v, int position);

        void onProfileClick(View v);
    }

    public static class CellTrackViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.root)
        LinearLayout mRoot;
        @InjectView(R.id.trackArt)
        ImageView mArt;
        @InjectView(R.id.trackTitle)
        CustomTextView mTitle;
        @InjectView(R.id.trackArtist)
        CustomTextView mArtist;
        @InjectView(R.id.trackDuration)
        CustomTextView mDuration;
        @InjectView(R.id.artWrap)SquaredFrameLayout mArtWrap;

        public CellTrackViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
