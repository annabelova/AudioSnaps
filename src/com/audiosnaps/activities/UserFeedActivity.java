package com.audiosnaps.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.ListView;

import com.audiosnaps.BaseActivity;
import com.audiosnaps.GCMIntentService;
import com.audiosnaps.MainActivity;
import com.audiosnaps.R;
import com.audiosnaps.data.LoggedUser;
import com.audiosnaps.http.HttpConnections;
import com.audiosnaps.log.MyLog;
import com.audiosnaps.tabs.FeedVerticalTab;
import com.sromku.simple.fb.SimpleFacebook;

public class UserFeedActivity extends FragmentActivity {

	private final String TAG = "UserFeedActivity";

	private FeedVerticalTab feedClass = null;
	public static ListView friendFeedViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.feed_vertical_listview);
		if(BaseActivity.DEBUG) MyLog.d(TAG, "onCreate iniciamos app");

		// Obtenemos targetId y abrimos nuevo feed amigo
		Bundle extras = getIntent().getExtras();
		FragmentManager fragmentManager = getSupportFragmentManager();
		friendFeedViewPager = (ListView) findViewById(R.id.feedViewPager2);
		feedClass = new FeedVerticalTab(this, fragmentManager, friendFeedViewPager);

		if(getIntent().getDataString() != null){
			String id = getIntent().getDataString().split("://")[1];
			extras.putString(HttpConnections.USER_TARGET_ID, id);
			if (LoggedUser.id.equalsIgnoreCase(id)) {
				extras.putInt(HttpConnections.FEED_MODE, BaseActivity.MY_FEED);
			} else {
				extras.putInt(HttpConnections.FEED_MODE, BaseActivity.FRIEND_FEED);
				getIntent().putExtra(BaseActivity.POSITION, BaseActivity.POSITION_BOTTOM);
			}
		}
		
		switch (extras.getInt(HttpConnections.FEED_MODE)) {
			case BaseActivity.MAIN_FEED:
				feedClass.initFeed(BaseActivity.MAIN_FEED, extras.getString(HttpConnections.USER_TARGET_ID), null, null);
				break;
			case BaseActivity.FRIEND_FEED:
				if (extras.containsKey(HttpConnections.TIMESTAMP)) {
					feedClass.initFeed(BaseActivity.FRIEND_FEED, extras.getString(HttpConnections.USER_TARGET_ID), extras.getString(HttpConnections.TIMESTAMP), null);
				} else {
					feedClass.initFeed(BaseActivity.FRIEND_FEED, extras.getString(HttpConnections.USER_TARGET_ID), null, null);
				}
				break;
			case BaseActivity.MY_FEED:
				feedClass.initFeed(BaseActivity.MY_FEED, extras.getString(HttpConnections.USER_TARGET_ID), null, null);
				break;
			case BaseActivity.ONE_PICTURE_FEED:
				feedClass.initFeed(BaseActivity.ONE_PICTURE_FEED, extras.getString(HttpConnections.USER_TARGET_ID), null, extras.getString(HttpConnections.PIC_HASH));
				break;
		}

	}
	
	@Override
	protected void onDestroy() {
		
		feedClass = null;
		System.gc();
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		if(!feedClass.onBackPressed()){
			if(getIntent().getBooleanExtra(GCMIntentService.FROM_PUSH_NOTIFICATION, false)){
				Intent intent = new Intent(this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			}			
			super.onBackPressed();
			finish();
		}
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
		SimpleFacebook.getInstance(this).onActivityResult(this, requestCode, resultCode, data);
    }
	
	
}
