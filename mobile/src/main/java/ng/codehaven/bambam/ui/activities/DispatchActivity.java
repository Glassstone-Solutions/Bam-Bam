package ng.codehaven.bambam.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.parse.ParseUser;

public class DispatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if there is current user info
        if (ParseUser.getCurrentUser() != null) {
            startActivity(new Intent(this, Home.class));
        } else {
            // Start and intent for the logged out activity
            startActivity(new Intent(this, SignIn.class));
        }
    }
}
