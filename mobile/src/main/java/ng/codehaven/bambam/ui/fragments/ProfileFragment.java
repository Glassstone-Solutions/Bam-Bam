package ng.codehaven.bambam.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.login.widget.ProfilePictureView;
import com.parse.ParseUser;

import org.json.JSONObject;

import ng.codehaven.bambam.R;


public class ProfileFragment extends Fragment {

    private ProfilePictureView p;
    private TextView t;
    private TextView a;
    private SharedPreferences sharedPref;
    private String defaultName;
    private String username;
    private String aboutMe;
    private boolean hasUsername;

    /**
     * Called to do initial creation of a fragment.  This is called after
     * {@link #onAttach(Activity)} and before
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * <p/>
     * <p>Note that this can be called while the fragment's activity is
     * still in the process of being created.  As such, you can not rely
     * on things like the activity's content view hierarchy being initialized
     * at this point.  If you want to do work once the activity itself is
     * created, see {@link #onActivityCreated(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        sharedPref = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);

        defaultName = "defaultName";
        aboutMe = " ";

        username = sharedPref.getString("username", defaultName);
        aboutMe = sharedPref.getString("about", aboutMe);
        hasUsername = sharedPref.getBoolean("hasUsername", false);

        Log.d("USER", username);



    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        p = (ProfilePictureView) v.findViewById(R.id.myProfilePic);
        t = (TextView) v.findViewById(R.id.username);
        a = (TextView) v.findViewById(R.id.about_me);
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

        t.setText(username);
        a.setText(aboutMe);

        GraphRequestAsyncTask request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject user, GraphResponse response) {
                if (user != null) {
                    // set the profile picture using their Facebook ID
                    p.setProfileId(user.optString("id"));
                    if (username.equals(defaultName) || !user.optString("name").equals(username) && !hasUsername) {
                        String un;
                        if (!hasUsername) {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("username", user.optString("name"));
                            editor.putBoolean("hasUsername", true);
                            editor.apply();
                            un = user.optString("name");
                        } else {
                            un = ParseUser.getCurrentUser().getString("f_username");
                        }
                        t.setText(un);
                    }
                }
            }
        }).executeAsync();

    }
}
