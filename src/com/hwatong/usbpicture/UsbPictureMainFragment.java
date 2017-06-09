package com.hwatong.usbpicture;

import com.hwatong.media.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class UsbPictureMainFragment extends Fragment implements OnClickListener {

	private Button btnPictureResize;
	private ImageView ivVideoPlay;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fangment_usb_picture_main,
				container, false);

		btnPictureResize = (Button) view.findViewById(R.id.btn_picture_resize);
		ivVideoPlay = (ImageView) view.findViewById(R.id.video_play);
		
		btnPictureResize.setOnClickListener(this);
		ivVideoPlay.setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_picture_resize:
			break;

//		case R.id.btn_folder:
//			FragmentTransaction transaction = getFragmentManager()
//					.beginTransaction();
//			transaction.replace(R.id.fangment_radio_main,
//					new FolderFragment()).commit();
//			Toast.makeText(getActivity(), "folder", Toast.LENGTH_SHORT).show();
//			break;

		default:
			break;
		}
	}
}
