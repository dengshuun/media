package com.hwatong.media;

import com.hwatong.btmusic.BluetoothMusicActivity;
import com.hwatong.ipodui.IPodMainActivity;
import com.hwatong.usbmusic.UsbMusicActivity;
import com.hwatong.usbpicture.UsbPictureActivity;
import com.hwatong.usbvedio.UsbVedioActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 主界面
 * 
 * @author Administrator
 * 
 */
public class MainActivity extends Activity implements OnClickListener {

	private RelativeLayout rl_usb;
	private RelativeLayout rl_btmusic;
	private RelativeLayout rl_ipod;
	private RelativeLayout rl_right;

	private TextView txt_usb;
	private TextView txt_btmusic;
	private TextView txt_ipod;
	private TextView txt_device;
	
	private Button btn_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();
	}

	private void initView(){
		rl_usb = (RelativeLayout) findViewById(R.id.usb_layout);
		rl_btmusic = (RelativeLayout) findViewById(R.id.btmusic_layout);
		rl_ipod = (RelativeLayout) findViewById(R.id.ipod_layout);
		rl_right = (RelativeLayout) findViewById(R.id.right_layout);

		txt_usb = (TextView) findViewById(R.id.txt_usb);
		txt_btmusic = (TextView) findViewById(R.id.txt_btmusic);
		txt_ipod = (TextView) findViewById(R.id.txt_ipod);
		txt_device = (TextView) findViewById(R.id.txt_device_status);
		
		btn_back = (Button) findViewById(R.id.btn_back);

		rl_usb.setOnClickListener(this);
		rl_btmusic.setOnClickListener(this);
		rl_ipod.setOnClickListener(this);
		btn_back.setOnClickListener(this);
	}
	
	private void changeToUsb() {
		txt_usb.setText(R.string.txt_music);
		txt_btmusic.setText(R.string.txt_video);
		txt_ipod.setText(R.string.txt_picture);
		rl_right.setBackgroundResource(R.drawable.usb_right_bg);
		txt_device.setText("");
	}
	
	private void changeToMedia() {
		txt_usb.setText(R.string.txt_usb);
		txt_btmusic.setText(R.string.txt_btmusic);
		txt_ipod.setText(R.string.txt_ipod);
		rl_right.setBackgroundResource(R.drawable.media_right_bg);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.usb_layout) {
			if (txt_usb.getText().equals("USB")) {
				changeToUsb();
			} else {
				Intent i = new Intent(MainActivity.this, UsbMusicActivity.class);
				startActivity(i);
			}
		} else if (v.getId() == R.id.btmusic_layout) {
			if (txt_btmusic.getText().equals("蓝牙音乐")) {
				Intent i = new Intent(MainActivity.this,
						BluetoothMusicActivity.class);
				startActivity(i);
			} else {
				Intent i = new Intent(MainActivity.this, UsbVedioActivity.class);
				startActivity(i);
			}
		} else if (v.getId() == R.id.ipod_layout) {
			if (txt_ipod.getText().equals("IPOD")) {
				Intent i = new Intent(MainActivity.this, IPodMainActivity.class);
				startActivity(i);
			} else {
				Intent i = new Intent(MainActivity.this,
						UsbPictureActivity.class);
				startActivity(i);
			}
		}else if (v.getId() == R.id.btn_back) {
			if (txt_usb.getText().equals("USB")) {
				this.finish();
			} else {
				changeToMedia();
			}
		}
	}
}
