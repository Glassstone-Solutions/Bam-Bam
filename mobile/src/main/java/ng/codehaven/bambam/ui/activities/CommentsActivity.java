package ng.codehaven.bambam.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import io.realm.Realm;
import io.realm.RealmList;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.adapters.CommentsAdapter;
import ng.codehaven.bambam.models.Comment;
import ng.codehaven.bambam.models.Track;
import ng.codehaven.bambam.services.GetCommentsService;
import ng.codehaven.bambam.ui.BaseActivity;
import ng.codehaven.bambam.ui.views.CustomTextView;

import static ng.codehaven.bambam.utils.FileHelper.getBitmapFromURL;

public class CommentsActivity extends BaseActivity implements View.OnClickListener {
    public static final String ARG_DRAWING_START_LOCATION = "arg_drawing_start_location";

    @InjectView(R.id.toolbar)
    protected Toolbar mToolBar;

    @InjectView(R.id.commentsRV)
    SuperRecyclerView mRecycler;
    @InjectView(R.id.evComment)
    EditText mCommentEV;
    @InjectView(R.id.btnSendComment)
    Button mBtnSendComment;
    @InjectView(R.id.art_iv)
    ImageView mArt;
    @InjectView(R.id.tune_card)
    CardView mCardView;

    @InjectView(R.id.title)
    CustomTextView mTitle;
    @InjectView(R.id.desc)
    CustomTextView mDesc;

    private List<Comment> mComments;
    private CommentsAdapter adapter;

    private Realm realm;

    private ParseUser to;
    private ParseObject mTune;

    private Track t;

    @Override
    public int getActivityResourceId() {
        return R.layout.activity_comments;
    }

    @Override
    public boolean hasToolBar() {
        return true;
    }

    @Override
    public Toolbar getToolBar() {
        return mToolBar;
    }

    @Override
    public int getMenuResourceId() {
        return R.menu.menu_comments;
    }

    @Override
    public boolean enableBack() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();

        if (i.getStringExtra("tuneId").isEmpty()) {
            finish();
        }


        mComments = new ArrayList<>();

        realm = Realm.getInstance(this);

        t = realm.where(Track.class).equalTo("parseId", i.getStringExtra("tuneId")).findFirst();
        Log.e("Image url", t.getArtUrl());
        Picasso.with(this).load(t.getArtUrl()).into(mArt, new Callback() {
            @Override
            public void onSuccess() {
                Bitmap bitmap = ((BitmapDrawable) mArt.getDrawable()).getBitmap();
                handleLoadedBitmap(bitmap);
            }

            @Override
            public void onError() {

            }
        });
        loadBitmap(t.getArtUrl());
        mTitle.setText(t.getTitle());
        mDesc.setText(t.getDesc());

        RealmList<Comment> comments = t.getmComments();

        for (Comment cc : comments) {
            mComments.add(cc);
        }

        adapter = new CommentsAdapter(mComments, this);

        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setAdapter(adapter);

        mRecycler.getRecyclerView().smoothScrollToPosition(mComments.size());

        mBtnSendComment.setOnClickListener(this);

    }

    public void loadBitmap(String url) {

        new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... strings) {
                Log.e("***", strings[0]);
                return getBitmapFromURL(strings[0]);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (bitmap != null) {
                    handleLoadedBitmap(bitmap);
                }
            }
        }.execute(url);
    }

    public void handleLoadedBitmap(Bitmap b) {
        // do something here

        if (b != null) {
            Palette.Builder builder = new Palette.Builder(b);

            builder.generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    Palette.Swatch s = palette.getLightVibrantSwatch();
                    if (s != null) {
                        setSwatch(s);
                    } else {
                        Palette.Swatch s1 = palette.getVibrantSwatch();
                        if (s1 != null) {
                            setSwatch(s1);
                        }
                    }
                }
            });
        } else {
            Log.e("***", "Bitmap failed");
        }
    }

    private void setSwatch(Palette.Swatch s) {
        mCardView.setCardBackgroundColor(s.getRgb());
        mTitle.setTextColor(s.getTitleTextColor());
        mDesc.setTextColor(s.getBodyTextColor());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnSendComment) {
            final String comment = mCommentEV.getText().toString().trim();
            if (!comment.isEmpty()) {

                GetCommentsService.startAction(this, t.getParseId(), ParseUser.getCurrentUser().getObjectId(), comment);
                startActivity(new Intent(this, Home.class));

            }
        }
    }
}
