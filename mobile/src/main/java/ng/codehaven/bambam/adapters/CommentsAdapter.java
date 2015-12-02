package ng.codehaven.bambam.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.models.Comment;
import ng.codehaven.bambam.ui.views.BezelImageView;
import ng.codehaven.bambam.ui.views.CircleTransform;
import ng.codehaven.bambam.utils.CalloutLink;
import ng.codehaven.bambam.utils.TimeUtils;


public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Comment> mComments;
    private Context mContext;

    public CommentsAdapter(List<Comment> mComments, Context mContext) {
        this.mComments = mComments;
        this.mContext = mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.comment_view, parent, false);
        return new CommentItemHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CommentItemHolder h = (CommentItemHolder) holder;
        String comment = String.format(mContext.getString(R.string.comments_text),mComments.get(position).getmUsername(), mComments.get(position).getmComment());
        SpannableString s = new SpannableString(comment);
        s.setSpan(new CalloutLink(mContext), 0, mComments.get(position).getmUsername().length(), 0);
        h.mCommentView.setText(s);
        Picasso.with(mContext).load(mComments.get(position).getmAvatar()).transform(new CircleTransform()).into(h.mAvatarView);
        h.mTimeStamp.setText(TimeUtils.timeAgo(mComments.get(position).getmCreatedAt().toString()));
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public static class CommentItemHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.bvAvatar)
        BezelImageView mAvatarView;
        @InjectView(R.id.tvComment)
        TextView mCommentView;
        @InjectView(R.id.time)TextView mTimeStamp;

        public CommentItemHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
