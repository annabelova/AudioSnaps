package com.audiosnaps.gesture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.audiosnap.library.util.BitmapUtil;
import com.audiosnaps.BaseActivity;
import com.audiosnaps.FullScreenPictureActivity;
import com.audiosnaps.MainActivity;
import com.audiosnaps.activities.UserFeedActivity;
import com.audiosnaps.classes.Animaciones;
import com.audiosnaps.fragments.AudioSnapFragment;
import com.audiosnaps.log.MyLog;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;

public class SwipeDetector implements View.OnTouchListener {

	static final String logTag = "SwipeDetector";
	private View feedPhoto, feedHeader, feedUserInfo, feedComments, userNotifications, captionBox, likesCommentsBox;
	private Animaciones animaciones;
	static final int MIN_DISTANCE = 15;
	private int position = BaseActivity.POSITION_CENTER;
	private float downX, downY, upX, upY;
	private Activity activity;
	private String picHash, caption;
	private AudioSnapFragment fragment;
	private InputMethodManager inputManager;
	private int feedMode;
	private boolean enabled;
	private boolean clickedEnabled;
	
	public SwipeDetector(Animaciones animaciones, View feedPhoto, View feedHeader, View feedUserInfo, View feedComments, View userNotifications, String picHash, String caption, Activity activity,
			AudioSnapFragment fragment, View captionBox, View likesCommentsBox, int feedMode) {
		this.feedPhoto = feedPhoto;
		this.feedHeader = feedHeader;
		this.feedUserInfo = feedUserInfo;
		this.feedComments = feedComments;
		this.animaciones = animaciones;
		this.userNotifications = userNotifications;
		this.picHash = picHash;
		this.caption = caption;
		this.activity = activity;
		this.fragment = fragment;
		this.captionBox = captionBox;
		this.likesCommentsBox = likesCommentsBox;
		this.feedMode = feedMode;
		this.enabled = true;
		this.clickedEnabled = false;
	}

	public void onRightToLeftSwipe() {
		if(BaseActivity.DEBUG) MyLog.i(logTag, "RightToLeftSwipe!");
	}

	public void onLeftToRightSwipe() {
		if(BaseActivity.DEBUG) MyLog.i(logTag, "LeftToRightSwipe!");
	}

	public void onTopToBottomSwipe(){
		onTopToBottomSwipe(false);
	}
	
	public void onTopToBottomSwipe(boolean durationZero) {
		MyLog.i(logTag, "!!! onTopToBottomSwipe");
		/*
		
		if(BaseActivity.DEBUG) MyLog.i(logTag, "onTopToBottomSwipe!");
		
		int margenPestañaFeed;
		
		// Cerrar teclado
		try {
			inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		switch (position) {
			case BaseActivity.POSITION_TOP:
				
				if(likesCommentsBox.getVisibility() == View.INVISIBLE){
					margenPestañaFeed = captionBox.getHeight() - BitmapUtil.getPixels(5, fragment.getActivity());
				}else{
					margenPestañaFeed = BitmapUtil.getPixels(40, fragment.getActivity());
				}
				
				animaciones.translatePhotoLayout(position, margenPestañaFeed, feedPhoto, feedHeader, feedUserInfo, feedComments, userNotifications, false, durationZero, getAnimatorListener());
				position = BaseActivity.POSITION_CENTER;
				unblockViewPagers();
				
				fragment.releaseComments();
				
				break;
			case BaseActivity.POSITION_CENTER:
				
				if(captionBox.getVisibility() == View.INVISIBLE){
					margenPestañaFeed = captionBox.getHeight() - BitmapUtil.getPixels(5, fragment.getActivity());
				}else{
					margenPestañaFeed = BitmapUtil.getPixels(40, fragment.getActivity());
				}
				
				animaciones.translatePhotoLayout(position, margenPestañaFeed, feedPhoto, feedHeader, feedUserInfo, feedComments, userNotifications, false, durationZero, getAnimatorListener());
				position = BaseActivity.POSITION_BOTTOM;
				blockViewPagers();
				
				if(feedMode == BaseActivity.MY_FEED) fragment.upadteNotifications();
				
				try {
					fragment.queryUserInfo(feedMode);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				break;
		}
		*/
	}

	public void onBottomToTopSwipe(){
		onBottomToTopSwipe(false);
	}
	
	public void onBottomToTopSwipe(boolean durationZero) {
		
		/*
		
		if(BaseActivity.DEBUG) MyLog.i(logTag, "onBottomToTopSwipe!");
		
		int margenPestañaFeed;
		
		// Cerrar teclado
		try {
			inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		switch (position) {
		case BaseActivity.POSITION_BOTTOM:
			
			if(likesCommentsBox.getVisibility() == View.INVISIBLE){
				margenPestañaFeed = captionBox.getHeight() - BitmapUtil.getPixels(5, fragment.getActivity());
			}else{
				margenPestañaFeed = BitmapUtil.getPixels(40, fragment.getActivity());
			}
			
			animaciones.translatePhotoLayout(position, margenPestañaFeed, feedPhoto, feedHeader, feedUserInfo, feedComments, userNotifications, true, durationZero, getAnimatorListener());
			position = BaseActivity.POSITION_CENTER;
			unblockViewPagers();
			
			break;
		case BaseActivity.POSITION_CENTER:
			if (!fragment.noPhotosFragment()) {
				
				if(captionBox.getVisibility() == View.INVISIBLE){
					margenPestañaFeed = captionBox.getHeight() - BitmapUtil.getPixels(5, fragment.getActivity());
				}else{
					margenPestañaFeed = BitmapUtil.getPixels(40, fragment.getActivity());
				}
				
				animaciones.translatePhotoLayout(position, margenPestañaFeed, feedPhoto, feedHeader, feedUserInfo, feedComments, userNotifications, true, durationZero, getAnimatorListener());
				position = BaseActivity.POSITION_TOP;
				blockViewPagers();
			}
			
			try {
				fragment.queryComments();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			break;
		}
		*/
	}

