package ng.codehaven.bambam.models;

import java.util.Date;
import java.util.List;

import io.realm.RealmObject;

public class Comment extends RealmObject {
    private String mComment, mUsername, mAvatar;
    private Date mCreatedAt;

    public Comment() {
    }

    public Comment(String mComment, String mUsername, String mAvatar, Date mCreatedAt) {
        this.mComment = mComment;
        this.mUsername = mUsername;
        this.mAvatar = mAvatar;
        this.mCreatedAt = mCreatedAt;
    }


    public Date getmCreatedAt() {
        return mCreatedAt;
    }

    public void setmCreatedAt(Date mCreatedAt) {
        this.mCreatedAt = mCreatedAt;
    }

    public String getmComment() {
        return mComment;
    }

    public void setmComment(String mComment) {
        this.mComment = mComment;
    }

    public String getmUsername() {
        return mUsername;
    }

    public void setmUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    public String getmAvatar() {
        return mAvatar;
    }

    public void setmAvatar(String mAvatar) {
        this.mAvatar = mAvatar;
    }
}
