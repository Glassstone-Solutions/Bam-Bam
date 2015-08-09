package ng.codehaven.bambam.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.parse.ParseUser;

import ng.codehaven.bambam.BuildType;
import ng.codehaven.bambam.utils.Connectivity;
import ng.codehaven.bambam.utils.IntentUtil;

public class DispatchActivity extends AppCompatActivity {

    private IntentUtil iUtil = new IntentUtil(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Connectivity mConnect = new Connectivity(this);

        // Check if there is current user info
        if (ParseUser.getCurrentUser() != null) {
            iUtil.goToActivity(new Intent(this, Home.class));
        } else {
            if (!mConnect.isConnectingToInternet()) {

                switch (BuildType.type) {
                    case 0:
                        doDebug();
                        break;
                    case 1:
                        doReleaseNoInternet();
                        break;
                }

            } else {
                // Start and intent for the logged out activity
                iUtil.goToActivity(new Intent(this, SignIn.class));
            }
        }
    }


    private void doReleaseNoInternet() {
        // Start and intent for the logged out activity
        iUtil.goToActivity(new Intent(this, SignIn.class));
    }

    private void doDebug() {
        // Start and intent for the logged out activity
        iUtil.goToActivity(new Intent(this, SignIn.class));
    }
}
