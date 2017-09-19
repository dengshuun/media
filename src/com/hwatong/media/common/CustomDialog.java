package com.hwatong.media.common;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

public class CustomDialog extends Dialog implements View.OnClickListener{

	private Button btn_back;

	public CustomDialog(Context context, String strMessage) {
		this(context, R.style.CustomDialog, strMessage);
	}

	public CustomDialog(Context context, int theme, String strMessage) {
		super(context, theme);
		this.setContentView(R.layout.search_dialog);
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		this.setCanceledOnTouchOutside(false);
		this.getWindow().getAttributes().gravity = Gravity.BOTTOM;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

	}

	@Override
	public void onClick(View view) {
		this.dismiss();
	}
}
