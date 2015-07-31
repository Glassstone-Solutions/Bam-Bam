package ng.codehaven.bambam.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.ParseUser;

import butterknife.ButterKnife;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.ui.activities.DispatchActivity;
import ng.codehaven.bambam.utils.Logger;

public abstract class BaseActivity extends AppCompatActivity {

    public abstract int getActivityResourceId();
    public abstract boolean hasToolBar();
    public abstract Toolbar getToolBar();
    public abstract int getMenuResourceId();

    protected ParseUser mCurrentUser;
    protected Logger mLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getActivityResourceId());

        ButterKnife.inject(this);

        if (hasToolBar()){
            setupToolBar(getToolBar());
        }

        // Set current user
        mCurrentUser = ParseUser.getCurrentUser();

        // Setup Logger

    }

    private void setupToolBar(Toolbar toolBar) {
        setSupportActionBar(toolBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(getMenuResourceId(), menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //TODO: Remember to add cases of each menu item created
//        if (id == R.id.action_settings) {
//            return true;
//        }
        switch (id){
            case R.id.action_signout:
                signout();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signout() {
        if (mCurrentUser.isAuthenticated()) {
            ParseUser.logOut();
            mCurrentUser = null;
        }

        Intent i = new Intent(this, DispatchActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

    }
}
