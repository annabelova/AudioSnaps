package com.audiosnaps.activities;

import android.app.Activity;
import android.os.Bundle;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.R;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.tabs.FriendsTab;
import com.google.analytics.tracking.android.EasyTracker;

public class UserFriendsFollowersActivity extends Activity {

	@SuppressWarnings("unused")
	private final String TAG = "UserFriendsFollowersActivity";

	private FriendsTab friendsClass = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends);


		// Obtenemos targetId y abrimos nuevo feed amigo
		Bundle extras = getIntent().getExtras();
		friendsClass = new FriendsTab(this, this);
		friendsClass.initFriends(BaseActivity.FRIEND_LIST, extras.getString(HttpConnections.USER_TARGET_ID), extras.getBoolean(HttpConnections.FRIENDS));
	}
	
	
	@Override
	protected void onDestroy() {
		friendsClass = null;
		System.gc();
		super.onDestroy();
	}

}
