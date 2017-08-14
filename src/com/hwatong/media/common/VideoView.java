package com.hwatong.media.common;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

public class VideoView extends SurfaceView {
	private static final String TAG = "Video";
	private static final boolean DBG = true;

	private int mVideoWidth = -1;
	private int mVideoHeight = -1;

    public VideoView(Context context) {
        super(context);
    }

    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if(DBG) Log.i(TAG, "onMeasure " + widthMeasureSpec + "x" + heightMeasureSpec + ", " + mVideoWidth + "x" + mVideoHeight + ", " + width + "x" + height);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            if (mVideoWidth * height  > width * mVideoHeight) {
                height = width * mVideoHeight / mVideoWidth;
            } else if (mVideoWidth * height  < width * mVideoHeight) {
                width = height * mVideoWidth / mVideoHeight;
            }
        }

        setMeasuredDimension(width, height);
    }

    public void setVideoSize(int width, int height) {
    	mVideoWidth = width;
    	mVideoHeight = height;
        getHolder().setFixedSize(mVideoWidth, mVideoHeight);
    }
}

