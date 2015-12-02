package ng.codehaven.bambam.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.realm.Realm;
import io.realm.RealmResults;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.models.Comment;
import ng.codehaven.bambam.models.Track;
import ng.codehaven.bambam.utils.FontCache;
import ng.codehaven.bambam.utils.GetTunesHelper;

public class CheckForTunesActivity extends AppCompatActivity {

    protected SharedPreferences sharedPref;
    protected TextView mToolBarTitle;
    @InjectView(R.id.toolbar)
    protected Toolbar mToolBar;
    JSONArray tuneLineArray, myTunesArray;
    boolean checked;
    int count = 0;
    int cc = 0;
    int tc = 0;
    @InjectView(R.id.progress)
    CircularProgressView mProgress;
    @InjectView(R.id.errorLayout)
    LinearLayout mErrorLayout;
    @InjectView(R.id.errorImg)
    ImageView mErrorImage;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_for_tunes);

        ButterKnife.inject(this);

        // Obtain a Realm instance
        realm = Realm.getInstance(this);

        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mToolBarTitle = (TextView) mToolBar.findViewById(R.id.toolbar_title);
        mToolBarTitle.setTypeface(FontCache.get("fonts/GrandHotel-Regular.otf", this));
        mToolBarTitle.setText(getString(R.string.app_name));
        mToolBarTitle.setTextColor(getResources().getColor(R.color.ColorPrimary));

        myTunesArray = new JSONArray();
        tuneLineArray = new JSONArray();

        mErrorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checked) {
                    doCheck();
                    checked = false;
                }
            }
        });

//        Get Timeline
        if (ParseUser.getCurrentUser() == null) {
            startActivity(new Intent(this, SignIn.class));
        } else {
            doCheck();
        }

    }

    private void doCheck() {
        if (mProgress.getVisibility() == View.INVISIBLE && mErrorLayout.getVisibility() == View.VISIBLE) {
            mProgress.setVisibility(View.VISIBLE);
            mErrorLayout.setVisibility(View.INVISIBLE);
        }

        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        Date sda = new Date(System.currentTimeMillis() - (7 * DAY_IN_MS));

        ParseQuery<ParseObject> friends = new ParseQuery<>("Activities");
        friends.whereEqualTo("type", "follow");
        friends.whereEqualTo("from", ParseUser.getCurrentUser());

        ParseQuery<ParseObject> friendsTunes = new ParseQuery<>("Tunes");
        friendsTunes.whereMatchesKeyInQuery("owner", "to", friends);

        ParseQuery<ParseObject> myTune = new ParseQuery<>("Tunes");
        myTune.whereEqualTo("owner", ParseUser.getCurrentUser());

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();

        queries.add(friendsTunes);
        queries.add(myTune);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
        mainQuery.setLimit(25);
        mainQuery.whereGreaterThanOrEqualTo("createdAt", sda);
        mainQuery.orderByDescending("createdAt");
        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> results, ParseException e) {
                checked = true;
                if (e == null) {
                    if (results.size() > 0) {
                        Log.e("BAMBAM", String.valueOf(results.size()));
                        executeSave(results);
                    } else {
                        applyToSharedPreferences("tunes");

                        startActivity(new Intent(CheckForTunesActivity.this, Home.class));
                    }
                } else {
                    e.printStackTrace();
                    mProgress.setVisibility(View.INVISIBLE);
                    mErrorLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void executeSave(final List<ParseObject> results) {
        for (final ParseObject i : results) {
            i.getParseUser("owner").fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(final ParseObject user, ParseException e) {
                    if (e == null) {

                        AsyncTask getLikeTask = new AsyncTask<Void, Void, Boolean>() {
                            @Override
                            protected Boolean doInBackground(Void... voids) {

                                try {
                                    return GetTunesHelper.getTuneLike(i.getObjectId());
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                    return false;
                                }
                            }

                            @Override
                            protected void onPostExecute(Boolean like) {
                                super.onPostExecute(like);
                                Track track = getTrack(user, i, like);

                                realm.beginTransaction();

                                Track trackRealm = realm.copyToRealm(track);

                                realm.commitTransaction();

                                count = count + 1;
                                Log.e("COUNT", String.valueOf(count) + " " + track.getTitle());
                                if (count >= results.size()) {
                                    ParseQuery<ParseObject> myTunes = new ParseQuery<ParseObject>("Tunes");
                                    myTunes.whereEqualTo("owner", ParseUser.getCurrentUser());
                                    myTunes.findInBackground(new FindCallback<ParseObject>() {
                                        @Override
                                        public void done(List<ParseObject> list, ParseException e) {
                                            if (e == null) {

                                                for (ParseObject i : list) {
                                                    RealmResults<Track> t = realm.where(Track.class).equalTo("parseId", i.getObjectId()).findAll();
                                                    if (t.size() == 0) {
                                                        Track tt = getTrack(ParseUser.getCurrentUser(), i, false);
                                                        realm.beginTransaction();
                                                        Track tr = realm.copyToRealm(tt);
                                                        realm.commitTransaction();
                                                    }
                                                }

                                                doComments();
                                            } else {
                                                e.printStackTrace();
                                                mProgress.setVisibility(View.INVISIBLE);
                                                mErrorLayout.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });

                                }
                            }
                        }.execute();


                    } else {
                        e.printStackTrace();
                        mProgress.setVisibility(View.INVISIBLE);
                        mErrorLayout.setVisibility(View.VISIBLE);
                    }

                }
            });
        }

    }

    private void doComments() {


        final RealmResults<Track> r = realm.where(Track.class).findAll();

        for (final Track t : r) {
            ParseQuery<ParseObject> obj = ParseQuery.getQuery("Activities");
            obj.whereEqualTo("tuneId", t.getParseId());
            obj.whereEqualTo("type", "comment");
            obj.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(final List<ParseObject> list, ParseException e) {
                    if (e == null) {
                        for (final ParseObject p : list) {
                            p.getParseUser("from").fetchIfNeededInBackground(new GetCallback<ParseUser>() {
                                @Override
                                public void done(ParseUser user, ParseException e) {
                                    if (e == null) {
                                        realm.beginTransaction();
                                        Comment c = new Comment();
                                        c.setmUsername(user.getString("f_username"));
                                        c.setmComment(p.getString("content"));
                                        c.setmAvatar("https://graph.facebook.com/" + user.getString("fb_id") + "/picture?type=large");
                                        c.setmCreatedAt(p.getCreatedAt());
                                        t.getmComments().add(c);
                                        realm.commitTransaction();

                                        if (cc >= list.size() && tc >= r.size()) {

                                            applyToSharedPreferences("tunes");

                                            startActivity(new Intent(CheckForTunesActivity.this, Home.class));

                                        }

                                    } else {
                                        e.printStackTrace();
                                        mProgress.setVisibility(View.INVISIBLE);
                                        mErrorLayout.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                            cc++;
                        }
                    } else {
                        e.printStackTrace();
                        mProgress.setVisibility(View.INVISIBLE);
                        mErrorLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
            tc++;
            cc = 0;
        }
    }

    @NonNull
    private Track getTrack(ParseObject user, ParseObject i, boolean like) {
        return new Track(
                i.getString("title"),
                i.getObjectId(),
                i.getString("desc"),
                i.getParseFile("track").getUrl(),
                i.getParseFile("art").getUrl(),
                user.getString("f_username"),
                user.getObjectId(),
                i.getBoolean("forSale"),
                like,
                i.getCreatedAt()
        );
    }

    private void applyToSharedPreferences(String key) {
        sharedPref = getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("checkedForTunes", true);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
