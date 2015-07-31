package ng.codehaven.bambam.ui.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import ng.codehaven.bambam.R;
import ng.codehaven.bambam.utils.Logger;

public class SignIn extends AppCompatActivity implements GraphRequest.OnProgressCallback {

    private Logger mLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "ng.codehaven.bambam",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        TextView tos = (TextView) findViewById(R.id.tos);
        tos.setMovementMethod(LinkMovementMethod.getInstance());

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginButtonClicked();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    private void onLoginButtonClicked() {
        List<String> permissions = Arrays.asList("public_profile");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null){
                    if(user.isNew()){
                        makeMeRequest(user);
                    } else {
                        finishActivity();
                    }
                }
            }
        });
    }

    private void makeMeRequest(final ParseUser pUser)  {

        pUser.put("has_username", false);

        // TODO: Change UI to indicate login process
        GraphRequestAsyncTask request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject user, GraphResponse response) {
                pUser.put("name", user.optString("name"));
                pUser.put("fb_id", user.optString("id"));
                pUser.saveEventually();
                finishActivity();
            }
        }).executeAsync();
//        GraphRequestAsyncTask request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), null).executeAsync();

    }

    private void finishActivity() {
        // Start an intent for the dispatch activity
        Intent intent = new Intent(SignIn.this, DispatchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * The method that will be called when progress is made.
     *
     * @param current the current value of the progress of the request.
     * @param max     the maximum value (target) value that the progress will have.
     */
    @Override
    public void onProgress(long current, long max) {
        mLogger = new Logger(String.valueOf(current), "PROGRESS-LOGGER");
    }

    /**
     * The method that will be called when a request completes.
     *
     * @param response the Response of this request, which may include error information if the
     *                 request was unsuccessful
     */
    @Override
    public void onCompleted(GraphResponse response) {
        mLogger = new Logger(response.getJSONObject().toString(), "RESPONSE-LOGGER");
    }
}
