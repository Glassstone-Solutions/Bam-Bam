package ng.codehaven.bambam.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import butterknife.InjectView;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.ui.BaseActivity;

public class EditProfile extends BaseActivity {

    @InjectView(R.id.toolbar)
    protected Toolbar mToolBar;
    @InjectView(R.id.username_view)
    protected EditText mUsernameView;
    @InjectView(R.id.about_me_view)
    protected EditText mAboutMeView;
    @InjectView(R.id.content)
    protected CoordinatorLayout mCoordinatorLayout;
    protected SharedPreferences sharedPref;
    protected SharedPreferences.Editor editor;
    private boolean isUsernameUnique;
    private String un;
    private InputMethodManager manager;

    @Override
    public int getActivityResourceId() {
        return R.layout.activity_edit_profile;
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
        return R.menu.menu_edit_profile;
    }

    @Override
    public boolean enableBack() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);

        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mUsernameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                un = charSequence.toString().toLowerCase().trim();
                new CheckUsernameTask().execute(un);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean r = false;
        int id = item.getItemId();
        switch (id) {
            case R.id.action_send:
                manager.hideSoftInputFromWindow(mAboutMeView.getWindowToken(), 0);
                doEditProfile();
                r = true;
                break;
        }
        return r;
    }

    private void doEditProfile() {

        mProgressBar.setVisibility(View.VISIBLE);

        final ParseUser u = ParseUser.getCurrentUser();

        final String username = mUsernameView.getText().toString().trim();
        String usernameLowerCase = username.toLowerCase().trim();

        String about = mAboutMeView.getText().toString().trim();

        if (!username.isEmpty()) {
            if (isUsernameUnique || pUsers.get(0).getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                u.setUsername(usernameLowerCase);
                u.put("f_username", username);
            } else {
                Snackbar.make(mCoordinatorLayout, R.string.taken_username_text, Snackbar.LENGTH_LONG).show();
            }
        } else {
            Snackbar.make(mCoordinatorLayout, R.string.empty_username_text, Snackbar.LENGTH_LONG).show();
        }

        if (!about.isEmpty()) {
            u.put("about", about);
        }


        u.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("username", u.getString("f_username"));
                    editor.putString("about", u.getString("about"));
                    editor.putBoolean("hasUsername", true);
                    editor.apply();
                    NavUtils.navigateUpFromSameTask(EditProfile.this);
                } else {
                    Snackbar.make(mCoordinatorLayout, R.string.general_error_text, Snackbar.LENGTH_LONG).setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            doEditProfile();
                        }
                    }).show();
                }
            }
        });


    }

    private class CheckUsernameTask extends AsyncTask<String, Void, List<ParseUser>> {

        @Override
        protected List<ParseUser> doInBackground(String... strings) {
            try {
                return checkUserName(strings[0]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ParseUser> aBoolean) {
            super.onPostExecute(aBoolean);
            isUsernameUnique = aBoolean.size() == 0;
            pUsers = aBoolean;
        }
    }
}
