package ng.codehaven.bambam.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.facebook.AccessToken;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import ng.codehaven.bambam.models.Comment;
import ng.codehaven.bambam.models.Track;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GetCommentsService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CREATE = "ng.codehaven.bambam.services.action.CREATE_COMMENT";
    private static final String ACTION_GET = "ng.codehaven.bambam.services.action.GET_COMMENT";
    private static final String ACTION_DESTROY = "ng.codehaven.bambam.services.action.DESTROY_COMMENT";


    private static final String EXTRA_TUNE_ID = "ng.codehaven.bambam.services.extra.TUNE_ID";
    private static final String EXTRA_USER_ID = "ng.codehaven.bambam.services.extra.USER_ID";
    private static final String EXTRA_COMMENT = "ng.codehaven.bambam.services.extra.EXTRA_COMMENT";

    Realm r;

    public GetCommentsService() {
        super("GetCommentsService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startAction(Context context, String tune_id, String user_id, String comment) {
        Intent intent = new Intent(context, GetCommentsService.class);
        intent.setAction(ACTION_CREATE);
        intent.putExtra(EXTRA_TUNE_ID, tune_id);
        intent.putExtra(EXTRA_USER_ID, user_id);
        intent.putExtra(EXTRA_COMMENT, comment);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, GetCommentsService.class);
        intent.setAction(ACTION_DESTROY);
        intent.putExtra(EXTRA_TUNE_ID, param1);
        intent.putExtra(EXTRA_USER_ID, param2);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setIntentRedelivery(false);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            r = Realm.getInstance(this);
            if (ACTION_CREATE.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_TUNE_ID);
                final String param2 = intent.getStringExtra(EXTRA_COMMENT);
                try {
                    createComment(param1, param2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else if (ACTION_GET.equals(action)) {
                // TODO: Iterate through local db and update comments
                try {
                    iterate();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else if (ACTION_DESTROY.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_TUNE_ID);
                final String param2 = intent.getStringExtra(EXTRA_USER_ID);
                deleteComment(param1, param2);
            }
        }
    }

    private void iterate() throws ParseException {
        RealmResults<Track> results = r.where(Track.class).findAll();

        for (Track t : results){
            ParseQuery<ParseObject> comments = ParseQuery.getQuery("Activities");
            comments.whereEqualTo("tuneId", t.getParseId());
            comments.whereEqualTo("type", "comment");

            List<ParseObject> mComments = comments.find();

            for (ParseObject i : mComments){
                ParseUser user;
                if (i.getParseUser("from") != null){
                    user = i.getParseUser("from");

                    user.fetchIfNeeded();

                    r.beginTransaction();
                    Comment c = new Comment();
                    c.setmUsername(user.getString("f_username"));
                    c.setmComment(i.getString("content"));
                    c.setmAvatar("https://graph.facebook.com/" + user.getString("fb_id") + "/picture?type=large");
                    c.setmCreatedAt(i.getCreatedAt());
                    try {
                        t.getmComments().add(c);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    r.commitTransaction();
                }
            }

        }

    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     * @param param1 Tune Id
     * @param param2 Comment
     */
    private void createComment(String param1, String param2) throws ParseException {
        ParseQuery<ParseObject> tune = ParseQuery.getQuery("Tunes");
        tune.whereEqualTo("objectId", param1);

        ParseObject t = tune.getFirst();

        ParseObject comment = new ParseObject("Activities");
        comment.put("to", t.getParseUser("owner"));
        comment.put("from", ParseUser.getCurrentUser());
        comment.put("tune", t);
        comment.put("type", "comment");
        comment.put("content", param2);

        comment.save();

        Track track = r.where(Track.class).equalTo("parseId", param1).findFirst();

        r.beginTransaction();
        Comment rComment = r.copyToRealm(new Comment(
                param2,
                ParseUser.getCurrentUser().getString("f_username"),
                "https://graph.facebook.com/" + AccessToken.getCurrentAccessToken().getUserId() + "/picture?type=large",
                comment.getCreatedAt()
        ));
        track.getmComments().add(rComment);
        r.commitTransaction();
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void deleteComment(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
