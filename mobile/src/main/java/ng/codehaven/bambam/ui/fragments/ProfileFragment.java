package ng.codehaven.bambam.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.adapters.UserProfileAdapter;
import ng.codehaven.bambam.models.Track;
import ng.codehaven.bambam.utils.GetTunesHelper;


public class ProfileFragment extends Fragment implements UserProfileAdapter.UserProfileHandler, SwipeRefreshLayout.OnRefreshListener {

    @InjectView(R.id.profileRV)
    RecyclerView mRecycler;
    @InjectView(R.id.swipeLayout)
    SwipeRefreshLayout mSwipeLayout;


    private UserProfileAdapter adapter;
    private List<Track> t;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance(Bundle args) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        t = new ArrayList<>();

        if (getArguments().getBoolean("isLocal")) {
            t = GetTunesHelper.getTunes(getActivity());
        }

    }

    private void GetTunes(final ParseUser parseUser) {
        ParseQuery<ParseObject> myTunes = new ParseQuery<>("Tunes");
        myTunes.whereEqualTo("owner", parseUser);
        myTunes.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    try {
                        t = GetTunesHelper.GetTunes(list);
                        adapter = new UserProfileAdapter(getActivity(), t, getArguments().getBoolean("isLocal"), parseUser);
                        adapter.setUserProfileHandler(ProfileFragment.this);
                        mRecycler.setAdapter(adapter);
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.inject(this, v);

        mSwipeLayout.setOnRefreshListener(this);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mRecycler.setLayoutManager(layoutManager);

        if (getArguments() != null) {
            if (!getArguments().getBoolean("isLocal")) {
                mSwipeLayout.setRefreshing(true);
                ParseQuery<ParseUser> u = ParseUser.getQuery();
                u.whereEqualTo("objectId", getArguments().getString("userId"));
                u.getFirstInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(final ParseUser parseUser, ParseException e) {
                        if (mSwipeLayout.isRefreshing()) {
                            mSwipeLayout.setRefreshing(false);
                        }
                        GetTunes(parseUser);
                    }
                });
            } else {
                adapter = new UserProfileAdapter(getActivity(), t, true, ParseUser.getCurrentUser());
                mRecycler.setAdapter(adapter);
            }
        } else {
            getActivity().finish();
        }


        return v;
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onItemsPresent(int position) {
        adapter.remove(position);
    }

    @Override
    public void onRefresh() {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mSwipeLayout.isRefreshing()) {
            mSwipeLayout.setRefreshing(false);
        }

    }
}
