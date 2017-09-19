package com.hwatong.usbvideo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver {
	private static final String TAG = "MediaButtonReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive: " + intent);

		String intentAction = intent.getAction();

		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

			if (event == null) {
				return;
			}

			int keyCode = event.getKeyCode();

			switch (keyCode) {

			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			case KeyEvent.KEYCODE_MEDIA_NEXT: 
			case KeyEvent.KEYCODE_MEDIA_PAUSE: 
			case KeyEvent.KEYCODE_MEDIA_PLAY: {
				if (mListener != null)
					mListener.onMediaButton(event);

				if (isOrderedBroadcast()) {
					abortBroadcast();
				}
				break;
			}
			}
		}
	}

	private static MediaButtonListener mListener;
	static void setMediaButtonListener(MediaButtonListener listener) {
		mListener = listener;
	}
	interface MediaButtonListener {
		public void onMediaButton(KeyEvent event);
	}
}
