package ng.codehaven.bambam.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ng.codehaven.bambam.R;


public class TuneTileView extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener {

    private int minValue = Integer.MIN_VALUE;
    private int maxValue = Integer.MAX_VALUE;

    View mRootView;
    @InjectView(R.id.btnLike)
    View mLikeButton;
    @InjectView(R.id.btnComments)
    View mCommentButton;
    @InjectView(R.id.ivFeedCenter)
    View mArt;
    @InjectView(R.id.vBgLike)
    View bgLike;
    @InjectView(R.id.ivLike)
    View ivLike;
    @InjectView(R.id.ivUserProfile)
    View mAvatar;

    @InjectView(R.id.tvUsername)
    CustomTextView mTitle;
    @InjectView(R.id.desc)
    CustomTextView mDesc;
    @InjectView(R.id.likeCount)
    CustomTextView mLikeCount;
    @InjectView(R.id.commentsCount)
    CustomTextView mCommentCount;

    @InjectView(R.id.tvTimeStamp)
    CustomTextView mTimeStamp;

    int commentCount, likeCount;
    String username, timestamp, art, title, desc;
    boolean isLiked;

    public TuneTileView(Context context) {
        super(context);
        init(context);
    }

    public TuneTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        int value = commentCount;
        if (value < minValue){
            value = minValue;
        } else if (value > maxValue){
            value = maxValue;
        }
        this.commentCount = value;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getArt() {
        return art;
    }

    public void setArt(String art) {
        this.art = art;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    private void init(Context context) {
        inflate(context, R.layout.tune_view_layout, this);
        ButterKnife.inject(this);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }
}
