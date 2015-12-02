package ng.codehaven.bambam.ui.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;

import com.facebook.AccessToken;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bolts.Continuation;
import bolts.Task;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ng.codehaven.bambam.interfaces.OnLoadMoreListener;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.models.Comment;
import ng.codehaven.bambam.models.Track;
import ng.codehaven.bambam.ui.views.BezelImageView;
import ng.codehaven.bambam.ui.views.CircleTransform;
import ng.codehaven.bambam.ui.views.CustomTextView;
import ng.codehaven.bambam.utils.CalloutLink;
import ng.codehaven.bambam.utils.Hashtag;
import ng.codehaven.bambam.utils.TimeUtils;
import ng.codehaven.bambam.utils.UIUtils;

/**
 * Created by Thompson on 09/08/2015.
 * Default feed adapter
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    private static final int ANIMATED_ITEMS_COUNT = 2;
    private final Map<Integer, Integer> likesCount = new HashMap<>();
    private final Map<RecyclerView.ViewHolder, AnimatorSet> likeAnimations = new HashMap<>();
    private final ArrayList<Integer> likedPositions = new ArrayList<>();
    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private Context context;
    private int lastAnimatedPosition = -1;
    private int itemsCount = 0;
    private boolean animateItems = false;
    private OnFeedItemClickListener onFeedItemClickListener;
    private List<Track> mTracks;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public FeedAdapter(Context context) {
        this.context = context;
    }

    public FeedAdapter(Context context, List<Track> tracks) {
        this.context = context;
        this.mTracks = tracks;
    }

    @Override
    public int getItemViewType(int position) {
        return mTracks.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
        final CellFeedViewHolder cellFeedViewHolder = new CellFeedViewHolder(view);
        cellFeedViewHolder.btnComments.setOnClickListener(this);
        cellFeedViewHolder.btnMore.setOnClickListener(this);
        cellFeedViewHolder.ivFeedCenter.setOnClickListener(this);
        cellFeedViewHolder.btnLike.setOnClickListener(this);
        cellFeedViewHolder.ivUserProfile.setOnClickListener(this);
        return cellFeedViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof CellFeedViewHolder) {
            runEnterAnimation(viewHolder.itemView, position);
            CellFeedViewHolder holder = (CellFeedViewHolder) viewHolder;
            holder.mDesc.setMovementMethod(LinkMovementMethod.getInstance());
            if (mTracks == null) {
                if (position % 2 == 0) {
                    holder.ivFeedCenter.setImageResource(R.drawable.ic_art1);
                    //            holder.mDesc.setText(R.string.desc);

                    SpannableString commentsContent = getSpannableString(context.getString(R.string.desc));

                    holder.mDesc.setText(commentsContent);

                } else {
                    holder.ivFeedCenter.setImageResource(R.drawable.img_feed_center_2);
                    holder.mDesc.setText(R.string.desc2);
                }
                updateLikesCounter(holder, false);
                updateHeartButton(holder, false);
            } else {
                Picasso.with(context).load(mTracks.get(position).getArtUrl()).into(holder.ivFeedCenter);

                String url = "https://graph.facebook.com/" + AccessToken.getCurrentAccessToken().getUserId() + "/picture?type=large";

                Picasso.with(context)
                        .load(url)
                        .transform(new CircleTransform())
                        .placeholder(R.drawable.com_facebook_profile_picture_blank_square)
                        .into(holder.ivUserProfile);

                if (!mTracks.get(position).getDesc().isEmpty()) {
                    SpannableString descContent = getSpannableString(mTracks.get(position).getDesc());
                    holder.mDesc.setText(descContent);
                } else {
                    holder.mDesc.setVisibility(View.GONE);
                }

                holder.mTitle.setText(mTracks.get(position).getTitle());
                holder.mUsernameTv.setText(mTracks.get(position).getArtistName());
                holder.mTS.setText(TimeUtils.timeAgo(mTracks.get(position).getCreatedAt().toString()));

                holder.tsLikesCounter.setText(String.valueOf(mTracks.get(position).getLikesCount()));



                try {
                    setValues(mTracks.get(position).getmComments().get(mTracks.get(position).getmComments().size() -3), holder.mC1);
                } catch (ArrayIndexOutOfBoundsException e) {
                    setValues(null, holder.mC1);
                    e.printStackTrace();
                }
                try{
                    setValues(mTracks.get(position).getmComments().get(mTracks.get(position).getmComments().size() - 2), holder.mC2);
                } catch (ArrayIndexOutOfBoundsException e) {
                    setValues(null, holder.mC2);
                    e.printStackTrace();
                }
                try{
                    setValues(mTracks.get(position).getmComments().get(mTracks.get(position).getmComments().size() - 1), holder.mC3);
                } catch (ArrayIndexOutOfBoundsException e) {
                    setValues(null, holder.mC3);
                    e.printStackTrace();
                }

                if (isLiked(position)){
                    holder.btnLike.setImageResource(R.drawable.ic_heart_red);
                } else {
                    holder.btnLike.setImageResource(R.drawable.ic_heart_outline_grey);
                }



//                insertComments(holder);

            }

            holder.btnComments.setTag(position);
            holder.btnMore.setTag(position);
            holder.ivFeedCenter.setTag(holder);
            holder.btnLike.setTag(holder);


            if (likeAnimations.containsKey(holder)) {
                likeAnimations.get(holder).cancel();
            }
            resetLikeAnimationState(holder);
        }
    }

    boolean isLiked(int p){
        return mTracks.get(p).getmLike() != null && mTracks.get(p).getmLike().isLiked();
    }

    void setValues(Comment c, CustomTextView t){
        if (c != null){
            if (!c.getmUsername().isEmpty() || !c.getmComment().isEmpty()){
                t.setText(getSpanText(c.getmUsername(), c.getmComment()));
                t.setVisibility(View.VISIBLE);
            }
        } else {
            t.setVisibility(View.GONE);
        }
    }

    private SpannableString getSpanText(String username, String comment) {
        SpannableString s = new SpannableString(username + " " + comment);
        s.setSpan(new CalloutLink(context), 0, username.length(), 0);
        return s;
    }


    public void setLoaded() {
        loading = false;
    }

    @NonNull
    private SpannableString getSpannableString(String desc) {
        ArrayList<int[]> hashtagSpans = getSpans(desc, '#');
        ArrayList<int[]> calloutSpans = getSpans(desc, '@');

        SpannableString commentsContent = new SpannableString(desc);

        for (int i = 0; i < hashtagSpans.size(); i++) {
            int[] span = hashtagSpans.get(i);
            int hastagStart = span[0];
            int hastagEnd = span[1];
            commentsContent.setSpan(new Hashtag(context), hastagStart, hastagEnd, 0);
        }

        for (int i = 0; i < calloutSpans.size(); i++) {
            int[] span = calloutSpans.get(i);
            int calloutStart = span[0];
            int calloutEnd = span[1];
            commentsContent.setSpan(new CalloutLink(context), calloutStart, calloutEnd, 0);
        }
        return commentsContent;
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void runEnterAnimation(View view, int position) {
        if (!animateItems || position >= ANIMATED_ITEMS_COUNT - 1) {
            return;
        }

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(UIUtils.getScreenHeight(context));
            view.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration(700)
                    .start();
        }
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        if (mTracks == null) {
            return itemsCount;
        } else {
            return mTracks.size();
        }
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.btnComments) {
            if (onFeedItemClickListener != null) {
                onFeedItemClickListener.onCommentsClick(view, (Integer) view.getTag());
            }
        } else if (viewId == R.id.btnMore) {
            if (onFeedItemClickListener != null) {
                onFeedItemClickListener.onMoreClick(view, (Integer) view.getTag());
            }
        } else if (viewId == R.id.btnLike) {
            CellFeedViewHolder holder = (CellFeedViewHolder) view.getTag();
            if (!likedPositions.contains(holder.getAdapterPosition())) {
                likedPositions.add(holder.getAdapterPosition());
                updateLikesCounter(holder, true);
                updateHeartButton(holder, true);
                doLike(holder.getAdapterPosition());
            } else {

            }
        } else if (viewId == R.id.ivFeedCenter) {
            CellFeedViewHolder holder = (CellFeedViewHolder) view.getTag();
            StartMusicService(holder);
            if (!likedPositions.contains(holder.getAdapterPosition())) {
                likedPositions.add(holder.getAdapterPosition());
                updateLikesCounter(holder, true);
                animatePhotoLike(holder);
                updateHeartButton(holder, false);
            }
        } else if (viewId == R.id.ivUserProfile) {
            if (onFeedItemClickListener != null) {
                onFeedItemClickListener.onProfileClick(view);
            }
        }
    }

    private void StartMusicService(CellFeedViewHolder holder) {
        onFeedItemClickListener.onCenterIVClick(holder.getAdapterPosition(), mTracks.get(holder.getAdapterPosition()));
    }

    public Task<ParseObject> getAsync(final ParseObject p) {
        final Task<ParseObject>.TaskCompletionSource tcs = Task.create();
        p.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    tcs.setResult(p);
                } else {
                    tcs.setError(e);
                }
            }
        });

        return tcs.getTask();
    }


    private void doLike(final int i) {
        ParseQuery<ParseObject> t = new ParseQuery<>("Tunes");
        t.whereEqualTo("objectId", mTracks.get(i).getParseId());
        t.getFirstInBackground().onSuccessTask(new Continuation<ParseObject, Task<ParseObject>>() {
            @Override
            public Task<ParseObject> then(Task<ParseObject> task) throws Exception {
                ParseObject track = task.getResult();
                track.increment("likeCount");
                return getAsync(track);
            }
        }).continueWith(new Continuation<ParseObject, Object>() {
            @Override
            public Object then(Task<ParseObject> task) throws Exception {
                mTracks.get(i).setLikesCount(task.getResult().getInt("likeCount"));
                mTracks.get(i).setIsLiked(true);
                return null;
            }
        });
    }

    private void updateLikesCounter(CellFeedViewHolder holder, boolean animated) {
        int currentLikesCount = likesCount.get(holder.getAdapterPosition()) + 1;
        String likesCountText = context.getResources().getQuantityString(
                R.plurals.likes_count, currentLikesCount, currentLikesCount
        );

        if (animated) {
            holder.tsLikesCounter.setText(likesCountText);
        } else {
            holder.tsLikesCounter.setCurrentText(likesCountText);
        }

        likesCount.put(holder.getAdapterPosition(), currentLikesCount);
    }

    private void updateHeartButton(final CellFeedViewHolder holder, boolean animated) {
        if (animated) {
            if (!likeAnimations.containsKey(holder)) {
                AnimatorSet animatorSet = new AnimatorSet();
                likeAnimations.put(holder, animatorSet);

                ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(holder.btnLike, "rotation", 0f, 360f);
                rotationAnim.setDuration(300);
                rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

                ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(holder.btnLike, "scaleX", 0.2f, 1f);
                bounceAnimX.setDuration(300);
                bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

                ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(holder.btnLike, "scaleY", 0.2f, 1f);
                bounceAnimY.setDuration(300);
                bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
                bounceAnimY.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        holder.btnLike.setImageResource(R.drawable.ic_heart_red);
                    }
                });

                animatorSet.play(rotationAnim);
                animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        resetLikeAnimationState(holder);
                    }
                });

                animatorSet.start();
            }
        } else {
            if (likedPositions.contains(holder.getAdapterPosition()) || mTracks.get(holder.getAdapterPosition()).isLiked()) {
                holder.btnLike.setImageResource(R.drawable.ic_heart_red);
            } else {
                holder.btnLike.setImageResource(R.drawable.ic_heart_outline_grey);
            }
        }
    }

    private void animatePhotoLike(final CellFeedViewHolder holder) {
        if (!likeAnimations.containsKey(holder)) {
            holder.vBgLike.setVisibility(View.VISIBLE);
            holder.ivLike.setVisibility(View.VISIBLE);

            holder.vBgLike.setScaleY(0.1f);
            holder.vBgLike.setScaleX(0.1f);
            holder.vBgLike.setAlpha(1f);
            holder.ivLike.setScaleY(0.1f);
            holder.ivLike.setScaleX(0.1f);

            AnimatorSet animatorSet = new AnimatorSet();
            likeAnimations.put(holder, animatorSet);

            ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleY", 0.1f, 1f);
            bgScaleYAnim.setDuration(200);
            bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleX", 0.1f, 1f);
            bgScaleXAnim.setDuration(200);
            bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(holder.vBgLike, "alpha", 1f, 0f);
            bgAlphaAnim.setDuration(200);
            bgAlphaAnim.setStartDelay(150);
            bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

            ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 0.1f, 1f);
            imgScaleUpYAnim.setDuration(300);
            imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 0.1f, 1f);
            imgScaleUpXAnim.setDuration(300);
            imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

            ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 1f, 0f);
            imgScaleDownYAnim.setDuration(300);
            imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
            ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 1f, 0f);
            imgScaleDownXAnim.setDuration(300);
            imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

            animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
            animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    resetLikeAnimationState(holder);
                }
            });
            animatorSet.start();
        }
    }

    private void resetLikeAnimationState(CellFeedViewHolder holder) {
        likeAnimations.remove(holder);
        holder.vBgLike.setVisibility(View.GONE);
        holder.ivLike.setVisibility(View.GONE);
    }

    public void updateItems(boolean animated) {
        animateItems = animated;
        if (mTracks == null) {
            itemsCount = 10;
            fillLikesWithRandomValues();
        } else {
            itemsCount = mTracks.size();
            for (int i = 0; i < getItemCount(); i++) {
                likesCount.put(i, mTracks.get(i).getLikesCount());
            }
        }
        notifyDataSetChanged();
    }

    private void fillLikesWithRandomValues() {
        for (int i = 0; i < getItemCount(); i++) {
            likesCount.put(i, new Random().nextInt(100));
        }
    }

    public void setOnFeedItemClickListener(OnFeedItemClickListener onFeedItemClickListener) {
        this.onFeedItemClickListener = onFeedItemClickListener;
    }

    public ArrayList<int[]> getSpans(String body, char prefix) {
        ArrayList<int[]> spans = new ArrayList<>();
        Pattern pattern = Pattern.compile(prefix + "\\w+");
        Matcher matcher = pattern.matcher(body);

        //Check all occurrences
        while (matcher.find()) {
            int[] currentSpan = new int[2];
            currentSpan[0] = matcher.start();
            currentSpan[1] = matcher.end();

            spans.add(currentSpan);
        }

        return spans;
    }

    public void add(Track t) {
        if (t != null) {
            mTracks.add(0, t);
            notifyItemInserted(0);
        }
    }

    public void addAll(List<Track> m) {
        mTracks = m;
        notifyItemRangeInserted(0, mTracks.size());
    }


    public interface OnFeedItemClickListener {
        void onCommentsClick(View v, int position);

        void onMoreClick(View v, int position);

        void onProfileClick(View v);

        void onCenterIVClick(int i, Track t);
    }

    public static class CellFeedViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.ivFeedCenter)
        ImageView ivFeedCenter;
        //        @InjectView(R.id.ivFeedBottom)
//        ImageView ivFeedBottom;
        @InjectView(R.id.btnComments)
        ImageButton btnComments;
        @InjectView(R.id.btnLike)
        ImageButton btnLike;
        @InjectView(R.id.btnMore)
        ImageButton btnMore;
        @InjectView(R.id.vBgLike)
        View vBgLike;
        @InjectView(R.id.ivLike)
        ImageView ivLike;
        @InjectView(R.id.tsLikesCounter)
        TextSwitcher tsLikesCounter;
        @InjectView(R.id.ivUserProfile)
        BezelImageView ivUserProfile;
        @InjectView(R.id.desc)
        CustomTextView mDesc;
        @InjectView(R.id.tvUsername)
        CustomTextView mUsernameTv;
        @InjectView(R.id.tvTimeStamp)
        CustomTextView mTS;
        @InjectView(R.id.title)
        CustomTextView mTitle;
        @InjectView(R.id.c1)
        CustomTextView mC1;
        @InjectView(R.id.c2)
        CustomTextView mC2;
        @InjectView(R.id.c3)
        CustomTextView mC3;

        public CellFeedViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
