package com.games.malcolm.graphgame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

/**
 * Created by Malcolm on 5/20/2017.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 123;

    private Button mSignInButton;
    private Button mNextLevelButton;
    private Button mSelectLevelButton;
    private Button mLevelEditorButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate()");

        mSignInButton = (Button) findViewById(R.id.button_signin);
        mNextLevelButton = (Button) findViewById(R.id.button_next_level);
        mSelectLevelButton = (Button) findViewById(R.id.button_select_level);
        mLevelEditorButton = (Button) findViewById(R.id.button_edit_level);

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                        .setProviders(Arrays.asList(
                                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                        .build(), RC_SIGN_IN);
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
}
