package com.games.malcolm.graphgame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.ActionCodeResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

/**
 * Created by Malcolm on 5/20/2017.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 123;

    private Button mNextLevelButton;
    private Button mSelectLevelButton;
    private Button mLevelEditorButton;
    private TextView mCurrentUser;
    private Button mSignInButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate()");

        mNextLevelButton = (Button) findViewById(R.id.button_next_level);
        mSelectLevelButton = (Button) findViewById(R.id.button_select_level);
        mLevelEditorButton = (Button) findViewById(R.id.button_edit_level);
        mCurrentUser = (TextView) findViewById(R.id.text_user);
        mSignInButton = (Button) findViewById(R.id.button_signin);

        mAuth = FirebaseAuth.getInstance();

        updateCurrentUser();

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() == null) {
                    startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                            .setProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                            .build(), RC_SIGN_IN);
                } else {
                    mAuth.signOut();
                    updateCurrentUser();
                }
            }
        });


        mNextLevelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        MainActivity.this,
                        "Play next level not yet implemented",
                        Toast.LENGTH_SHORT).show();
            }
        });

        mSelectLevelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        MainActivity.this,
                        "Level select not yet implemented",
                        Toast.LENGTH_SHORT).show();
            }
        });

        mLevelEditorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LevelEditorActivity.class);

                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == ResultCodes.OK) {
                updateCurrentUser();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    Log.w(TAG, "User pressed back button");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Log.w(TAG, "No network during sign-in");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Log.e(TAG, "Unknown error during sign-in");
                    return;
                }
            }
        }
    }

    private void updateCurrentUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            mCurrentUser.setText("");
            mSignInButton.setText(R.string.button_signin);
        } else {
            mCurrentUser.setText("Signed-in with: " + user.getDisplayName());
            mSignInButton.setText(R.string.button_signout);
        }
    }
}
