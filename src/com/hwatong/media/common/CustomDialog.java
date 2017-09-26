package com.hwatong.media.common;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.WindowManager;

public class CustomDialog extends Dialog{

	public CustomDialog(Context context, String strMessage) {
		this(context, R.style.CustomDialog, strMessage);
	}

	public CustomDialog(Context context, int theme, String strMessage) {
		super(context, theme);
		this.setContentView(R.layout.search_dialog);
		this.setCanceledOnTouchOutside(false);
		this.getWindow().getAttributes().gravity = Gravity.RIGHT;
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

	}
}
