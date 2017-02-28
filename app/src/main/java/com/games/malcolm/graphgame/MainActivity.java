package com.games.malcolm.graphgame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    private InteractiveCircleView mInteractiveCircleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInteractiveCircleView = (InteractiveCircleView) findViewById(R.id.drawing_canvas);

        /*mButton = (Button) findViewById(R.id.click_button);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "BUTTON WAS CLICKED");
                Toast.makeText(getApplicationContext(), "Yay!", Toast.LENGTH_LONG).show();
            }
        });*/
    }
}
