package ng.codehaven.bambam.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.login.widget.ProfilePictureView;
import com.parse.ParseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.models.Track;
import ng.codehaven.bambam.ui.views.BezelImageView;
import ng.codehaven.bambam.ui.views.CircleTransform;
import ng.codehaven.bambam.utils.UIUtils;


public class UserProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_PROFILE_HEADER = 0;
    public static final int TYPE_PROFILE_OPTIONS = 1;
    public static final int TYPE_EMPTY = 2;
    public static final int TYPE_TRACK = 3;

    private static final int MIN_ITEMS_COUNT = 3;

    private final Context context;
    private final int cellSize;

    private final List<Track> tracks;

    private boolean isMyProfile = true;

    private String mUserId;
    private ParseUser mUser;

    private SharedPreferences sharedPref;
    private String defaultName;
    private String username;
    private String aboutMe;
    private boolean hasUsername;
    private boolean hasTrack;

    private UserProfileHandler uh;

    public UserProfileAdapter(Context c, List<Track> t, boolean isLocal, ParseUser u) {
        this.context = c;
        this.cellSize = UIUtils.getScreenWidth(context) / 3;
        isMyProfile = isLocal;
        this.mUser = u;
        if (isMyProfile) {
            this.mUserId = ParseUser.getCurrentUser().getObjectId();
        } else {
            this.mUserId = u.getObjectId();
        }
        tracks = t;

        sharedPref = c.getSharedPreferences("user", Context.MODE_PRIVATE);

        defaultName = "defaultName";
        aboutMe = " ";

        username = sharedPref.getString("username", defaultName);
        aboutMe = sharedPref.getString("about", aboutMe);
        hasUsername = sharedPref.getBoolean("hasUsername", false);
    }

    public void setUserProfileHandler(UserProfileHandler handler) {
        this.uh = handler;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_PROFILE_HEADER;
        } else if (position == 1) {
            return TYPE_PROFILE_OPTIONS;
        } else if (position == 2) {
            return TYPE_EMPTY;
        } else {
            return TYPE_TRACK;
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (TYPE_PROFILE_HEADER == viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.view_user_profile_header, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.setFullSpan(true);
            view.setLayoutParams(layoutParams);
            return new ProfileHeaderViewHolder(view);
        } else if (TYPE_PROFILE_OPTIONS == viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.view_user_profile_options, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.setFullSpan(true);
            view.setLayoutParams(layoutParams);
            return new ProfileOptionsViewHolder(view);
        }
        if (TYPE_EMPTY == viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.view_no_tunes, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.setFullSpan(true);
            view.setLayoutParams(layoutParams);
            return new NoTunesViewHolder(view);
        }
        if (TYPE_TRACK == viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.height = cellSize;
            layoutParams.width = cellSize;
            layoutParams.setFullSpan(false);
            view.setLayoutParams(layoutParams);
            return new TrackViewHolder(view);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (TYPE_PROFILE_HEADER == viewType) {
            bindProfileHeader((ProfileHeaderViewHolder) holder);
        } else if (TYPE_PROFILE_OPTIONS == viewType) {
            bindProfileOptions((ProfileOptionsViewHolder) holder);
        } else if (TYPE_EMPTY == viewType) {
            bindEmpty((NoTunesViewHolder) holder);
        } else if (TYPE_TRACK == viewType) {
            bindTrack((TrackViewHolder) holder, position);
        }
    }

    private void bindEmpty(NoTunesViewHolder holder) {
        if (!String.valueOf(tracks.size()).equals("0")) {
            holder.mRoot.setVisibility(View.GONE);
            holder.mArt.setVisibility(View.GONE);
            holder.mNoTuneLabel.setVisibility(View.GONE);
        } else {
            if (isMyProfile) {
                holder.mNoTuneLabel.setText(context.getText(R.string.my_empty_tunes));
            } else {
                holder.mNoTuneLabel.setText(context.getText(R.string.empty_tunes));
            }
        }
    }

    private void bindProfileHeader(final ProfileHeaderViewHolder holder) {
        BezelImageView pv = holder.ivUserProfilePhoto;
        String id;
        if (isMyProfile) {
            id = AccessToken.getCurrentAccessToken().getUserId();
//            pv.setProfileId(AccessToken.getCurrentAccessToken().getUserId());
        } else {
            id = mUser.getString("fb_id");
//            pv.setProfileId(mUser.getString("fb_id"));
        }
        String url = "https://graph.facebook.com/" + id + "/picture?type=large";
        Log.e("TAG", url);

        Picasso.with(context)
                .load(url)
                .transform(new CircleTransform())
                .placeholder(R.drawable.com_facebook_profile_picture_blank_square)
                .into(pv, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });

    }

    private void bindProfileOptions(ProfileOptionsViewHolder holder) {
        setUsername(holder.mUsername);
        setAbout(holder.mAboutMe);
        holder.mTunes.setText(String.valueOf(tracks.size()));
    }

    private void bindTrack(final TrackViewHolder holder, int position) {
        Picasso.with(context)
                .load(tracks.get(position - MIN_ITEMS_COUNT).getArtUrl())
                .resize(cellSize, cellSize)
                .centerCrop()
                .placeholder(R.drawable.com_facebook_profile_picture_blank_portrait)
                .into(holder.ivPhoto);
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return MIN_ITEMS_COUNT + tracks.size();
    }

    private void setUsername(TextView mUsername) {
        if (isMyProfile && hasUsername) {
            mUsername.setText(username);
        } else {
            mUsername.setText(mUser.getUsername());
        }
    }

    private void setAbout(TextView mAboutMe) {
        if (isMyProfile) {
            mAboutMe.setText(aboutMe);
        } else {
            mAboutMe.setText(mUser.getString("f_username"));
        }
    }

    public void remove(int position) {
        hasTrack = position == TYPE_EMPTY;

        notifyItemRemoved(position);
    }

    public interface UserProfileHandler {
        void onItemsPresent(int position);
    }

    static class ProfileHeaderViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.myProfilePic)
        BezelImageView ivUserProfilePhoto;

        public ProfileHeaderViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

    }

    static class ProfileOptionsViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.username)
        TextView mUsername;

        @InjectView(R.id.about_me)
        TextView mAboutMe;

        @InjectView(R.id.tunes)
        TextView mTunes;
        @InjectView(R.id.followers)
        TextView mFolloers;
        @InjectView(R.id.followed)
        TextView mFollowing;

        public ProfileOptionsViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.flRoot)
        FrameLayout flRoot;

        @InjectView(R.id.ivPhoto)
        ImageView ivPhoto;

        public TrackViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

    }

    static class NoTunesViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.no_tune_label)
        TextView mNoTuneLabel;
        @InjectView(R.id.root)
        RelativeLayout mRoot;
        @InjectView(R.id.errorImg)
        ImageView mArt;

        public NoTunesViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

    }
}
