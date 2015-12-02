package ng.codehaven.bambam.models;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Thompson on 9/30/2015.
 */
public class Playlist extends RealmObject {
    private String title;
    private String desc;

    private int playhead;

    private RealmList<Track> tracks;

    public Playlist(String mTitle, String mDesc) {
        this.title = mTitle;
        this.desc = mDesc;
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

    public int getPlayhead() {
        return playhead;
    }

    public void setPlayhead(int playhead) {
        this.playhead = playhead;
    }

    public RealmList<Track> getTracks() {
        return tracks;
    }

    public void setTracks(RealmList<Track> tracks) {
        this.tracks = tracks;
    }
}
