package com.hwatong.usbvedio;

import com.hwatong.media.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class UsbVedioMainFragment extends Fragment implements OnClickListener {

	private Button btnLoopMode;
	private ImageView ivVideoPlay;

	private static int LoopMode = 1; // 循环模式，默认为单曲循环（1）、文件夹循环（2）、文件夹随机（3）。

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fangment_usb_vedio_main,
				container, false);

		btnLoopMode = (Button) view.findViewById(R.id.btn_loop_mode);
		ivVideoPlay = (ImageView) view.findViewById(R.id.video_play);

		btnLoopMode.setOnClickListener(this);
		ivVideoPlay.setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_loop_mode:
			if (1 == LoopMode) {
				btnLoopMode.setBackgroundResource(R.drawable.folder_cycle);
				LoopMode = 2;
			} else if (2 == LoopMode) {
				btnLoopMode.setBackgroundResource(R.drawable.folder_random);
				LoopMode = 3;
			} else if (3 == LoopMode) {
				btnLoopMode.setBackgroundResource(R.drawable.single_cycle);
				LoopMode = 1;
			}
			break;

		default:
			break;
		}
	}
}