	public int getPosition() {
		return position;
	}

	// check swipe type
	public boolean onTouch(View v, MotionEvent event) {
	
		
		if(enabled()){
			switch (event.getAction()) {
			
			
				case MotionEvent.ACTION_DOWN: {
					downX = event.getX();
					downY = event.getY();
					if(BaseActivity.DEBUG) MyLog.i(logTag, "action down, x:" + downX + ", y:" + downY);
					return true;
				}
				case MotionEvent.ACTION_UP: {
					upX = event.getX();
					upY = event.getY();
		
					float deltaX = downX - upX;
					float deltaY = downY - upY;
		
					if(BaseActivity.DEBUG) MyLog.i(logTag, "action up, deltaX:" + deltaX + ", deltaY:" + deltaY);
		
					// swipe horizontal?
					// if (Math.abs(deltaX) > MIN_DISTANCE) {
					// // left or right
					// if (deltaX < 0) {
					// this.onLeftToRightSwipe();
					// return true;
					// }
					// if (deltaX > 0) {
					// this.onRightToLeftSwipe();
					// return true;
					// }
					// } else {
					// if(BaseActivity.DEBUG) MyLog.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long, need at least " + MIN_DISTANCE);
					// return false; // We don't consume the event
					// }
		
					// swipe vertical?
					if (Math.abs(deltaY) > MIN_DISTANCE) {
						// top or down
						/*
						if (deltaY < 0) {
							this.onTopToBottomSwipe();
							return true;
						}
						if (deltaY > 0) {
							this.onBottomToTopSwipe();
							return true;
						}*/
					} else {
						// Detected tap
						//if (this.clickedEnabled) launchFullScreenPictureActivity();
						if(BaseActivity.DEBUG) MyLog.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long, need at least " + MIN_DISTANCE);
						return true; // We consume the event
					}
		
					return true;
				}
			}
		}
		
		return false;
		
	}

	// Métodos que bloquean y desbloquean los viewPagers
	public void blockViewPagers() {
		 
		/*
		try {
	//		MainActivity.mainFeedViewPager.setPagingEnabled(false);
		} catch (Exception e) {
		}
		try {
	//		MainActivity.myFeedViewPager.setPagingEnabled(false);
		} catch (Exception e) {
		}
		try {
			UserFeedActivity.friendFeedViewPager.setPagingEnabled(false);
		} catch (Exception e) {
		}
		
		if(captionBox.getVisibility() == View.INVISIBLE){
			captionBox.setVisibility(View.VISIBLE);
			likesCommentsBox.setVisibility(View.INVISIBLE);
		}else{
			captionBox.setVisibility(View.GONE);
			likesCommentsBox.setVisibility(View.GONE);
		}
		*/
	}

	public void unblockViewPagers() {
		
		/*
		try {
	//		MainActivity.mainFeedViewPager.setPagingEnabled(true);
		} catch (Exception e) {
		}
		try {
	//		MainActivity.myFeedViewPager.setPagingEnabled(true);
		} catch (Exception e) {
		}
		try {
			UserFeedActivity.friendFeedViewPager.setPagingEnabled(true);
		} catch (Exception e) {
		}
		if(likesCommentsBox.getVisibility() == View.INVISIBLE){
			captionBox.setVisibility(View.INVISIBLE);
			likesCommentsBox.setVisibility(View.VISIBLE);
		}else{
			captionBox.setVisibility(View.VISIBLE);
			likesCommentsBox.setVisibility(View.VISIBLE);
		}
		*/
	}

	private void launchFullScreenPictureActivity() {
/*
		if (position == BaseActivity.POSITION_CENTER) {
			if(picHash != null && caption != null){
				Intent intent = new Intent(activity, FullScreenPictureActivity.class);
				intent.putExtra("picHash", picHash);
				intent.putExtra("captionApp", Html.fromHtml(caption).toString());
				if (fragment.audioPlayer != null)
						fragment.audioPlayer.stop();
				activity.startActivity(intent);
				if(BaseActivity.DEBUG) MyLog.d(logTag, "Pulsamos imagen mostrar activity de foto individual");
			}
		}
*/
	}
	
	public synchronized void enable(){
		this.enabled = true;
	}
	
	public synchronized void disable(){
		this.enabled = false;
	}

	private synchronized boolean enabled(){
		return enabled;
	}
	
	private AnimatorListener getAnimatorListener(){
		return new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				disable();
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				enable();
				
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				enable();
			}
		};
	}
	
	public void setClickedEnabled(boolean b) {
		this.clickedEnabled = b;
	}
}