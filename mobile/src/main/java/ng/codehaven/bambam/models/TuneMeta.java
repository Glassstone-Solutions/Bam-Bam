package ng.codehaven.bambam.models;

import java.util.HashMap;
import java.util.List;


public class TuneMeta {
    private int likeCount;
    private boolean isLiked;
    private List<HashMap<String, String[]>> comments;

    public TuneMeta(int likeCount, boolean isLiked, List<HashMap<String, String[]>> comments) {
        this.likeCount = likeCount;
        this.isLiked = isLiked;
        this.comments = comments;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public List<HashMap<String, String[]>> getComments() {
        return comments;
    }

    public void setComments(List<HashMap<String, String[]>> comments) {
        this.comments = comments;
    }
}
