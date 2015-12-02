package ng.codehaven.bambam.models;


import io.realm.RealmObject;

public class Like extends RealmObject {
    private boolean isLiked;

    public Like() {
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }
}
