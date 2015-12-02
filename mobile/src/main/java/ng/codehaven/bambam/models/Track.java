package ng.codehaven.bambam.models;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Track extends RealmObject {
    public static final String TRACK_TITLE = "title";

    public static final String TRACK_DESC = "desc";
    public static final String ART_URL = "artUrl";
    public static final String TRACK_URL = "trackUrl";
    public static final String TRACK_LIKES_COUNT = "likesCount";
    public static final String TRACK_COMMENTS_COUNT = "commentsCount";
    public static final String TRACK_PLAY_COUNT = "playCount";
    public static final String TRACK_DOWNLOAD_COUNT = "downloadCount";
    public static final String TRACK_POINTS = "points";
    @PrimaryKey
    private String title;

    private String desc, artUrl, trackUrl, parseId, artistObjId, artistName, c1, c2, c3;
    private Date createdAt;

    private RealmList<Comment> mComments;

    private Like mLike;

    private boolean forSale, isLiked;

    private int likesCount, commentsCount, playCount, downloadCount, points;

    public Track(String title, String desc) {
        this.title = title;
        this.desc = desc;
    }

    public Track(String mTitle,
                 String mObjectId,
                 String mDesc,
                 String mTuneUrl,
                 String mTuneArtUrl,
                 String mArtisteName,
                 String mArtisteObjectId,
                 boolean mForSale,
                 boolean mIsLiked,
                 Date mCreatedAt) {

        this.title = mTitle;
        this.desc = mDesc;
        this.artUrl = mTuneArtUrl;
        this.trackUrl = mTuneUrl;
        this.parseId = mObjectId;
        this.artistObjId = mArtisteObjectId;
        this.artistName = mArtisteName;
        this.forSale = mForSale;
        this.isLiked = mIsLiked;
        this.createdAt = mCreatedAt;
    }

    public String getC1() {
        return c1;
    }

    public void setC1(String c1) {
        this.c1 = c1;
    }

    public String getC2() {
        return c2;
    }

    public void setC2(String c2) {
        this.c2 = c2;
    }

    public String getC3() {
        return c3;
    }

    public void setC3(String c3) {
        this.c3 = c3;
    }

    public void setTrackUrl(String trackUrl) {
        this.trackUrl = trackUrl;
    }

    public void setParseId(String parseId) {
        this.parseId = parseId;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isLiked() {
        return isLiked;
    }


    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistObjId() {
        return artistObjId;
    }

    public void setArtistObjId(String artistObjId) {
        this.artistObjId = artistObjId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getArtUrl() {
        return artUrl;
    }

    public void setArtUrl(String artUrl) {

        this.artUrl = artUrl;
    }

    public String getTrackUrl() {
        return trackUrl;
    }

    public Track() {
    }

    public String getParseId() {
        return parseId;
    }

    public boolean isForSale() {
        return forSale;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean getForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public RealmList<Comment> getmComments() {
        return mComments;
    }

    public void setmComments(RealmList<Comment> mComments) {
        this.mComments = mComments;
    }

    public void setLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public Like getmLike() {
        return mLike;
    }

    public void setmLike(Like mLike) {
        this.mLike = mLike;
    }
}
