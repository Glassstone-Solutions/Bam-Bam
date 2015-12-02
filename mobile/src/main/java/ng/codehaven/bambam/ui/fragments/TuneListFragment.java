package ng.codehaven.bambam.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.malinskiy.superrecyclerview.swipe.SwipeDismissRecyclerViewTouchListener;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.ui.adapters.FeedAdapter;
import ng.codehaven.bambam.models.Track;
import ng.codehaven.bambam.services.UploadTuneService;
import ng.codehaven.bambam.ui.activities.CommentsActivity;


public class TuneListFragment extends Fragment implements
        FeedAdapter.OnFeedItemClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        OnMoreListener,
        SwipeDismissRecyclerViewTouchListener.DismissCallbacks {

    protected SuperRecyclerView mRecyclerView;
    protected Handler handler;
    private FeedAdapter feedAdapter;
    private List<Track> mTunes;
    private Realm realm;
    private SimpleDateFormat format;
    private boolean mDoneTrackSaveBroadcastReceiver;

    private FragmentInteraction mHandler;

    private static final int TIMEOUT = 1000 * 60 * 4;

    private BroadcastReceiver doneUploadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doRefresh();
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mHandler = (FragmentInteraction) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        mTunes = new ArrayList<>();

        format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

        realm = Realm.getInstance(getActivity());

        RealmResults<Track> r = realm.where(Track.class).findAllSorted("createdAt", RealmResults.SORT_ORDER_DESCENDING);
        for (Track t : r) {
            mTunes.add(t);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tune_list, container, false);

        mRecyclerView = (SuperRecyclerView) v.findViewById(R.id.rvFeed);

        setupFeed(mRecyclerView);

        if (savedInstanceState != null)
            feedAdapter.updateItems(false);

        feedAdapter.updateItems(true);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mDoneTrackSaveBroadcastReceiver) {
            getActivity().registerReceiver(doneUploadReceiver, new IntentFilter(UploadTuneService.S3_KEY_DONE_ACTION));
            mDoneTrackSaveBroadcastReceiver = true;
        }
        if (mTunes != null) {
            doRefresh();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDoneTrackSaveBroadcastReceiver) {
            getActivity().unregisterReceiver(doneUploadReceiver);
            mDoneTrackSaveBroadcastReceiver = false;
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setupFeed(SuperRecyclerView mRecyclerView) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        mRecyclerView.setLayoutManager(linearLayoutManager);
        feedAdapter = new FeedAdapter(getActivity(), mTunes);
        feedAdapter.setOnFeedItemClickListener(this);
        mRecyclerView.setRefreshListener(this);
        mRecyclerView.setRefreshingColor(
                R.color.ColorPrimary,
                R.color.md_yellow_600,
                R.color.md_purple_600,
                R.color.md_light_green_600);
        mRecyclerView.setupMoreListener(this, 1);
        mRecyclerView.setAdapter(feedAdapter);
    }





    @Override
    public void onRefresh() {
        if (mTunes != null) {
            doRefresh();
        }
    }

    private void doRefresh() {
        int skip = mTunes.size();
        final SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

        ParseQuery<ParseObject> friends = getFriendsQuery();

        ParseQuery<ParseObject> friendsTunes = getFriendsTunes(friends);

        ParseQuery<ParseObject> myTune = getMyTunes();

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();

        queries.add(friendsTunes);
        queries.add(myTune);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
        mainQuery.setSkip(skip);
        mainQuery.setLimit(25);
        mainQuery.orderByAscending("createdAt");
        if (mTunes.size() > 0) {
            mainQuery.whereGreaterThan("createdAt", mTunes.get(0).getCreatedAt());
        }
        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> list, ParseException e) {
                if (mRecyclerView.getSwipeToRefresh().isRefreshing()) {
                    mRecyclerView.getSwipeToRefresh().setRefreshing(false);
                }
                if (e == null) {
                    Log.e("COUNT", String.valueOf(list.size()));
                    for (final ParseObject i : list) {
                        i.getParseUser("owner").fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject user, ParseException e) {
                                if (e == null) {
                                    Track t;
                                    try {
                                        t = new Track(
                                                i.getString("title"),
                                                i.getObjectId(),
                                                i.getString("desc"),
                                                i.getParseFile("track").getUrl(),
                                                i.getParseFile("art").getUrl(),
                                                user.getString("f_username"),
                                                user.getObjectId(),
                                                i.getBoolean("forSale"),
                                                false,
                                                format.parse(i.getCreatedAt().toString())
                                        );
                                    } catch (java.text.ParseException e1) {
                                        e1.printStackTrace();
                                        t = null;
                                    }
                                    assert t != null;
                                    insertIntoRecycler(t);
                                }
                            }
                        });
                    }

                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    @NonNull
    private ParseQuery<ParseObject> getFriendsTunes(ParseQuery<ParseObject> friends) {
        ParseQuery<ParseObject> friendsTunes = new ParseQuery<>("Tunes");
        friendsTunes.whereMatchesKeyInQuery("owner", "to", friends);
        return friendsTunes;
    }

    private void insertIntoRecycler(Track t) {
        if (mTunes.size() == 0 || !mTunes.get(0).getParseId().equals(t.getParseId())) {
            mTunes.add(0, t);
            feedAdapter.notifyItemInserted(0);
            mRecyclerView.getRecyclerView().smoothScrollToPosition(0);

            try {
                realm.beginTransaction();

                Track trackRealm = realm.copyToRealm(t);

                realm.commitTransaction();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean canDismiss(int position) {
        return mTunes.get(position).getArtistObjId().equals(ParseUser.getCurrentUser().getObjectId());
    }

    @Override
    public void onDismiss(RecyclerView recyclerView, int[] ints) {
        // TODO: Remove from db and delete from Parse.com before you remove from array
    }

    @Override
    public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
//        mRecyclerView.hideMoreProgress();

        ParseQuery<ParseObject> friends = getFriendsQuery();

        ParseQuery<ParseObject> friendsTunes = getFriendsTunes(friends);

        ParseQuery<ParseObject> myTune = getMyTunes();

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();

        queries.add(friendsTunes);
        queries.add(myTune);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
        mainQuery.setSkip(numberBeforeMore);
        mainQuery.setLimit(25);
        mainQuery.orderByDescending("createdAt");
        mainQuery.whereLessThan("createdAt", mTunes.get(mTunes.size() - 1).getCreatedAt());

        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    mRecyclerView.hideMoreProgress();
                    for (final ParseObject i : list) {
                        i.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject user, ParseException e) {
                                if (e == null) {
                                    String id = i.getObjectId();
                                    RealmResults<Track> r = realm.where(Track.class).equalTo("parseId", id).findAll();

                                    if (r.size() == 0){
                                        try {
                                            Track t = new Track(
                                                i.getString("title"),
                                                i.getObjectId(),
                                                i.getString("desc"),
                                                i.getParseFile("track").getUrl(),
                                                i.getParseFile("art").getUrl(),
                                                user.getString("f_username"),
                                                user.getObjectId(),
                                                i.getBoolean("forSale"),
                                                false,
                                                format.parse(i.getCreatedAt().toString())
                                            );
                                            mTunes.add(t);
                                            feedAdapter.notifyItemInserted(mTunes.size());
                                            realm.beginTransaction();
                                            Track trackRealm = realm.copyToRealm(t);
                                            realm.commitTransaction();
                                        } catch (java.text.ParseException e1) {
                                            e1.printStackTrace();
                                        }
                                    } else {
                                        Log.e("COUNT-REALM", String.valueOf(r.size()));
                                    }

                                }
                            }
                        });
                    }
                } else {
                    e.printStackTrace();
                    mRecyclerView.hideMoreProgress();
                }
            }
        });
    }

    @NonNull
    private ParseQuery<ParseObject> getMyTunes() {
        ParseQuery<ParseObject> myTune = new ParseQuery<>("Tunes");
        myTune.whereEqualTo("owner", ParseUser.getCurrentUser());
        return myTune;
    }

    @NonNull
    private ParseQuery<ParseObject> getFriendsQuery() {
        ParseQuery<ParseObject> friends = new ParseQuery<>("Activities");
        friends.whereEqualTo("type", "follow");
        friends.whereEqualTo("from", ParseUser.getCurrentUser());
        return friends;
    }

    @Override
    public void onCommentsClick(View v, int position) {
        Track t = mTunes.get(position);
        Log.e("Start-Service", t.getTitle());
        Intent i = new Intent(getActivity(), CommentsActivity.class);
        i.putExtra("tuneId", t.getParseId());
        getActivity().startActivity(i);
    }@Override
     public void onMoreClick(View v, int position) {

    }

    @Override
    public void onProfileClick(View v) {

    }

    @Override
    public void onCenterIVClick(int i, Track t) {
//        Uri trackUri = Uri.parse(t.getTrackUrl());
//        Intent intent = new Intent(getActivity(), MusicService.class);
//        intent.setData(trackUri);
//        intent.setAction(MusicService.ACTION_ADD);
//        intent.putExtra("trackId", t.getParseId());
//        getActivity().startService(intent);
        mHandler.onCenterIVClick(t);
    }

    public interface FragmentInteraction {
        void commentClicked(View v, Track t);
        void onCenterIVClick(Track t);
        void onScroll(int dx, int dy);
    }
}
