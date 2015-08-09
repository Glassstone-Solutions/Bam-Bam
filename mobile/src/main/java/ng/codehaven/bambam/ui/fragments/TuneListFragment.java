package ng.codehaven.bambam.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ng.codehaven.bambam.R;
import ng.codehaven.bambam.adapters.FeedAdapter;


public class TuneListFragment extends Fragment implements FeedAdapter.OnFeedItemClickListener {

    protected RecyclerView mRecyclerView;
    private FeedAdapter feedAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tune_list, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.rvFeed);

        setupFeed(mRecyclerView);

        if (savedInstanceState != null)
            feedAdapter.updateItems(false);

        feedAdapter.updateItems(true);

        return v;
    }

    private void setupFeed(RecyclerView mRecyclerView) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        mRecyclerView.setLayoutManager(linearLayoutManager);
        feedAdapter = new FeedAdapter(getActivity());
        feedAdapter.setOnFeedItemClickListener(this);
        mRecyclerView.setAdapter(feedAdapter);
    }

    @Override
    public void onCommentsClick(View v, int position) {

    }

    @Override
    public void onMoreClick(View v, int position) {

    }

    @Override
    public void onProfileClick(View v) {

    }
}
