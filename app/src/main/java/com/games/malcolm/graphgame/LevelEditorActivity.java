package com.games.malcolm.graphgame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LevelEditorActivity extends AppCompatActivity {

    private static String TAG = "LevelEditorActivity";

    private InteractiveCircleView mInteractiveCircleView;

    private Button mClearButton;
    private Button mSaveLevelButton;
    private Button mToggleModeButton;
    private EditText mLevelName;

    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_editor);

        Log.d(TAG, "onCreate()");

        mInteractiveCircleView = (InteractiveCircleView) findViewById(R.id.drawing_canvas);

        mClearButton = (Button) findViewById(R.id.button_clear);
        mSaveLevelButton = (Button) findViewById(R.id.button_save_level);
        mToggleModeButton = (Button) findViewById(R.id.button_toggle_mode);
        mLevelName = (EditText) findViewById(R.id.text_level_name);
        mToggleModeButton.setText(mInteractiveCircleView.getMode());

        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mToggleModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInteractiveCircleView.toggleMode();
                mToggleModeButton.setText(mInteractiveCircleView.getMode());

            }
        });


        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInteractiveCircleView.clear();
            }
        });

        mSaveLevelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String levelName = mLevelName.getText().toString();
                if (TextUtils.isEmpty(levelName)) {
                    Toast.makeText(
                            LevelEditorActivity.this,
                            "You must enter a name to save a level.",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(
                            LevelEditorActivity.this,
                            "You must be logged-in to save your level!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference ref = mDatabase.getReference();
                ref.child(mAuth.getCurrentUser().getUid()).child("levels")
                        .child(levelName).setValue(true);
            }
        });
    }
}
