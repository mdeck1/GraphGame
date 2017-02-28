package com.games.malcolm.graphgame;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Malcolm on 2/26/2017.
 */

public class DrawingCanvas extends View {

    private Paint mTextPaint;
    private int mTextColor;
    private float mTextHeight;
//    private GestureDetector mDetector = new GestureDetector(DrawingCanvas.this.getContext(), new mListener());

    public DrawingCanvas(Context context) {
        super(context);
    }

    public DrawingCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private static String TAG = "DrawingCanvas";

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, String.format("Size of Canvas: %d, %d", canvas.getHeight(), canvas.getWidth()));
        canvas.drawCircle(492, 600, 20, new Paint());
    }

//    private class mListener extends GestureDetector.SimpleOnGestureListener {
//        @Override
//        public boolean onDown(MotionEvent e) {
//            return true;
//        }
//    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        Log.i(TAG,"Touch detected.");
        return true;
    }

    private void init() {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        if (mTextHeight == 0) {
            mTextHeight = mTextPaint.getTextSize();
        } else {
            mTextPaint.setTextSize(mTextHeight);
        }

        /*mPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPiePaint.setStyle(Paint.Style.FILL);
        mPiePaint.setTextSize(mTextHeight);

        mShadowPaint = new Paint(0);
        mShadowPaint.setColor(0xff101010);
        mShadowPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));
        */
    }
}
