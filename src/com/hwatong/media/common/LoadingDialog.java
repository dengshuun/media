package com.hwatong.media.common;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

public class LoadingDialog extends Dialog {
	public LoadingDialog(Context context, int id) {
		this(context, R.style.LoadingDialog, id);
	}

	public LoadingDialog(Context context, int theme, int id) {
		super(context, theme);
		this.setContentView(R.layout.loading_dialog);
		this.setCanceledOnTouchOutside(true);
		this.getWindow().getAttributes().gravity = Gravity.LEFT;
		this.getWindow().getAttributes().x = 66;
		setText(id);
	}

	public void setText(int id) {
		TextView tvMsg = (TextView) this.findViewById(R.id.txt_loading);
		if (tvMsg != null && id != -1) {
			tvMsg.setText(id);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		if (!hasFocus) {
			// dismiss();
		}
	}
}
