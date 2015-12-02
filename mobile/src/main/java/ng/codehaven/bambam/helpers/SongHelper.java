package ng.codehaven.bambam.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import ng.codehaven.bambam.Common;
import ng.codehaven.bambam.models.Track;

/**
 * Created by Thompson on 9/28/2015.
 */
public class SongHelper {

    private SongHelper mSongHelper;
    private Common mApp;
    private int mIndex;
    private boolean mIsCurrentSong = false;
    private boolean mIsAlbumArtLoaded = false;

    //Song parameters.
    private String mTitle;
    private String mArtist;
    private String mFilePath;
    private String mId;
    private String mAlbumArtPath;
    private Bitmap mAlbumArt;

    private AlbumArtLoadedListener mAlbumArtLoadedListener;
    private String id;

    public void populateSongData(Context mContext, int songIndex) {

    }

    /**
     * Interface that provides callbacks to the provided listener
     * once the song's album art has been loaded.
     */
    public interface AlbumArtLoadedListener {

        /**
         * Called once the album art bitmap is ready for use.
         */
        void albumArtLoaded();

    }
    /**
     * Moves the specified cursor to the specified index and populates this
     * helper object with new song data.
     *
     * @param context             Context used to get a new Common object.
     * @param index               The index of the song.
     * @param albumArtTransformer The transformer to apply to the album art bitmap;
     */
    public void populateSongData(Context context, int index, Transformation albumArtTransformer) {
        mSongHelper = this;
        mApp = (Common) context.getApplicationContext();
        mIndex = index;

        if (mApp.isServiceRunning()) {
            Track t = mApp.getService().getTrack(index);
            this.setId(t.getParseId());
            this.setTitle(t.getTitle());
            this.setArtist(t.getArtistName());
            this.setFilePath(t.getTrackUrl());
            this.setAlbumArtPath(t.getArtUrl());
            mApp.getPicasso()
                    .load(getAlbumArtPath())
                    .transform(albumArtTransformer)
                    .into(imageLoadingTarget);
        }

    }

    /**
     * Image loading listener to store the current song's album art.
     */
    Target imageLoadingTarget = new Target() {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mIsAlbumArtLoaded = true;
            setAlbumArt(bitmap);
            if (getAlbumArtLoadedListener()!=null)
                getAlbumArtLoadedListener().albumArtLoaded();

            if (mIsCurrentSong) {
                mApp.getService().updateNotification(mSongHelper);
                mApp.getService().updateWidgets();

            }

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            setAlbumArt(null);
            onBitmapLoaded(mAlbumArt, null);

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            mIsAlbumArtLoaded = false;

        }

    };


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String mFilePath) {
        this.mFilePath = mFilePath;
    }

    public Bitmap getAlbumArt() {
        return mAlbumArt;
    }

    public void setAlbumArt(Bitmap mAlbumArt) {
        this.mAlbumArt = mAlbumArt;
    }

    public String getAlbumArtPath() {
        return mAlbumArtPath;
    }

    public void setAlbumArtPath(String mAlbumArtPath) {
        this.mAlbumArtPath = mAlbumArtPath;
    }

    public void setAlbumArtLoadedListener(AlbumArtLoadedListener listener) {
        mAlbumArtLoadedListener = listener;
    }

    public AlbumArtLoadedListener getAlbumArtLoadedListener() {
        return mAlbumArtLoadedListener;
    }
}
