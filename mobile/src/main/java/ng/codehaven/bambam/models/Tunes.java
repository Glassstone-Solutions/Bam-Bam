package ng.codehaven.bambam.models;

import android.content.Context;
import android.net.Uri;

import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ng.codehaven.bambam.Constants;
import ng.codehaven.bambam.utils.FileHelper;

@ParseClassName("Tunes")
public class Tunes extends ParseObject {

    private ParseUser mCurrentUser;

    public Tunes() {}

    public String getTitle() {
        return getString("Title");
    }

    public void setTitle(String title) {
        put("Title", title);
    }

    public ParseUser getArtist() {
        return getParseUser("parent");
    }

    public void setArtist(ParseUser user) {
        put("parent", user);
    }

    public ParseFile getSongFile() {
        return getParseFile("file");
    }

    public void setSongFile(ParseFile file) {
        put("file", file);
    }

    public ParseFile getCoverArt() {
        return getParseFile("coverArt");
    }

    public void setCoverArt(ParseFile file) {
        put("coverArt", file);
    }

    public String getFileType() {
        return getString(Constants.KEY_FILE_TYPE);
    }

    public void setFileType(String fileType) {
        put(Constants.KEY_FILE_TYPE, fileType);
    }

    public int getLikeCount(){
        return getInt("Likes");
    }

    public Date getTuneCreatedAt(){
        return getCreatedAt();
    }

    public void increaseLike(int likeCount){
        increment("Likes", +likeCount);
    }
    public void reduceLike(int likeCount){
        if (getLikeCount() > 0) {
            increment("Likes", -likeCount);
        }
    }



    public boolean sendTune(final Context ctx, String title, final Tunes t, Uri mTuneUri, final String mFileType,final Uri aUri) throws ParseException {
        boolean isSaved = false;
        mCurrentUser = ParseUser.getCurrentUser();
        t.setArtist(ParseUser.getCurrentUser());
        t.setTitle(title);
        byte[] fileBytes = FileHelper.getByteArrayFromFile(ctx, mTuneUri);

        String fileName = FileHelper.getFileName(ctx, mTuneUri, mFileType);
        final ParseFile file = new ParseFile(fileName, fileBytes);


        file.save();

        if(file.isDataAvailable()){
            isSaved = true;
        }

        return isSaved;



//        file.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                t.setSongFile(file);
//                t.setFileType(mFileType);
//                if (aUri != null){
//                    byte[] fb = FileHelper.getByteArrayFromFile(ctx, aUri);
//                    String fn = FileHelper.getFileName(ctx, aUri, ParseConstants.TYPE_IMAGE);
//                    ParseFile ia = new ParseFile(fn, fb);
//                    t.setCoverArt(ia);
//                }
//                t.saveInBackground(new SaveCallback() {
//                    @Override
//                    public void done(ParseException e) {
//                        Activities activity = new Activities();
//                        activity.setActivityType("tune");
//                        activity.setFrom(mCurrentUser);
//                        activity.setTuneId(t.getObjectId());
//                    }
//                });
//            }
//        });

    }

    private Tunes getTune(JSONObject tune){
        return null;
    }

    public List<Tunes> getTuneList() throws ParseException, JSONException {
        mCurrentUser = ParseUser.getCurrentUser();
        JSONObject userJson = new JSONObject();
        try {
            userJson.put("__type", "Pointer");
            userJson.put("className", "_User");
            userJson.put("objectId", mCurrentUser.getObjectId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject user = new JSONObject();
        try {
            user.put("user", userJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HashMap<String, ParseUser> params = new HashMap<>();
        params.put("user", mCurrentUser);
        List<Tunes> t = new ArrayList<>();
        Object b = ParseCloud.callFunction("tuneline", params);

        JSONObject tunes = new JSONObject(b.toString());
        JSONArray tunesArray = tunes.getJSONArray(Constants.KEY_TUNELINE_ARRAY_NAME);

        if (tunesArray.length()>0){
            for (int i = 0; i < tunesArray.length(); i++){
                t.add(getTune(tunesArray.getJSONObject(i)));
            }
        }

        return t;

    }

}
