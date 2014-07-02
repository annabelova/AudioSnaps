package com.audiosnaps.activities;

import android.app.Activity;
import android.os.Bundle;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.log.MyLog;
import com.audiosnaps.tabs.DiscoverTab;

public class DiscoverHashtagActivity extends Activity {

	private final String TAG = "DiscoverHashtagActivity";

	private DiscoverTab discoverClass = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discover);
		if(BaseActivity.DEBUG) MyLog.d(TAG, "onCreate iniciamos app");

		// Obtenemos targetId y abrimos nuevo feed amigo
		Bundle extras = getIntent().getExtras();
		
		if(extras.getString(HttpConnections.HASHTAG) != null) discoverClass = new DiscoverTab(this, this, extras.getString(HttpConnections.HASHTAG));
		else discoverClass = new DiscoverTab(this, this, getIntent().getDataString().split("://")[1]);
		discoverClass.initDiscover();
	}

	@Override
	protected void onDestroy() {
		discoverClass = null;
		System.gc();
		super.onDestroy();
	}

}